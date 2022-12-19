package fr.hyriode.hylios.host;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.host.HostData;
import fr.hyriode.api.host.HostRequest;
import fr.hyriode.api.host.IHostManager;
import fr.hyriode.hyggdrasil.api.event.HyggEventBus;
import fr.hyriode.hyggdrasil.api.event.model.server.HyggServerUpdatedEvent;
import fr.hyriode.hyggdrasil.api.protocol.data.HyggData;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerCreationInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by AstFaster
 * on 29/07/2022 at 19:55
 */
public class HostManager {

    private final List<String> startedServers;

    private final IHostManager api;

    public HostManager() {
        this.api = HyriAPI.get().getHostManager();
        this.startedServers = new ArrayList<>();

        final HyggEventBus eventBus = HyriAPI.get().getHyggdrasilManager().getHyggdrasilAPI().getEventBus();

        eventBus.subscribe(HyggServerUpdatedEvent.class, event -> this.onServerUpdated(event.getServer()));

        HyriAPI.get().getPubSub().subscribe(IHostManager.CHANNEL, new HostReceiver(this));
        HyriAPI.get().getNetworkManager().getEventBus().register(new HostListener(this));
    }

    private void onServerUpdated(HyggServer server) {
        final String serverName = server.getName();

        if (server.getState() == HyggServer.State.READY && this.startedServers.remove(serverName)) {
            final HostData hostData = this.api.getHostData(server);

            if (hostData == null) {
                return;
            }

            final UUID owner = hostData.getOwner();

            if (!HyriAPI.get().getPlayerManager().isOnline(owner)) {
                HyriAPI.get().getServerManager().removeServer(serverName, null);
                return;
            }

            HyriAPI.get().getServerManager().sendPlayerToServer(owner, serverName);
        }
    }

    public void createHost(HostRequest request) {
        final String game = request.getGame();
        final String gameType = request.getGameType();
        final HyggData serverData = new HyggData();

        serverData.add(IHostManager.DATA_KEY, HyriAPI.GSON.toJson(new HostData(request.getHostType(), request.getOwner(), "Host " + HyriAPI.get().getGameManager().getGameInfo(game).getDisplayName())));

        final HyggServerCreationInfo serverRequest = new HyggServerCreationInfo(game)
                .withType(game)
                .withGameType(gameType)
                .withAccessibility(HyggServer.Accessibility.HOST)
                .withProcess(HyggServer.Process.TEMPORARY)
                .withData(serverData);

        HyriAPI.get().getServerManager().createServer(serverRequest, server -> this.startedServers.add(server.getName()));
    }

    public void removePlayerHost(UUID playerId) {
        final HyggServer server = this.getPlayerHost(playerId);

        if (server == null) {
            return;
        }

        final HyggServer.State state = server.getState();

        if (state == HyggServer.State.CREATING || state == HyggServer.State.STARTING || state == HyggServer.State.READY) {
            final String serverName = server.getName();

            HyriAPI.get().getLobbyAPI().evacuateToLobby(serverName);
            HyriAPI.get().getServerManager().removeServer(serverName, null);
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
