package fr.hyriode.hylios.queue;

import fr.hyriode.api.packet.HyriPacket;
import fr.hyriode.api.packet.IHyriPacketReceiver;
import fr.hyriode.api.queue.packet.JoinQueuePacket;
import fr.hyriode.api.queue.packet.LeaveQueuePacket;

/**
 * Created by AstFaster
 * on 16/04/2022 at 09:08
 */
public class QueueReceiver implements IHyriPacketReceiver {

    private final QueueManager queueManager;

    public QueueReceiver(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public void receive(String channel, HyriPacket packet) {
        if (packet instanceof final JoinQueuePacket queuePacket) {
            this.queueManager.onJoin(queuePacket);
        } else if (packet instanceof final LeaveQueuePacket queuePacket) {
            this.queueManager.onLeave(queuePacket);
        }
    }
    
}
