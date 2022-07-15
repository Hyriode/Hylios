package fr.hyriode.hylios.api.queue.packet;

import fr.hyriode.api.packet.HyriPacket;
import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueueInfo;
import fr.hyriode.hylios.api.queue.QueuePlayer;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:08
 */
public class QueueInfoPacket extends HyriPacket {

    private final QueuePlayer player;
    private final QueueGroup group;
    private final QueueInfo queueInfo;

    private final int place;

    public QueueInfoPacket(QueuePlayer player, QueueGroup group, QueueInfo queueInfo, int place) {
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

    public QueueInfo getQueueInfo() {
        return this.queueInfo;
    }

    public int getPlace() {
        return this.place;
    }

}
