package fr.hyriode.hylios.metrics.processor;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hylios.api.HyliosMetricsRedisKey;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;

import java.util.Set;

public interface IMetricHandler {

    Set<IHyreosMetric> process();

    static long fetch(HyliosMetricsRedisKey key) {
        return HyriAPI.get().getRedisProcessor().get(jedis -> {
            final String value = jedis.get(key.getKey());

            return value == null ? 0 : Long.parseLong(value);
        });
    }
}
