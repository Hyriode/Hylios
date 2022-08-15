package fr.hyriode.hylios.api.queue.server.event;

import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.event.QueueEventType;
import fr.hyriode.hylios.api.queue.server.SQueueInfo;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:13
 */
public class SQueueRemoveEvent extends SQueueGroupEvent {

    public SQueueRemoveEvent(QueueEventType type, QueueGroup group, SQueueInfo queueInfo) {
        super(type, group, queueInfo);
    }

}
