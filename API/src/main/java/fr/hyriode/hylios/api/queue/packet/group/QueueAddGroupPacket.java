package fr.hyriode.hylios.api.queue.packet.group;

import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.packet.QueueAddPacket;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:02
 */
public class QueueAddGroupPacket extends QueueAddPacket {

    private final QueueGroup group;

    public QueueAddGroupPacket(String game, String gameType, String map, QueueGroup group) {
        super(game, gameType, map);
        this.group = group;
    }

    public QueueGroup getGroup() {
        return this.group;
    }

}
