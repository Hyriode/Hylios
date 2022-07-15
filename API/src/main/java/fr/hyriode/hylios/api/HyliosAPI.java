package fr.hyriode.hylios.api;

import fr.hyriode.hylios.api.lobby.LobbyAPI;

/**
 * Created by AstFaster
 * on 06/07/2022 at 21:16
 */
public class HyliosAPI {

    /** The name of the API */
    public static final String NAME = "HyliosAPI";

    /** The API used for lobbies (best lobby, map, types, etc) */
    private final LobbyAPI lobbyAPI;

    /**
     * Constructor used to create an instance of {@link HyliosAPI}
     */
    public HyliosAPI() {
        this.lobbyAPI = new LobbyAPI();
    }

    /**
     * Get the lobby API instance
     *
     * @return The {@link LobbyAPI} instance
     */
    public LobbyAPI getLobbyAPI() {
        return this.lobbyAPI;
    }

}
