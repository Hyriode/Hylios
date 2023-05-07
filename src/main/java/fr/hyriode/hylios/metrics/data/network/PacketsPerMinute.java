package fr.hyriode.hylios.metrics.data.network;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;

import java.time.Instant;

@Measurement(name = "packets_per_minute")
public class PacketsPerMinute implements IHyreosMetric {

    @Column(name = "app_type", tag = true)
    private final AppType type;
    @Column(name = "packets")
    private final long packets;
    @Column(timestamp = true)
    private final Instant time;

    public PacketsPerMinute(AppType type, long packets) {
        this.type = type;
        this.packets = packets;

        this.time = Instant.now();
    }
}
