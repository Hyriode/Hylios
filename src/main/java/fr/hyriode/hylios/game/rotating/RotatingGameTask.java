package fr.hyriode.hylios.game.rotating;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.game.rotating.IHyriRotatingGame;
import fr.hyriode.api.game.rotating.IHyriRotatingGameManager;
import fr.hyriode.api.server.IHyriServerManager;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerState;

import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 27/07/2022 at 18:46
 */
public class RotatingGameTask {

    private static final int MAX_GAME_TIME = 3600 * 24 * 14 * 1000;

    public void start() {
        HyriAPI.get().getScheduler().schedule(this::process, 0, 1, TimeUnit.MINUTES);
    }

    public void process() {
        final IHyriRotatingGameManager gameManager = HyriAPI.get().getGameManager().getRotatingGameManager();
        final IHyriRotatingGame game = gameManager.getRotatingGame();

        if (game == null) {
            return;
        }

        if (System.currentTimeMillis() - game.sinceWhen() >= MAX_GAME_TIME) {
            gameManager.switchToNextRotatingGame();

            final IHyriServerManager serverManager = HyriAPI.get().getServerManager();

            for (HyggServer server : serverManager.getServers(game.getInfo().getName())) {
                final HyggServerState state = server.getState();

                if (state == HyggServerState.PLAYING || state == HyggServerState.SHUTDOWN) {
                    continue;
                }

                final String serverName = server.getName();
                final HyggServer bestLobby = HyriAPI.get().getServerManager().getLobby();

                if (bestLobby != null) {
                    serverManager.evacuateServer(serverName, bestLobby.getName());
                }

                serverManager.removeServer(serverName, null);
            }
        }
    }

}
