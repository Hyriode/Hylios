package fr.hyriode.hylios.host;

import fr.hyriode.api.event.HyriEventHandler;
import fr.hyriode.api.player.event.PlayerQuitServerEvent;

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
        this.hostManager.removePlayerHost(event.getPlayerId());
    }

}
