package fr.hyriode.hylios.host;

import fr.hyriode.api.packet.HyriPacket;
import fr.hyriode.api.packet.IHyriPacketReceiver;
import fr.hyriode.hylios.api.host.packet.HostCreatePacket;

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
        if (packet instanceof final HostCreatePacket hostPacket) {
            this.hostManager.createHost(hostPacket.getRequest());
        }
    }

}
