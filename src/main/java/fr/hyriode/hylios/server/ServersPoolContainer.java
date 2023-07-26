package fr.hyriode.hylios.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AstFaster
 * on 05/05/2023 at 12:36
 */
public class ServersPoolContainer {

    private final List<ServersPool> pools = new ArrayList<>();

    public void disable() {
        for (ServersPool pool : this.pools) {
            pool.stopProcess();
        }

        this.pools.clear();
    }

    public ServersPool getPool(String game, String gameType) {
        for (ServersPool pool : this.pools) {
            if (pool.getGame().equals(game) && pool.getGameType().equals(gameType)) {
                return pool;
            }
        }

        final ServersPool newPool = new ServersPool(game, gameType);

        newPool.initProcess();

        this.pools.add(newPool);

        return newPool;
    }

}
