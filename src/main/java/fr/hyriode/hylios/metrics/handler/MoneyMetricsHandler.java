package fr.hyriode.hylios.metrics.handler;

import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hylios.metrics.processor.IMetricHandler;
import fr.hyriode.hylios.metrics.processor.IMetricProcessor;
import fr.hyriode.hylios.metrics.data.money.CirculatingMoney;
import fr.hyriode.hylios.metrics.data.money.MoneyType;
import fr.hyriode.hyreos.api.HyreosRedisKey;

import java.util.Set;

public class MoneyMetricsHandler implements IMetricHandler {

    private static final IMetricProcessor HYRIS = () -> new CirculatingMoney(MoneyType.HYRIS, IMetricProcessor.fetch(HyreosRedisKey.HYRIS));
    private static final IMetricProcessor HYODES = () -> new CirculatingMoney(MoneyType.HYODES, IMetricProcessor.fetch(HyreosRedisKey.HYODES));

    @Override
    public Set<IHyreosMetric> process() {
        return Set.of(HYRIS.process(), HYODES.process());
    }
}
