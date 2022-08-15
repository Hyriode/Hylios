package fr.hyriode.hylios.api.queue.server.packet;

import fr.hyriode.api.packet.HyriPacket;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:01
 */
public abstract class SQueueAddPacket extends HyriPacket {

    protected final String serverName;

    public SQueueAddPacket(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return this.serverName;
    }

}
