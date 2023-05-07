package fr.hyriode.hylios.metrics;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.api.HyliosMetricsRedisKey;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hylios.metrics.handler.MoneyMetricsHandler;
import fr.hyriode.hylios.metrics.handler.PlayersMetricsHandler;
import fr.hyriode.hylios.metrics.handler.ServiceMetricsHandler;
import fr.hyriode.hylios.metrics.processor.IMetricHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MetricsManager {

    private final Set<IMetricHandler> metrics = new HashSet<>();

    public void initialize() {
        for (final HyliosMetricsRedisKey key : HyliosMetricsRedisKey.values()) {
            if (this.exists(key)) {
                return;
            }
        }

        Hylios.get().getLogger().info("Metrics keys not found, creating them...");

        long hyris = 0L;
        long hyodes = 0L;
        long hyriplus = 0L;
        long registered = 0L;
        for (final IHyriPlayer player : HyriAPI.get().getPlayerManager().getPlayers()) {
            hyris += player.getHyris().getAmount();
            hyodes += player.getHyodes().getAmount();

            if (player.getHyriPlus().has()) {
                hyriplus++;
            }

            registered++;
        }

        this.update(HyliosMetricsRedisKey.HYRIS, hyris);
        this.update(HyliosMetricsRedisKey.HYODES, hyodes);
        this.update(HyliosMetricsRedisKey.HYRI_PLUS, hyriplus);
        this.update(HyliosMetricsRedisKey.REGISTERED_PLAYERS, registered);
    }

    public void start() {
        this.metrics.add(new MoneyMetricsHandler());
        this.metrics.add(new PlayersMetricsHandler());
        this.metrics.add(new ServiceMetricsHandler());

        HyriAPI.get().getScheduler().schedule(this::process, 60, 60, TimeUnit.SECONDS);
    }

    public void process() {
        for (final IMetricHandler metric : this.metrics) {
            metric.process().forEach(this::save);
        }
    }

    private boolean exists(HyliosMetricsRedisKey key) {
        return HyriAPI.get().getRedisProcessor().get(jedis -> jedis.exists(key.getKey()));
    }

    private void update(HyliosMetricsRedisKey key, long value) {
        HyriAPI.get().getRedisProcessor().processAsync(jedis -> jedis.set(key.getKey(), String.valueOf(value)));
    }

    private void save(IHyreosMetric metric) {
        Hylios.get().getInfluxDB().sendMetric(metric);
    }
}
