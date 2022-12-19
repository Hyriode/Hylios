package fr.hyriode.hylios.host;

import fr.hyriode.api.host.HostRequest;
import fr.hyriode.api.packet.HyriPacket;
import fr.hyriode.api.packet.IHyriPacketReceiver;

/**
 * Created by AstFaster
 * on 29/07/2022 at 20:16
 */
public class HostReceiver implements IHyriPacketReceiver {

    private final HostManager hostManager;

    public HostReceiver(HostManager hostManager) {
        this.hostManager = hostManager;
    }

    @Override
    public void receive(String channel, HyriPacket packet) {
        if (packet instanceof final HostRequest request) {
            this.hostManager.createHost(request);
        }
    }

}
