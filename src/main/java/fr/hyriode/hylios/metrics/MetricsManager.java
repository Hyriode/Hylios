package fr.hyriode.hylios.metrics;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.metrics.handler.MoneyMetricsHandler;
import fr.hyriode.hylios.metrics.handler.PlayersMetricsHandler;
import fr.hyriode.hylios.metrics.processor.IMetricProcessor;
import fr.hyriode.hyreos.api.HyreosRedisKey;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hyreos.api.players.PlayersPerGame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MetricsManager {

    private final Set<IMetricProcessor> metrics = new HashSet<>();

    public void initialize() {
        for (final HyreosRedisKey key : HyreosRedisKey.values()) {
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

        this.update(HyreosRedisKey.HYRIS, hyris);
        this.update(HyreosRedisKey.HYODES, hyodes);
        this.update(HyreosRedisKey.HYRI_PLUS, hyriplus);
        this.update(HyreosRedisKey.REGISTERED_PLAYERS, registered);
    }

    public void start() {
        this.metrics.add(new MoneyMetricsHandler());
        this.metrics.add(new PlayersMetricsHandler());

        HyriAPI.get().getScheduler().schedule(this::process, 60, 60, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public void process() {
        for (final IMetricProcessor metric : this.metrics) {
            final Set<Object> processed = Set.class.cast(metric.process());


        }

        this.handlePlayersPerGame();
    }

    public void handlePlayersPerGame() {
        final Map<String, Map<String, Integer>> perGameType = new HashMap<>();

        for (final HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            if (server.getType() == null || server.getGameType() == null) {
                continue;
            }

            final Map<String, Integer> types = perGameType.getOrDefault(server.getType(), new HashMap<>());
            final int players = server.getPlayers().size();

            if (types.containsKey(server.getGameType())) {
                final int count = types.get(server.getGameType());

                types.put(server.getGameType(), count + players);
            } else {
                types.put(server.getGameType(), players);
            }

            perGameType.put(server.getType(), types);
        }

        perGameType.forEach((type, types) -> types.forEach((gameType, players) -> {
            final IHyreosMetric metric = new PlayersPerGame(type, gameType, players);

            Hyreos.get().getInfluxDB().sendMetrics(metric);
        }));
    }

    private boolean exists(HyreosRedisKey key) {
        return HyriAPI.get().getRedisProcessor().get(jedis -> jedis.exists(key.getKey()));
    }

    private void update(HyreosRedisKey key, long value) {
        HyriAPI.get().getRedisProcessor().processAsync(jedis -> jedis.set(key.getKey(), String.valueOf(value)));
    }

    private void save(Object object) {
        Hylios.get().getInfluxDB().sendMetric(object);
    }
}
