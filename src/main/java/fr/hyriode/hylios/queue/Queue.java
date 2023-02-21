package fr.hyriode.hylios.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.host.HostData;
import fr.hyriode.api.impl.common.queue.HyriQueue;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.api.player.IHyriPlayerSession;
import fr.hyriode.api.queue.event.QueueDisabledEvent;
import fr.hyriode.api.queue.event.QueueUpdatedEvent;
import fr.hyriode.api.scheduler.IHyriTask;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerCreationInfo;
import fr.hyriode.hylios.Hylios;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import static fr.hyriode.api.queue.IHyriQueue.Type;

/**
 * Created by AstFaster
 * on 04/12/2022 at 17:02
 */
public class Queue {

    private final PriorityBlockingQueue<Set<UUID>> groupsQueue;

    private final IHyriTask process;
    private final HyriQueue handle;

    public Queue(String server) {
        this(new HyriQueue(Type.SERVER, null, null, null, server));
    }

    public Queue(String game, String gameType, String map) {
        this(new HyriQueue(Type.SERVER, game, gameType, map, null));
    }

    private Queue(HyriQueue handle) {
        this.handle = handle;
        this.groupsQueue = new PriorityBlockingQueue<>(100000, Comparator.comparingInt(this::calculatePriority));
        this.process = HyriAPI.get().getScheduler().schedule(this::process, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void disable() {
        this.process.cancel();

        for (UUID player : this.handle.getPlayers()) {
            final IHyriPlayerSession session = IHyriPlayerSession.get(player);

            if (session == null) {
                continue;
            }

            session.setQueue(null);
            session.update();
        }

        HyriAPI.get().getQueueManager().deleteQueue(this.handle.getId());
        HyriAPI.get().getNetworkManager().getEventBus().publish(new QueueDisabledEvent(this.handle));

        Hylios.get().getQueueManager().removeQueue(this);
    }

    private void process() {
        this.groupsQueue.addAll(this.handle.getTotalPlayers());

        if (this.handle.getType() == Type.GAME) {
            this.processGame();
        } else if (this.handle.getType() == Type.SERVER) {
            this.processServer();
        }

        this.groupsQueue.clear(); // Finally, clear process queue
    }

    private void processGame() {
        final List<HyggServer> servers = this.getGameServers();
        final int totalPlayers = servers.stream().mapToInt(server -> server.getPlayingPlayers().size()).sum() + this.groupsQueue.stream().mapToInt(Set::size).sum(); // Playing players + players in queue
        final int slots = servers.stream().filter(server -> server.getState() == HyggServer.State.READY || server.getState() == HyggServer.State.PLAYING).findFirst().map(HyggServer::getSlots).orElse(-1);

        if (slots == -1) {
            this.disable();
            return;
        }

        final int neededServers = (int) Math.ceil((double) totalPlayers * 1.5 / slots);
        final int currentServers = servers.size();

        if (neededServers > currentServers) { // Not enough servers started
            for (int i = 0; i < neededServers - currentServers; i++) {
                final HyggServerCreationInfo serverInfo = new HyggServerCreationInfo(this.handle.getGame())
                        .withGameType(this.handle.getGameType())
                        .withMap(this.handle.getMap())
                        .withProcess(HyggServer.Process.TEMPORARY)
                        .withAccessibility(HyggServer.Accessibility.PUBLIC)
                        .withSlots(slots);

                HyriAPI.get().getServerManager().createServer(serverInfo, null); // Create a new server
            }
        }

        final List<HyggServer> availableServers = new ArrayList<>(servers.stream().filter(server -> server.getState() == HyggServer.State.READY && server.getPlayingPlayers().size() < server.getSlots()).toList()); // Get servers that players could join

        // Sort servers by the highest amount of players
        availableServers.sort(Comparator.comparingInt(server -> server.getPlayingPlayers().size()));

        final Map<String, Integer> players = new HashMap<>(); // Create a copy of players (for synchronisation)

        for (HyggServer server : availableServers) {
            players.put(server.getName(), server.getPlayingPlayers().size());
        }

        for (Set<UUID> group : this.groupsQueue) {
            for (HyggServer server : availableServers) { // Check for a good server
                final String serverName = server.getName();
                final int serverPlayers = players.get(serverName);

                if (serverPlayers + group.size() <= server.getSlots()) { // Check if the server can handle all the players in the group
                    for (UUID player : group) { // Teleport each player
                        final IHyriPlayerSession session = IHyriPlayerSession.get(player);

                        if (session != null) {
                            session.setQueue(null);
                            session.update();
                        }

                        HyriAPI.get().getServerManager().sendPlayerToServer(player, serverName);
                    }

                    players.put(serverName, serverPlayers + group.size()); // Add teleported players in the copy
                    break;
                }
            }
        }
    }

    private void processServer() {
        final HyggServer server = this.getServer();

        if (server == null) { // Server doesn't exist anymore
            this.disable();
            return;
        }

        int players = server.getPlayingPlayers().size();

        if (server.getState() != HyggServer.State.READY || players >= server.getSlots()) {
            return;
        }

        if (server.getAccessibility() == HyggServer.Accessibility.HOST) {
            final HostData hostData = HyriAPI.get().getHostManager().getHostData(server);

            if (hostData == null) {
                return;
            }

            if (hostData.isWhitelisted()) {
                for (Set<UUID> group : this.groupsQueue) {
                    for (UUID player : group) {
                        if (players >= server.getSlots()) { // Check if the server is full
                            return;
                        }

                        if (!hostData.getWhitelistedPlayers().contains(player)) { // Check if the player is whitelisted
                            continue;
                        }

                        final IHyriPlayerSession session = IHyriPlayerSession.get(player);

                        if (session != null) {
                            session.setQueue(null);
                            session.update();
                        }

                        HyriAPI.get().getServerManager().sendPlayerToServer(player, server.getName());
                        players++;
                    }
                }
                return;
            }
        }

        for (Set<UUID> group : this.groupsQueue) {
            if (players + group.size() >= server.getSlots()) { // Check if the server is full
                return;
            }

            for (UUID player : group) {
                final IHyriPlayerSession session = IHyriPlayerSession.get(player);

                if (session != null) {
                    session.setQueue(null);
                    session.update();
                }

                HyriAPI.get().getServerManager().sendPlayerToServer(player, server.getName());
                players++;
            }
        }
    }

    private void updateInfo() {
        HyriAPI.get().getQueueManager().updateQueue(this.handle);
        HyriAPI.get().getNetworkManager().getEventBus().publish(new QueueUpdatedEvent(this.handle));
    }

    private int calculatePriority(Set<UUID> group) {
        int priority = -1;

        for (UUID member : group) {
            final IHyriPlayer account = IHyriPlayer.get(member);

            if (priority == -1 || account.getPriority() < priority) {
                priority = account.getPriority();
            }
        }
        return priority;
    }

    private List<HyggServer> getGameServers() {
        final List<HyggServer> servers = new ArrayList<>();

        for (HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            if (server.getType().equals(this.handle.getGame()) && Objects.equals(server.getGameType(), this.handle.getGameType()) && (this.handle.getMap() == null || Objects.equals(server.getMap(), this.handle.getMap()))) {
                servers.add(server);
            }
        }
        return servers;
    }

    private HyggServer getServer() {
        return HyriAPI.get().getServerManager().getServer(this.handle.getServer());
    }

    public void addPlayer(UUID player) {
        final IHyriPlayerSession session = IHyriPlayerSession.get(player);

        if (session == null) {
            return;
        }

        session.setQueue(this.handle.getId());
        session.update();

        this.handle.addPlayer(player);

        this.updateInfo();
    }

    public void removePlayer(UUID player) {
        this.handle.removePlayer(player);

        this.updateInfo();

        final IHyriPlayerSession session = IHyriPlayerSession.get(player);

        if (session == null) {
            return;
        }

        session.setQueue(null);
        session.update();

        if (this.handle.getPlayers().size() == 0) {
            this.disable();
        }
    }

    public boolean containsPlayer(UUID playerId) {
        return this.handle.getPlayers().contains(playerId);
    }

    public HyriQueue getHandle() {
        return this.handle;
    }

}
