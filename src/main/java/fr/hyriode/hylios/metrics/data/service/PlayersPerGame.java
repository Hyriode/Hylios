package fr.hyriode.hylios.metrics.data.service;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;

import java.time.Instant;

@Measurement(name = "players_per_game")
public class PlayersPerGame implements IHyreosMetric {

    @Column(name = "game_name", tag = true)
    private final String game;
    @Column(name = "game_type", tag = true)
    private final String type;
    @Column(name = "players")
    private final int players;
    @Column(timestamp = true)
    private final Instant time;

    public PlayersPerGame(String game, String type, int players) {
        this.game = game;
        this.type = type;
        this.players = players;

        this.time = Instant.now();
    }
}
