package fr.hyriode.hylios.host;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.host.HostData;
import fr.hyriode.api.host.HostRequest;
import fr.hyriode.api.host.IHostManager;
import fr.hyriode.api.player.IHyriPlayerSession;
import fr.hyriode.hyggdrasil.api.protocol.data.HyggData;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerCreationInfo;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.server.ServersPool;
import fr.hyriode.hylios.server.template.Template;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 29/07/2022 at 19:55
 */
public class HostManager {

    public HostManager() {
        HyriAPI.get().getPubSub().subscribe(IHostManager.CHANNEL, new HostReceiver(this));
        HyriAPI.get().getNetworkManager().getEventBus().register(new HostListener(this));
    }

    public void createHost(HostRequest request) {
        final String game = request.getGame();
        final String gameType = request.getGameType();
        final HyggData serverData = new HyggData();
        final HostData hostData = new HostData(request.getHostType(), request.getOwner(), "Host " + HyriAPI.get().getGameManager().getGameInfo(game).getDisplayName());

        serverData.add(IHostManager.DATA_KEY, HyriAPI.GSON.toJson(hostData));

        final Template.Resources resources = Hylios.get().getTemplateManager().getTemplate(game).getMode(gameType).getHostResources();
        final HyggServerCreationInfo serverInfo = new HyggServerCreationInfo(game)
                .withAccessibility(HyggServer.Accessibility.HOST)
                .withGameType(gameType)
                .withData(serverData)
                .withMaxMemory(resources.getMaxMemory())
                .withMinMemory(resources.getMinMemory())
                .withCpus(resources.getCpus());

        System.out.println("Starting host for '" + request.getOwner() + "'...");

        Hylios.get().getServersStarter().startServer(serverInfo, server -> {
            final UUID owner = hostData.getOwner();
            final IHyriPlayerSession session = IHyriPlayerSession.get(owner);

            if (session == null || session.isModerating()) {
                HyriAPI.get().getServerManager().removeServer(server.getName(), null);
                return;
            }

            HyriAPI.get().getServerManager().sendPlayerToServer(owner, server.getName());
        });
    }

    public void removePlayerHost(HyggServer server) {
        final HyggServer.State state = server.getState();

        if (state == HyggServer.State.CREATING || state == HyggServer.State.STARTING || state == HyggServer.State.READY) {
            final String serverName = server.getName();

            HyriAPI.get().getScheduler().schedule(() ->  HyriAPI.get().getLobbyAPI().evacuateToLobby(serverName), 1, TimeUnit.SECONDS);
            HyriAPI.get().getScheduler().schedule(() -> HyriAPI.get().getServerManager().removeServer(serverName, null), 5, TimeUnit.SECONDS);
        }
    }

}
