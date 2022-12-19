package fr.hyriode.hylios.game.rotating;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.game.rotating.IHyriRotatingGame;
import fr.hyriode.api.game.rotating.IHyriRotatingGameManager;
import fr.hyriode.api.server.IHyriServerManager;
import fr.hyriode.hyggdrasil.api.server.HyggServer;

import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 27/07/2022 at 18:46
 */
public class RotatingGameTask {

    private static final int MAX_GAME_TIME = 3600 * 24 * 7 * 1000;

    public void start() {
        final IHyriRotatingGameManager gameManager = HyriAPI.get().getGameManager().getRotatingGameManager();
        final IHyriRotatingGame game = gameManager.getRotatingGame();

        if (game == null) {
            return;
        }

        HyriAPI.get().getScheduler().schedule(this::process, MAX_GAME_TIME - (System.currentTimeMillis() - game.sinceWhen()), TimeUnit.MILLISECONDS);
    }

    public void process() {
        final IHyriRotatingGameManager gameManager = HyriAPI.get().getGameManager().getRotatingGameManager();
        final IHyriRotatingGame game = gameManager.getRotatingGame();

        if (game == null) {
            return;
        }

        gameManager.switchToNextRotatingGame();

        final IHyriServerManager serverManager = HyriAPI.get().getServerManager();

        for (HyggServer server : serverManager.getServers(game.getInfo().getName())) {
            final HyggServer.State state = server.getState();

            if (state == HyggServer.State.PLAYING || state == HyggServer.State.SHUTDOWN) {
                continue;
            }

            final String serverName = server.getName();
            final HyggServer bestLobby = HyriAPI.get().getLobbyAPI().getBestLobby();

            if (bestLobby != null) {
                HyriAPI.get().getLobbyAPI().evacuateToLobby(serverName);
            }

            serverManager.removeServer(serverName, null);
        }

        this.start();
    }

}
