package fr.hyriode.hylios.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.host.HostData;
import fr.hyriode.api.impl.common.queue.HyriQueue;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.api.player.IHyriPlayerSession;
import fr.hyriode.api.queue.event.PlayerJoinQueueEvent;
import fr.hyriode.api.queue.event.PlayerLeaveQueueEvent;
import fr.hyriode.api.queue.event.QueueDisabledEvent;
import fr.hyriode.api.queue.event.QueueUpdatedEvent;
import fr.hyriode.api.scheduler.IHyriTask;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerCreationInfo;
import fr.hyriode.hylios.Hylios;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static fr.hyriode.api.queue.IHyriQueue.Type;

/**
 * Created by AstFaster
 * on 04/12/2022 at 17:02
 */
public class Queue {

    private final PriorityBlockingQueue<Set<UUID>> groupsQueue;

    private final IHyriTask playersProcess;
    private IHyriTask startingProcess;

    private final HyriQueue handle;

    public Queue(String server) {
        this(new HyriQueue(Type.SERVER, null, null, null, server));
    }

    public Queue(String game, String gameType, String map) {
        this(new HyriQueue(Type.GAME, game, gameType, map, null));
    }

    private Queue(HyriQueue handle) {
        this.handle = handle;
        this.groupsQueue = new PriorityBlockingQueue<>(100000, Comparator.comparingInt(this::calculatePriority));
        this.playersProcess = HyriAPI.get().getScheduler().schedule(this::processPlayers, 0, 500, TimeUnit.MILLISECONDS);

        if (this.handle.getType() == Type.GAME) {
            this.startingProcess = HyriAPI.get().getScheduler().schedule(this::processServersStarting, 0, 2, TimeUnit.SECONDS);
        }
    }

    public void disable() {
        this.playersProcess.cancel();

        if (this.startingProcess != null) {
            this.startingProcess.cancel();
        }

        for (UUID player : this.handle.getPlayers()) {
            final IHyriPlayerSession session = IHyriPlayerSession.get(player);

            if (session == null) {
                continue;
            }

            session.setQueue(null);
            session.update();
        }

        HyriAPI.get().getNetworkManager().getEventBus().publish(new QueueDisabledEvent(this.handle));
        Hylios.get().getQueueManager().removeQueue(this);
        HyriAPI.get().getScheduler().schedule(() -> HyriAPI.get().getQueueManager().deleteQueue(this.handle.getId()), 10, TimeUnit.SECONDS);
    }

    private void processPlayers() {
        this.groupsQueue.addAll(this.handle.getTotalPlayers());

        if (this.handle.getType() == Type.GAME) {
            this.processGame();
        } else if (this.handle.getType() == Type.SERVER) {
            this.processServer();
        }

        this.groupsQueue.clear(); // Finally, clear process queue
    }

    private void processGame() {
        final List<HyggServer> availableServers = new ArrayList<>(this.getGameServers().stream()
                .filter(server -> server.getState() == HyggServer.State.READY)
                .filter(server -> server.getPlayingPlayers().size() < server.getSlots())
                .filter(server -> System.currentTimeMillis() - server.getStartedTime() > 15 * 1000L)
                .sorted((o1, o2) -> o2.getPlayingPlayers().size() - o1.getPlayingPlayers().size()) // Sort servers by the highest amount of players
                .toList()); // Get servers that players could join

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
                        HyriAPI.get().getServerManager().sendPlayerToServer(player, serverName);

                        this.removePlayer(player);
                    }

                    players.put(serverName, serverPlayers + group.size()); // Add teleported players in the copy
                    break;
                }
            }
        }
    }

    private void processServersStarting() {
        final List<HyggServer> servers = this.getGameServers();
        final List<HyggServer> potentialServers = servers.stream().filter(server -> server.getState() != HyggServer.State.PLAYING).toList();
        final int totalPlayers = servers.stream().mapToInt(server -> server.getPlayingPlayers().size()).sum() + this.groupsQueue.stream().mapToInt(Set::size).sum(); // Playing players + players in queue
        final int slots = servers.stream()
                .filter(server -> server.getSlots() != -1)
                .findFirst()
                .map(HyggServer::getSlots)
                .orElse(-1);
        final int neededServers = slots == -1 ? 1 : (int) Math.ceil((double) totalPlayers * 1.5 / slots);

        if (potentialServers.size() < neededServers) { // Not enough servers started
            for (int i = 0; i < neededServers - potentialServers.size(); i++) {
                final HyggServerCreationInfo serverInfo = new HyggServerCreationInfo(this.handle.getGame())
                        .withGameType(this.handle.getGameType())
                        .withMap(this.handle.getMap())
                        .withProcess(HyggServer.Process.TEMPORARY)
                        .withAccessibility(HyggServer.Accessibility.PUBLIC)
                        .withSlots(slots);

                HyriAPI.get().getServerManager().createServer(serverInfo, null); // Create a new server
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

            final IHyriPlayer ownerAccount = IHyriPlayer.get(hostData.getOwner());

            if (hostData.isWhitelisted()) {
                for (Set<UUID> group : this.groupsQueue) {
                    for (UUID player : group) {
                        if (players >= server.getSlots()) { // Check if the server is full
                            return;
                        }

                        if (ownerAccount.getHosts().hasBannedPlayer(player)) { // Check if the player is banned
                            continue;
                        }

                        if (!hostData.getWhitelistedPlayers().contains(player) && !hostData.getSecondaryHosts().contains(player)) { // Check if the player is whitelisted
                            continue;
                        }

                        HyriAPI.get().getServerManager().sendPlayerToServer(player, server.getName());

                        this.removePlayer(player);
                        players++;
                    }

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                for (Set<UUID> group : this.groupsQueue) {
                    for (UUID player : group) {
                        if (players >= server.getSlots()) { // Check if the server is full
                            return;
                        }

                        if (ownerAccount.getHosts().hasBannedPlayer(player)) { // Check if the player is banned
                            continue;
                        }

                        HyriAPI.get().getServerManager().sendPlayerToServer(player, server.getName());

                        this.removePlayer(player);
                        players++;
                    }

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return;
        }

        for (Set<UUID> group : this.groupsQueue) {
            if (players + group.size() > server.getSlots()) { // Check if the server is full
                return;
            }

            for (UUID player : group) {
                HyriAPI.get().getServerManager().sendPlayerToServer(player, server.getName());

                this.removePlayer(player);
                players++;
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
                final HyggServer.State state = server.getState();

                if (state == HyggServer.State.SHUTDOWN || state == HyggServer.State.IDLE) {
                    continue;
                }

                if (server.getAccessibility() != HyggServer.Accessibility.PUBLIC) {
                    continue;
                }

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

        if (session != null) {
            session.setQueue(this.handle.getId());
            session.update();
        }

        this.handle.addPlayer(player);

        this.updateInfo();

        HyriAPI.get().getNetworkManager().getEventBus().publish(new PlayerJoinQueueEvent(this.handle, player));
    }

    public void removePlayer(UUID player) {
        final IHyriPlayerSession session = IHyriPlayerSession.get(player);

        if (session != null) {
            session.setQueue(null);
            session.update();
        }

        this.handle.removePlayer(player);

        HyriAPI.get().getNetworkManager().getEventBus().publish(new PlayerLeaveQueueEvent(this.handle, player));

        this.updateInfo();
    }

    public boolean containsPlayer(UUID playerId) {
        return this.handle.getPlayers().contains(playerId);
    }

    public HyriQueue getHandle() {
        return this.handle;
    }

}
