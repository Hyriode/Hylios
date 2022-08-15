package fr.hyriode.hylios.api.queue.server;

/**
 * Created by AstFaster
 * on 10/05/2022 at 16:32
 */
public class SQueueInfo {

    private final String serverName;

    private int totalGroups;
    private int totalPlayers;

    public SQueueInfo(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return this.serverName;
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
