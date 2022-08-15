package fr.hyriode.hylios.api.host.packet;

import fr.hyriode.api.packet.HyriPacket;
import fr.hyriode.hylios.api.host.HostRequest;

/**
 * Created by AstFaster
 * on 29/07/2022 at 20:13
 */
public class HostCreatePacket extends HyriPacket {

    private final HostRequest request;

    public HostCreatePacket(HostRequest request) {
        this.request = request;
    }

    public HostRequest getRequest() {
        return this.request;
    }

}
