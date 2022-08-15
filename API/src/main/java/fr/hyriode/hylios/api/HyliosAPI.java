package fr.hyriode.hylios.api;

import fr.hyriode.hylios.api.host.HostAPI;
import fr.hyriode.hylios.api.lobby.LobbyAPI;
import fr.hyriode.hylios.api.queue.QueueAPI;

/**
 * Created by AstFaster
 * on 06/07/2022 at 21:16
 */
public class HyliosAPI {

    /** The name of the API */
    public static final String NAME = "HyliosAPI";

    /** The API used for lobbies (best lobby, map, types, etc) */
    private final LobbyAPI lobbyAPI;
    /** The API used to interact with queue system */
    private final QueueAPI queueAPI;
    /** The API used to intreact with host system (create one, remove one, etc) */
    private final HostAPI hostAPI;

    /**
     * Constructor used to create an instance of {@link HyliosAPI}
     */
    public HyliosAPI() {
        this.lobbyAPI = new LobbyAPI();
        this.queueAPI = new QueueAPI();
        this.hostAPI = new HostAPI();
    }

    /**
     * Get the lobby API instance
     *
     * @return The {@link LobbyAPI} instance
     */
    public LobbyAPI getLobbyAPI() {
        return this.lobbyAPI;
    }

    /**
     * Get the queue API instance
     *
     * @return The {@link QueueAPI} instance
     */
    public QueueAPI getQueueAPI() {
        return this.queueAPI;
    }

    /**
     * Get the host API instance
     *
     * @return The {@link HostAPI} instance
     */
    public HostAPI getHostAPI() {
        return this.hostAPI;
    }

}
