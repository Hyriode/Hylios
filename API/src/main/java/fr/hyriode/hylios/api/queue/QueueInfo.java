package fr.hyriode.hylios.api.queue;

/**
 * Created by AstFaster
 * on 10/05/2022 at 16:32
 */
public class QueueInfo {

    private final String game;
    private final String gameType;
    private final String map;

    private int totalGroups;
    private int totalPlayers;

    public QueueInfo(String game, String gameType, String map, int totalGroups, int totalPlayers) {
        this.game = game;
        this.gameType = gameType;
        this.map = map;
        this.totalGroups = totalGroups;
        this.totalPlayers = totalPlayers;
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

    public int getTotalGroups() {
        return this.totalGroups;
    }

    public void setTotalGroups(int totalGroups) {
        this.totalGroups = totalGroups;
    }

    public int getTotalPlayers() {
        return this.totalPlayers;
    }

    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }

}
