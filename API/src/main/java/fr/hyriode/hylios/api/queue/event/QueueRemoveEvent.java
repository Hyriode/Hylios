package fr.hyriode.hylios.api.queue.event;

import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueueInfo;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:13
 */
public class QueueRemoveEvent extends QueueGroupEvent {

    public QueueRemoveEvent(QueueEventType type, QueueGroup group, QueueInfo queueInfo) {
        super(type, group, queueInfo);
    }

}
