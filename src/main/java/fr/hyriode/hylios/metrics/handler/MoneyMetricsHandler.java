package fr.hyriode.hylios.metrics.handler;

import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.api.MetricsRedisKey;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hylios.metrics.data.money.CirculatingMoney;
import fr.hyriode.hylios.metrics.data.money.MoneyType;
import fr.hyriode.hylios.metrics.processor.IMetricHandler;
import fr.hyriode.hylios.metrics.processor.IMetricProcessor;

import java.util.List;
import java.util.Set;

public class MoneyMetricsHandler implements IMetricHandler {

    private static final IMetricProcessor HYRIS = () -> new CirculatingMoney(MoneyType.HYRIS, IMetricHandler.fetch(MetricsRedisKey.HYRIS));
    private static final IMetricProcessor HYODES = () -> new CirculatingMoney(MoneyType.HYODES, IMetricHandler.fetch(MetricsRedisKey.HYODES));

    @Override
    public boolean isInitialized() {
        final boolean hyris = this.exists(MetricsRedisKey.HYRIS);
        final boolean hyodes = this.exists(MetricsRedisKey.HYODES);

        return hyris && hyodes;
    }

    @Override
    public void initialize(List<IHyriPlayer> players) {
        Hylios.get().getLogger().info("Initializing money metrics...");
        long hyris = 0;
        long hyodes = 0;

        for (final IHyriPlayer player : players) {
            hyris += player.getHyris().getAmount();
            hyodes += player.getHyodes().getAmount();
        }

        this.update(MetricsRedisKey.HYRIS, hyris);
        this.update(MetricsRedisKey.HYODES, hyodes);
    }


    @Override
    public Set<IHyreosMetric> process() {
        return Set.of(HYRIS.process(), HYODES.process());
    }
}
