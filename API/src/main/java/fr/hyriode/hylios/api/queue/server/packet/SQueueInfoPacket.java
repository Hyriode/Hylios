package fr.hyriode.hylios.api.queue.server.packet;

import fr.hyriode.api.packet.HyriPacket;
import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueuePlayer;
import fr.hyriode.hylios.api.queue.server.SQueueInfo;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:08
 */
public class SQueueInfoPacket extends HyriPacket {

    private final QueuePlayer player;
    private final QueueGroup group;
    private final SQueueInfo queueInfo;

    private final int place;

    public SQueueInfoPacket(QueuePlayer player, QueueGroup group, SQueueInfo queueInfo, int place) {
        this.player = player;
        this.group = group;
        this.queueInfo = queueInfo;
        this.place = place;
    }

    public QueuePlayer getPlayer() {
        return this.player;
    }

    public QueueGroup getGroup() {
        return this.group;
    }

    public SQueueInfo getQueueInfo() {
        return this.queueInfo;
    }

    public int getPlace() {
        return this.place;
    }

}
