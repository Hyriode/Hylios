package fr.hyriode.hylios.api.queue.packet.player;

import fr.hyriode.hylios.api.queue.QueuePlayer;
import fr.hyriode.hylios.api.queue.packet.QueueAddPacket;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:05
 */
public class QueueAddPlayerPacket extends QueueAddPacket {

    private final QueuePlayer player;

    public QueueAddPlayerPacket(QueuePlayer player, String game, String gameType, String map) {
        super(game, gameType, map);
        this.player = player;
    }

    public QueuePlayer getPlayer() {
        return this.player;
    }

}
