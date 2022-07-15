package fr.hyriode.hylios.api.queue.packet.group;

import fr.hyriode.api.packet.HyriPacket;
import fr.hyriode.hylios.api.queue.QueueGroup;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:04
 */
public class QueueUpdateGroupPacket extends HyriPacket {

    private final QueueGroup group;

    public QueueUpdateGroupPacket(QueueGroup group) {
        this.group = group;
    }

    public QueueGroup getGroup() {
        return this.group;
    }

}
