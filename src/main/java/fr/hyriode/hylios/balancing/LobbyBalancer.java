package fr.hyriode.hylios.balancing;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.scheduler.IHyriScheduler;
import fr.hyriode.hyggdrasil.api.event.HyggEventBus;
import fr.hyriode.hyggdrasil.api.event.model.server.HyggServerUpdatedEvent;
import fr.hyriode.hyggdrasil.api.protocol.environment.HyggData;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerRequest;
import fr.hyriode.hyggdrasil.api.server.HyggServerState;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.api.lobby.LobbyAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 15/07/2022 at 12:01
 */
public class LobbyBalancer {

    private final List<String> lobbies;
    private final List<String> startedLobbies;

    public LobbyBalancer() {
        this.lobbies = new CopyOnWriteArrayList<>();
        this.startedLobbies = new ArrayList<>();

        final HyggEventBus eventBus = HyriAPI.get().getHyggdrasilManager().getHyggdrasilAPI().getEventBus();

        eventBus.subscribe(HyggServerUpdatedEvent.class, event -> this.onUpdate(event.getServer()));
        eventBus.subscribe(HyggServerUpdatedEvent.class, event -> this.onStop(event.getServer()));

        this.startTasks();
    }

    private void startTasks() {
        System.out.println("Starting lobby balancing tasks...");

        final IHyriScheduler scheduler = HyriAPI.get().getScheduler();

        HyriAPI.get().getRedisProcessor().process(jedis -> {
            System.out.println("Removing old lobbies from balancer...");

            for (String lobby : jedis.zrange(LobbyAPI.REDIS_KEY, 0, -1)) {
                jedis.zrem(LobbyAPI.REDIS_KEY, lobby);
            }
        });

        scheduler.schedule(() -> {
            HyriAPI.get().getRedisProcessor().process(jedis -> {
                for (String lobby : this.lobbies) {
                    final HyggServer server = HyriAPI.get().getServerManager().getServer(lobby);

                    jedis.zadd(LobbyAPI.REDIS_KEY, server.getPlayers().size(), lobby);
                }
            });
        }, 800, 800, TimeUnit.MILLISECONDS);

        scheduler.schedule(this::process, 500, 500, TimeUnit.MILLISECONDS);
    }

    public void onUpdate(HyggServer server) {
        if (server.getType().equals(LobbyAPI.TYPE)) {
            final String serverName = server.getName();

            if (server.getState() == HyggServerState.READY) {
                this.startedLobbies.remove(serverName);

                this.addLobby(serverName);
            } else {
                this.removeLobby(server);
            }
        }
    }

    public void onStop(HyggServer server) {
        if (server.getType().equals(LobbyAPI.TYPE)) {
            this.removeLobby(server);
        }
    }

    private void process() {
        final int lobbiesNumber = this.getLobbiesNumber();
        final int neededLobbies = this.neededLobbies();

        if (lobbiesNumber < neededLobbies) {
            for (int i = neededLobbies - lobbiesNumber; i > 0; i--) {
                this.startLobby();
            }
        } else if (lobbiesNumber > neededLobbies && lobbiesNumber > 1) {
            for (String serverName : this.lobbies) {
                final HyggServer server = HyriAPI.get().getServerManager().getServer(serverName);

                if (server.getState() != HyggServerState.READY) {
                    continue;
                }

                if (server.getPlayers().size() <= LobbyAPI.MIN_PLAYERS && this.lobbies.size() > 1) {
                    final String bestLobby = Hylios.get().getAPI().getLobbyAPI().getBestLobby();

                    if (bestLobby.equals(server.getName())) {
                        return;
                    }

                    HyriAPI.get().getServerManager().evacuateServer(serverName, bestLobby);

                    HyriAPI.get().getScheduler().schedule(() -> {
                        HyriAPI.get().getServerManager().removeServer(serverName, null);
                    }, 5, TimeUnit.SECONDS);
                }
            }
        }
    }

    private void startLobby() {
        final HyggData data = new HyggData();

        data.add(HyggServer.MAP_KEY, Hylios.get().getAPI().getLobbyAPI().getCurrentMap());
        data.add(HyggServer.SUB_TYPE_KEY, LobbyAPI.SUBTYPE);

        final HyggServerRequest request = new HyggServerRequest()
                .withServerType(LobbyAPI.TYPE)
                .withGameType(LobbyAPI.SUBTYPE)
                .withServerData(data);

        HyriAPI.get().getHyggdrasilManager().getHyggdrasilAPI().getServerRequester().createServer(request, server -> this.startedLobbies.add(server.getName()));
    }

    private int neededLobbies() {
        return (int) Math.ceil((double) this.getPlayersOnLobbies() * 1.1 / LobbyAPI.MAX_PLAYERS);
    }

    public void addLobby(String name) {
        if (!this.lobbies.contains(name)) {
            this.lobbies.add(name);
        }
    }

    public void removeLobby(HyggServer server) {
        HyriAPI.get().getRedisProcessor().process(jedis -> jedis.zrem(LobbyAPI.REDIS_KEY, server.getName()));

        this.lobbies.remove(server.getName());
    }

    private int getPlayersOnLobbies() {
        int players = 0;

        for (String serverName : this.lobbies) {
            final HyggServer server = HyriAPI.get().getServerManager().getServer(serverName);

            players += server.getPlayers().size();
        }

        return players;
    }

    private int getLobbiesNumber() {
        return this.lobbies.size() + this.startedLobbies.size();
    }

}
