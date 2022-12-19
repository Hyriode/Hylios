package fr.hyriode.hylios.metrics;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.api.rank.type.HyriPlayerRankType;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hyreos.api.metrics.HyreosMetric;
import fr.hyriode.hyreos.api.metrics.HyreosMetricsManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 15/10/2022 at 18:00
 *
 * Heavy players metrics actions.
 */
public class PlayersMetrics {

    private final HyreosMetricsManager metricsManager;

    public PlayersMetrics() {
        this.metricsManager = Hylios.get().getHyreosAPI().getMetricsManager();
    }

    public void start() {
        HyriAPI.get().getScheduler().schedule(this.process(), 0, 12, TimeUnit.HOURS);
    }

    private Runnable process() {
        return () -> {
            final long before = System.currentTimeMillis();
            final List<IHyriPlayer> players = HyriAPI.get().getPlayerManager().getPlayers();
            final HyreosMetric registeredPlayers = new HyreosMetric("registered-players");
            final HyreosMetric premiumPlayers = new HyreosMetric("premium-players");
            final HyreosMetric crackPlayers = new HyreosMetric("crack-players");
            final HyreosMetric hyrisMetric = new HyreosMetric("hyris");
            final HyreosMetric experienceMetric = new HyreosMetric("experience");
            final HyreosMetric hyriPlusMetric = new HyreosMetric("hyri+");
            final HyreosMetric ranksMetric = new HyreosMetric("ranks");

            int premiumCount = 0;
            long hyris = 0;
            double experience = 0;
            int hyriPlus = 0;

            final Map<HyriPlayerRankType, Integer> ranks = new HashMap<>();

            for (IHyriPlayer player : players) {
                hyris += player.getHyris().getAmount();
                experience += player.getNetworkLeveling().getExperience();

                if (player.hasHyriPlus()) {
                    hyriPlus++;
                }

                if (player.isPremium()) {
                    premiumCount++;
                }

                final HyriPlayerRankType rank = player.getRank().getRealPlayerType();

                ranks.put(rank, ranks.getOrDefault(rank, 0) + 1);
            }

            registeredPlayers.addField("value", players.size());
            premiumPlayers.addField("value", premiumCount);
            crackPlayers.addField("value", players.size() - premiumCount);
            hyrisMetric.addField("value", hyris);
            experienceMetric.addField("value", experience);
            hyriPlusMetric.addField("value", hyriPlus);

            for (Map.Entry<HyriPlayerRankType, Integer> entry : ranks.entrySet()) {
                ranksMetric.addField(entry.getKey().name(), entry.getValue());
            }

            this.metricsManager.writeMetric(registeredPlayers);
            this.metricsManager.writeMetric(premiumPlayers);
            this.metricsManager.writeMetric(crackPlayers);
            this.metricsManager.writeMetric(hyrisMetric);
            this.metricsManager.writeMetric(experienceMetric);
            this.metricsManager.writeMetric(hyriPlusMetric);
            this.metricsManager.writeMetric(ranksMetric);

            System.out.println("Heavy players metrics took " + (System.currentTimeMillis() - before) + "ms to process.");
        };
    }

}
