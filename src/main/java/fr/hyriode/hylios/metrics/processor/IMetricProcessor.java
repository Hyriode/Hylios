package fr.hyriode.hylios.metrics.processor;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hyreos.api.HyreosRedisKey;

@FunctionalInterface
public interface IMetricProcessor {

    IHyreosMetric process();

    static long fetch(HyreosRedisKey key) {
        return HyriAPI.get().getRedisProcessor().get(jedis -> {
            final String value = jedis.get(key.getKey());

            return value == null ? 0 : Long.parseLong(value);
        });
    }
}
