package fr.hyriode.hylios.api.queue.event;

import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueueInfo;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:11
 */
public class QueueAddEvent extends QueueGroupEvent {

    public QueueAddEvent(IType type, QueueGroup group, QueueInfo queueInfo) {
        super(type, group, queueInfo);
    }

}
