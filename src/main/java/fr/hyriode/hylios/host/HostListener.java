package fr.hyriode.hylios.host;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.event.HyriEventHandler;
import fr.hyriode.api.host.HostData;
import fr.hyriode.api.player.event.PlayerQuitNetworkEvent;
import fr.hyriode.api.player.event.PlayerQuitServerEvent;
import fr.hyriode.hyggdrasil.api.server.HyggServer;

import java.util.UUID;

/**
 * Created by AstFaster
 * on 29/07/2022 at 20:47
 */
public class HostListener {

    private final HostManager hostManager;

    public HostListener(HostManager hostManager) {
        this.hostManager = hostManager;
    }

    @HyriEventHandler
    public void onQuit(PlayerQuitServerEvent event) {
        final HyggServer server = HyriAPI.get().getServerManager().getServer(event.getServerName());

        if (server == null) {
            return;
        }

        final HostData hostData = HyriAPI.get().getHostManager().getHostData(server);

        if (hostData == null) {
            return;
        }

        final UUID playerId = event.getPlayerId();

        if (hostData.getOwner().equals(playerId)) {
            this.hostManager.removePlayerHost(server);
        }
    }

}
