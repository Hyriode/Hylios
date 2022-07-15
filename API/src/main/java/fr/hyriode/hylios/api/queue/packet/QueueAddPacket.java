package fr.hyriode.hylios.api.queue.packet;

import fr.hyriode.api.packet.HyriPacket;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:01
 */
public abstract class QueueAddPacket extends HyriPacket {

    protected final String game;
    protected final String gameType;
    protected final String map;

    public QueueAddPacket(String game, String gameType, String map) {
        this.game = game;
        this.gameType = gameType;
        this.map = map;
    }

    public String getGame() {
        return this.game;
    }

    public String getGameType() {
        return this.gameType;
    }

    public String getMap() {
        return this.map;
    }

}
