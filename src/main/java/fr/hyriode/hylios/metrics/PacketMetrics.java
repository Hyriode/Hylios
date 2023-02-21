package fr.hyriode.hylios.metrics;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.event.HyriEventHandler;
import fr.hyriode.api.packet.event.HyriPacketReceiveEvent;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hyreos.api.metrics.HyreosMetric;
import fr.hyriode.hyreos.api.metrics.HyreosMetricsManager;

import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 16/10/2022 at 11:57
 */
public class PacketMetrics {

    private int packetsFlux;

    private final HyreosMetricsManager metricsManager;

    public PacketMetrics() {
        this.metricsManager = Hylios.get().getHyreosAPI().getMetricsManager();

        HyriAPI.get().getEventBus().register(this);
    }

    public void start() {
        HyriAPI.get().getScheduler().schedule(this.process(), 1, 1, TimeUnit.SECONDS);
    }

    private Runnable process() {
        return () -> {
            final HyreosMetric metric = new HyreosMetric("packets-flux");

            metric.addField("value", this.packetsFlux);

            this.metricsManager.writeMetric(metric);

            this.packetsFlux = 0;
        };
    }

    @HyriEventHandler
    public void onPacketReceive(HyriPacketReceiveEvent event) {
        this.packetsFlux++;
    }

}