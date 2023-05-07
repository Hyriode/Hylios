package fr.hyriode.hylios.metrics.handler;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.api.rank.PlayerRank;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.api.MetricsRedisKey;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hylios.metrics.data.players.ConnectedPlayers;
import fr.hyriode.hylios.metrics.data.players.HyriPlusPlayers;
import fr.hyriode.hylios.metrics.data.players.PlayersPerRank;
import fr.hyriode.hylios.metrics.data.players.RegisteredPlayers;
import fr.hyriode.hylios.metrics.processor.IMetricHandler;
import fr.hyriode.hylios.metrics.processor.IMetricProcessor;
import fr.hyriode.hylios.metrics.processor.IMultiMetricProcessor;

import java.util.*;

public class PlayersMetricsHandler implements IMetricHandler {

    private static final IMetricProcessor CONNECTED_PLAYERS = () -> new ConnectedPlayers(HyriAPI.get().getNetworkManager().getPlayerCounter().getPlayers());
    private static final IMultiMetricProcessor PLAYERS_PER_RANK = () -> {
        final Set<IHyreosMetric> metrics = new HashSet<>();

        for (final PlayerRank rank : PlayerRank.values()) {
            final long count = IMetricHandler.fetch(String.format(MetricsRedisKey.RANKS.getKey(), rank.getName()));

            metrics.add(new PlayersPerRank(rank, count));
        }

        return metrics;
    };
    private static final IMetricProcessor HYRIPLUS_PLAYERS = () -> new HyriPlusPlayers(IMetricHandler.fetch(MetricsRedisKey.HYRI_PLUS));
    private static final IMetricProcessor REGISTERED_PLAYERS = () -> new RegisteredPlayers(IMetricHandler.fetch(MetricsRedisKey.REGISTERED_PLAYERS));

    @Override
    public boolean isInitialized() {
        boolean ranks = true;
        for (final PlayerRank rank : PlayerRank.values()) {
            ranks &= IMetricHandler.exists(String.format(MetricsRedisKey.RANKS.getKey(), rank.getName()));
        }

        final boolean hyriPlus = IMetricHandler.exists(MetricsRedisKey.HYRI_PLUS);
        final boolean registered = IMetricHandler.exists(MetricsRedisKey.REGISTERED_PLAYERS);

        return ranks && hyriPlus && registered;
    }

    @Override
    public void initialize(List<IHyriPlayer> players) {
        Hylios.get().getLogger().info("Initializing players metrics...");
        final Map<PlayerRank, Long> ranks = new HashMap<>();
        long hyriPlus = 0;
        long registered = 0;

        for (final PlayerRank rank : PlayerRank.values()) {
            ranks.put(rank, 0L);
        }

        for (final IHyriPlayer player : players) {
            if (!player.getRank().isStaff()) {
                final PlayerRank rank = player.getRank().getPlayerType();
                ranks.put(rank, ranks.get(rank) + 1);
            }

            if (player.getHyriPlus().has()) hyriPlus++;
            registered++;
        }

        for (final PlayerRank rank : PlayerRank.values()) {
            final String key = String.format(MetricsRedisKey.RANKS.getKey(), rank.getName());

            IMetricHandler.update(key, ranks.get(rank));
        }

        IMetricHandler.update(MetricsRedisKey.HYRI_PLUS, hyriPlus);
        IMetricHandler.update(MetricsRedisKey.REGISTERED_PLAYERS, registered);
    }

    @Override
    public Set<IHyreosMetric> process() {
        final Set<IHyreosMetric> metrics = new HashSet<>();
        metrics.add(CONNECTED_PLAYERS.process());
        metrics.addAll(PLAYERS_PER_RANK.process());
        metrics.add(HYRIPLUS_PLAYERS.process());
        metrics.add(REGISTERED_PLAYERS.process());

        return metrics;
    }
}
