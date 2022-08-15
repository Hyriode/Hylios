package fr.hyriode.hylios.api.queue.server.event;

import fr.hyriode.api.event.HyriEvent;
import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.event.QueueEventType;
import fr.hyriode.hylios.api.queue.server.SQueueInfo;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:16
 */
public abstract class SQueueGroupEvent extends HyriEvent {

    private final QueueEventType type;
    private final QueueGroup group;
    private final SQueueInfo queueInfo;

    public SQueueGroupEvent(QueueEventType type, QueueGroup group, SQueueInfo queueInfo) {
        this.type = type;
        this.group = group;
        this.queueInfo = queueInfo;
    }

    public QueueEventType getType() {
        return this.type;
    }

    public QueueGroup getGroup() {
        return this.group;
    }

    public SQueueInfo getQueueInfo() {
        return this.queueInfo;
    }

}
