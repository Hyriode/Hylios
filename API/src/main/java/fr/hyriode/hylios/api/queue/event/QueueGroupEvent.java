package fr.hyriode.hylios.api.queue.event;

import fr.hyriode.api.event.HyriEvent;
import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueueInfo;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:16
 */
public abstract class QueueGroupEvent extends HyriEvent {

    private final Type type;
    private final QueueGroup group;
    private final QueueInfo queueInfo;

    public QueueGroupEvent(Type type, QueueGroup group, QueueInfo queueInfo) {
        this.type = type;
        this.group = group;
        this.queueInfo = queueInfo;
    }

    public Type getType() {
        return this.type;
    }

    public QueueGroup getGroup() {
        return this.group;
    }

    public QueueInfo getQueueInfo() {
        return this.queueInfo;
    }

    public enum Type {
        OK,
        NOT_IN_QUEUE,
        ALREADY_IN,
        UNKNOWN
    }

}
