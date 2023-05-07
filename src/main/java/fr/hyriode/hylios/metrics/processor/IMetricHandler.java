package fr.hyriode.hylios.metrics.processor;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hylios.api.MetricsRedisKey;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;

import java.util.List;
import java.util.Set;

public interface IMetricHandler {

    boolean isInitialized();
    void initialize(List<IHyriPlayer> players);

    Set<IHyreosMetric> process();

    static boolean exists(MetricsRedisKey key) {
        return IMetricHandler.exists(key.getKey());
    }

    static boolean exists(String key) {
        return HyriAPI.get().getRedisProcessor().get(jedis -> jedis.exists(key));
    }

    static void update(MetricsRedisKey key, long value) {
        IMetricHandler.update(key.getKey(), value);
    }

    static void update(String key, long value) {
        HyriAPI.get().getRedisProcessor().processAsync(jedis -> jedis.set(key, String.valueOf(value)));
    }

    static long fetch(MetricsRedisKey key) {
        return IMetricHandler.fetch(key.getKey());
    }

    static long fetch(String key) {
        return HyriAPI.get().getRedisProcessor().get(jedis -> {
            final String value = jedis.get(key);

            return value == null ? 0 : Long.parseLong(value);
        });
    }
}
