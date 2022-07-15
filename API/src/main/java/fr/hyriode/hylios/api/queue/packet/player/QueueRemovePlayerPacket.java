package fr.hyriode.hylios.api.queue.packet.player;

import fr.hyriode.api.packet.HyriPacket;

import java.util.UUID;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:06
 */
public class QueueRemovePlayerPacket extends HyriPacket {

    private final UUID playerId;

    public QueueRemovePlayerPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

}
