package fr.hyriode.hylios.metrics.data.service;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hylios.metrics.data.service.ServiceType;

import java.time.Instant;

@Measurement(name = "players_per_service")
public class PlayersPerService implements IHyreosMetric {

    @Column(name = "service_type", tag = true)
    private final ServiceType type;
    @Column(name = "service_name", tag = true)
    private final String name;
    @Column(name = "players")
    private final int players;
    @Column(timestamp = true)
    private final Instant time;

    public PlayersPerService(ServiceType type, String name, int players) {
        this.type = type;
        this.name = name;
        this.players = players;

        this.time = Instant.now();
    }
}
