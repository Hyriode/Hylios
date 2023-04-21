package fr.hyriode.hylios.game.rotating;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.game.IHyriGameInfo;
import fr.hyriode.api.game.IHyriGameType;
import fr.hyriode.api.game.rotating.IHyriRotatingGame;
import fr.hyriode.api.game.rotating.IHyriRotatingGameManager;
import fr.hyriode.api.leveling.NetworkLeveling;
import fr.hyriode.api.server.IHyriServerManager;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hylios.Hylios;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 27/07/2022 at 18:46
 */
public class RotatingGameTask {

    public void start() {
        final IHyriRotatingGameManager gameManager = HyriAPI.get().getGameManager().getRotatingGameManager();
        final IHyriRotatingGame game = gameManager.getRotatingGame();

        if (game == null) {
            return;
        }

        final Calendar calendar = Calendar.getInstance();

        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        calendar.set(Calendar.DAY_OF_WEEK, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.set(Calendar.WEEK_OF_MONTH, calendar.get(Calendar.WEEK_OF_MONTH) + 1);
        }

        HyriAPI.get().getScheduler().schedule(this::process, calendar.getTimeInMillis() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public void process() {
        final IHyriRotatingGameManager gameManager = HyriAPI.get().getGameManager().getRotatingGameManager();
        final IHyriRotatingGame game = gameManager.getRotatingGame();

        if (game == null) {
            return;
        }

        System.out.println("Switching to next rotating game...");

        gameManager.switchToNextRotatingGame();

        final IHyriGameInfo gameInfo = game.getInfo();
        final IHyriServerManager serverManager = HyriAPI.get().getServerManager();

        // Remove started servers (except playing ones)
        for (HyggServer server : serverManager.getServers(gameInfo.getName())) {
            final HyggServer.State state = server.getState();

            if (state == HyggServer.State.PLAYING || state == HyggServer.State.SHUTDOWN) {
                continue;
            }

            final String serverName = server.getName();
            final HyggServer bestLobby = HyriAPI.get().getLobbyAPI().getBestLobby();

            if (bestLobby != null) {
                HyriAPI.get().getLobbyAPI().evacuateToLobby(serverName);
            }

            HyriAPI.get().getScheduler().schedule(() -> serverManager.removeServer(serverName, null), 5, TimeUnit.SECONDS);
        }

        // Disable active queues
        for (IHyriGameType gameType : gameInfo.getTypes()) {
            Hylios.get().getQueueManager().getGameQueue(gameInfo.getName(), gameType.getName(), null).disable();
        }

        // Reset leaderboards
        HyriAPI.get().getLeaderboardProvider().getLeaderboard(NetworkLeveling.LEADERBOARD_TYPE, "rotating-game-experience").clear();

        this.start();
    }

}
