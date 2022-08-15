package fr.hyriode.hylios.api.host.event;

import fr.hyriode.api.event.HyriEvent;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hylios.api.host.HostData;

/**
 * Created by AstFaster
 * on 29/07/2022 at 20:15
 */
public class HostCreatedEvent extends HyriEvent {

    private final HyggServer server;
    private final HostData hostData;

    public HostCreatedEvent(HyggServer server, HostData hostData) {
        this.server = server;
        this.hostData = hostData;
    }

    public HyggServer getServer() {
        return this.server;
    }

    public HostData getHostData() {
        return this.hostData;
    }

}
