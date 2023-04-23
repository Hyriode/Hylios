package fr.hyriode.hylios.balancing;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.packet.HyriChannel;
import fr.hyriode.api.scheduler.IHyriScheduler;
import fr.hyriode.api.server.IHyriServerManager;
import fr.hyriode.api.server.ILobbyAPI;
import fr.hyriode.api.server.event.LobbyRestartingEvent;
import fr.hyriode.api.server.packet.RemoteStateEditPacket;
import fr.hyriode.hyggdrasil.api.event.model.HyggTemplateUpdatedEvent;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerCreationInfo;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.util.HyliosException;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by AstFaster
 * on 15/07/2022 at 12:01
 */
public class LobbyBalancer {

    private boolean restarting = false;

    public LobbyBalancer() {
        System.out.println("Starting lobbies balancing tasks...");

        final IHyriScheduler scheduler = HyriAPI.get().getScheduler();

        scheduler.schedule(() -> HyriAPI.get().getRedisProcessor().process(jedis -> {
            for (String lobby : jedis.zrange(ILobbyAPI.BALANCER_KEY, 0, -1)) { // Delete all lobbies from balancer (in case one is idling or stopping)
                jedis.zrem(ILobbyAPI.BALANCER_KEY, lobby);
            }

            for (HyggServer lobby : this.getWorkingLobbies()) { // Only add working lobbies
                jedis.zadd(ILobbyAPI.BALANCER_KEY, lobby.getPlayingPlayers().size(), lobby.getName());
            }
        }), 1000, 500, TimeUnit.MILLISECONDS);

        scheduler.schedule(this::process, 5, 30, TimeUnit.SECONDS);

        // Lobby update check
        HyriAPI.get().getHyggdrasilManager().getHyggdrasilAPI().getEventBus().subscribe(HyggTemplateUpdatedEvent.class, event -> {
            final String template = event.getTemplate();

            if (template.equals(ILobbyAPI.TYPE) && !this.restarting) {
                System.out.println("Restarting all lobbies! An update was detected.");

                this.restarting = true;

                final Queue<HyggServer> lobbies = new ArrayDeque<>(this.getWorkingLobbies());

                for (HyggServer lobby : lobbies) {
                    HyriAPI.get().getPubSub().send(HyriChannel.SERVERS, new RemoteStateEditPacket(lobby.getName(), HyggServer.State.SHUTDOWN));
                }

                if (lobbies.size() > 0)  {
                    this.restartLobby(lobbies.poll(), lobbies);
                }
            }
        });
    }

    private void restartLobby(HyggServer lobby, Queue<HyggServer> lobbies) {
        final IHyriServerManager serverManager = HyriAPI.get().getServerManager();
        final IHyriScheduler scheduler = HyriAPI.get().getScheduler();

        System.out.println("Restarting '" + lobby.getName() + "'...");

        this.startLobby(newLobby -> {
            HyriAPI.get().getNetworkManager().getEventBus().publish(new LobbyRestartingEvent(lobby.getName(), 30));

            scheduler.schedule(() -> {
                HyriAPI.get().getLobbyAPI().evacuateToLobby(lobby.getName());

                scheduler.schedule(() -> {
                    serverManager.removeServer(lobby.getName(), null);

                    if (lobbies.size() == 0) {
                        this.restarting = false;
                    } else {
                        final HyggServer nextLobby = lobbies.poll();

                        this.restartLobby(nextLobby, lobbies);
                    }
                }, 5, TimeUnit.SECONDS);
            }, 30L, TimeUnit.SECONDS);
        });
    }

    private void process() {
        final List<HyggServer> lobbies = this.getWorkingLobbies();
        final int currentLobbies = lobbies.size();
        final int neededLobbies = this.neededLobbies();

        if (this.restarting) {
            return;
        }

        if (currentLobbies > neededLobbies) {
            lobbies.sort(Comparator.comparingLong(HyggServer::getStartedTime).reversed()); // Compare servers by their time started time: young servers are prioritized.

            for (int i = 0; i < currentLobbies - neededLobbies; i++) {
                final HyggServer lobby = lobbies.get(i);

                this.evacuateLobby(lobby); // Evacuate the lobby

                try { // Wait 1s for proxies to evacuate players
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new HyliosException(e);
                }

                HyriAPI.get().getRedisProcessor().process(jedis -> jedis.zrem(ILobbyAPI.BALANCER_KEY, lobby.getName())); // Remove lobby from balancer
                HyriAPI.get().getServerManager().removeServer(lobby.getName(), null); // Kill server process
            }
        } else if (currentLobbies < neededLobbies) {
            for (int i = 0; i < neededLobbies - currentLobbies; i++) {
                this.startLobby(server -> System.out.println("Started '" + server.getName() + "' (current: " + (currentLobbies) + ")."));
            }
        }
    }

    private void evacuateLobby(HyggServer lobby) {
        final List<HyggServer> lobbies = this.getWorkingLobbies();
        final Queue<UUID> players = new LinkedBlockingQueue<>(lobby.getPlayers()); // Create a queue of players to evacuate

        lobbies.sort(Comparator.comparingInt(o -> o.getPlayers().size()));  // Sort lobbies by lower to greater amount of players

        for (HyggServer server : lobbies) {
            if (server.getName().equals(lobby.getName())) {
                continue;
            }

            for (int i = 0; i < server.getSlots() - server.getPlayers().size(); i++) { // For-each the available slots of the lobby
                if (players.size() == 0) { // No more players to evacuate
                    return;
                }

                final UUID player = players.poll(); // Remove a player from the queue (declared as evacuated)

                HyriAPI.get().getServerManager().sendPlayerToServer(player, server.getName());
            }
        }
    }

    private void startLobby(Consumer<HyggServer> onStarted) {
        final HyggServerCreationInfo request = new HyggServerCreationInfo(ILobbyAPI.TYPE)
                .withAccessibility(HyggServer.Accessibility.PUBLIC)
                .withProcess(HyggServer.Process.PERMANENT)
                .withSlots(ILobbyAPI.MAX_PLAYERS);

        HyriAPI.get().getServerManager().createServer(request, onStarted);
    }

    private int neededLobbies() {
        final int minLobbies = Hylios.get().getConfig().minLobbies();

        int neededLobbies = (int) Math.ceil((double) this.getLobbyPlayers() * 1.5 / ILobbyAPI.MAX_PLAYERS); // The "perfect" amount of lobbies needed

        if (neededLobbies < minLobbies) {
            neededLobbies = minLobbies;
        }
        return neededLobbies;
    }

    private int getLobbyPlayers() {
        return HyriAPI.get().getNetworkManager().getPlayerCounter().getCategory(ILobbyAPI.TYPE).getPlayers();
    }

    private List<HyggServer> getWorkingLobbies() {
        return HyriAPI.get().getLobbyAPI().getLobbies()
                .stream()
                .filter(server -> server.getState() == HyggServer.State.READY)
                .collect(Collectors.toList());
    }

}
