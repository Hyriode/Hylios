package fr.hyriode.hylios.host;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.api.server.IHyriServerManager;
import fr.hyriode.hyggdrasil.api.event.HyggEventBus;
import fr.hyriode.hyggdrasil.api.event.model.server.HyggServerUpdatedEvent;
import fr.hyriode.hyggdrasil.api.protocol.environment.HyggData;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerOptions;
import fr.hyriode.hyggdrasil.api.server.HyggServerRequest;
import fr.hyriode.hyggdrasil.api.server.HyggServerState;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.api.host.HostAPI;
import fr.hyriode.hylios.api.host.HostData;
import fr.hyriode.hylios.api.host.HostRequest;
import fr.hyriode.hylios.api.host.event.HostCreatedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by AstFaster
 * on 29/07/2022 at 19:55
 */
public class HostManager {

    private final List<String> startedServers;

    private final HostAPI api;

    public HostManager() {
        this.api = Hylios.get().getAPI().getHostAPI();
        this.startedServers = new ArrayList<>();

        final HyggEventBus eventBus = HyriAPI.get().getHyggdrasilManager().getHyggdrasilAPI().getEventBus();

        eventBus.subscribe(HyggServerUpdatedEvent.class, event -> this.onServerUpdated(event.getServer()));

        HyriAPI.get().getPubSub().subscribe(HostAPI.CHANNEL, new HostReceiver(this));
        HyriAPI.get().getNetworkManager().getEventBus().register(new HostListener(this));
    }

    private void onServerUpdated(HyggServer server) {
        final String serverName = server.getName();

        if (server.getState() == HyggServerState.READY && this.startedServers.remove(serverName)) {
            final HostData hostData = this.api.getHostData(server);

            if (hostData == null) {
                return;
            }

            final UUID owner = hostData.getOwner();
            final IHyriPlayer ownerAccount = IHyriPlayer.get(owner);

            if (!ownerAccount.isOnline()) {
                HyriAPI.get().getServerManager().removeServer(serverName, null);
                return;
            }

            HyriAPI.get().getNetworkManager().getEventBus().publish(new HostCreatedEvent(server, hostData));
            HyriAPI.get().getServerManager().sendPlayerToServer(owner, serverName);
        }
    }

    public void createHost(HostRequest request) {
        final String game = request.getGame();
        final String gameType = request.getGameType();
        final HyggData serverData = new HyggData();

        serverData.add(HostAPI.DATA_KEY, HyriAPI.GSON.toJson(new HostData(request.getHostType(), request.getOwner(), game, gameType, "Host " + HyriAPI.get().getGameManager().getGameInfo(game).getDisplayName())));

        final HyggServerRequest serverRequest = new HyggServerRequest()
                .withServerData(serverData)
                .withServerOptions(new HyggServerOptions())
                .withServerType(game)
                .withGameType(gameType);

        HyriAPI.get().getServerManager().createServer(serverRequest, server -> this.startedServers.add(server.getName()));
    }

    public void removePlayerHost(UUID playerId) {
        final HyggServer server = this.getPlayerHost(playerId);
        final Runnable removeTask = () -> {
            if (server == null) {
                return;
            }

            final String serverName = server.getName();
            final IHyriServerManager serverManager = HyriAPI.get().getServerManager();

            serverManager.evacuateServer(serverName, serverManager.getLobby().getName());
            serverManager.removeServer(serverName, null);
        };

        if (server == null) {
            return;
        }

        final HyggServerState state = server.getState();

        if (state == HyggServerState.STARTING || state == HyggServerState.CREATING) {
            removeTask.run();
            return;
        }

        if (!server.getPlayers().contains(playerId)) {
            return;
        }

        if (state == HyggServerState.READY) {
            removeTask.run();
        }
    }

    private HyggServer getPlayerHost(UUID playerId) {
        for (HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            final HostData hostData = this.api.getHostData(server);

            if (hostData == null) {
                continue;
            }

            if (hostData.getOwner().equals(playerId)) {
                return server;
            }
        }
        return null;
    }

}
