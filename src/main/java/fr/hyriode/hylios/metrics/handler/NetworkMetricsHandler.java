package fr.hyriode.hylios.metrics.handler;

import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.api.MetricsRedisKey;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hylios.metrics.data.network.AppType;
import fr.hyriode.hylios.metrics.data.network.PacketsPerMinute;
import fr.hyriode.hylios.metrics.processor.IMetricHandler;
import fr.hyriode.hylios.metrics.processor.IMetricProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkMetricsHandler implements IMetricHandler {

    private static final IMetricProcessor HYRIAPI = () -> {
        final long packets = IMetricHandler.fetch(MetricsRedisKey.HYRIAPI_PACKETS);
        IMetricHandler.update(MetricsRedisKey.HYRIAPI_PACKETS, 0);

        return new PacketsPerMinute(AppType.HYRIAPI, packets);
    };
    private static final IMetricProcessor HYGGDRASIL = () -> {
        final long packets = IMetricHandler.fetch(MetricsRedisKey.HYGGDRASIL_PACKETS);
        IMetricHandler.update(MetricsRedisKey.HYGGDRASIL_PACKETS, 0);

        return new PacketsPerMinute(AppType.HYGGDRASIL, packets);
    };

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void initialize(List<IHyriPlayer> players) {
        Hylios.get().getLogger().info("Initializing network metrics...");

        IMetricHandler.update(MetricsRedisKey.HYRIAPI_PACKETS, 0);
        IMetricHandler.update(MetricsRedisKey.HYGGDRASIL_PACKETS, 0);
    }

    @Override
    public Set<IHyreosMetric> process() {
        final Set<IHyreosMetric> metrics = new HashSet<>();
        metrics.add(HYRIAPI.process());
        metrics.add(HYGGDRASIL.process());

        return metrics;
    }
}
