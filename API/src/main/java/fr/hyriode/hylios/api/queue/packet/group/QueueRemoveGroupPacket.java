package fr.hyriode.hylios.api.queue.packet.group;

import fr.hyriode.api.packet.HyriPacket;

import java.util.UUID;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:04
 */
public class QueueRemoveGroupPacket extends HyriPacket {

    private final UUID groupId;

    public QueueRemoveGroupPacket(UUID groupId) {
        this.groupId = groupId;
    }

    public UUID getGroupId() {
        return this.groupId;
    }

}
