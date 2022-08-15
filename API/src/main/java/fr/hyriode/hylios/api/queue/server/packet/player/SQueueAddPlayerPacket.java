package fr.hyriode.hylios.api.queue.server.packet.player;

import fr.hyriode.hylios.api.queue.QueuePlayer;
import fr.hyriode.hylios.api.queue.server.packet.SQueueAddPacket;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:05
 */
public class SQueueAddPlayerPacket extends SQueueAddPacket {

    private final QueuePlayer player;

    public SQueueAddPlayerPacket(String serverName, QueuePlayer player) {
        super(serverName);
        this.player = player;
    }

    public QueuePlayer getPlayer() {
        return this.player;
    }

}
