package fr.hyriode.hylios.api.lobby;

import fr.hyriode.api.HyriAPI;

import java.util.List;

/**
 * Created by AstFaster
 * on 15/07/2022 at 11:35
 */
public class LobbyAPI {

    /** The maximum amount of players on a lobby */
    public static final int MAX_PLAYERS = 75;
    /** The minimum amount of players on a lobby to let it started */
    public static final int MIN_PLAYERS = 5;
    /** The redis key used to balance lobby */
    public static final String REDIS_KEY = "lobby-balancer";
    /** The type of the lobby servers */
    public static final String TYPE = "lobby";
    /** The Redis key of the current lobby map */
    public static final String MAP = "lobby-map";
    /** The subtype of default lobbies */
    public static final String SUBTYPE = "default";
    /** The default map name of the lobby */
    public static final String DEFAULT_MAP = "normal";

    /**
     * Get the best lobby on the network
     *
     * @return A server name
     */
    public String getBestLobby() {
        return HyriAPI.get().getRedisProcessor().get(jedis -> {
            final List<String> lobbies = jedis.zrange(REDIS_KEY, 0, -1);

            if (lobbies != null && lobbies.size() > 0) {
                return lobbies.get(0);
            }
            return null;
        });
    }

    /**
     * Get the current map of the lobby
     *
     * @return A map name
     */
    public String getCurrentMap() {
        return HyriAPI.get().getRedisProcessor().get(jedis -> {
            final String map = jedis.get(MAP);

            if (map != null) {
                return map;
            }
            return DEFAULT_MAP;
        });
    }

    /**
     * Set the current map
     *
     * @param map The new current map
     */
    public void setCurrentMap(String map) {
        HyriAPI.get().getRedisProcessor().process(jedis -> jedis.set(MAP, map));
    }

}
