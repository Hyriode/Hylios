package fr.hyriode.hylios.api.queue.server.packet.group;

import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.server.packet.SQueueAddPacket;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:02
 */
public class SQueueAddGroupPacket extends SQueueAddPacket {

    private final QueueGroup group;

    public SQueueAddGroupPacket(String serverName, QueueGroup group) {
        super(serverName);
        this.group = group;
    }

    public QueueGroup getGroup() {
        return this.group;
    }

}
