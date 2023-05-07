package fr.hyriode.hylios.metrics.handler;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hyggdrasil.api.limbo.HyggLimbo;
import fr.hyriode.hyggdrasil.api.proxy.HyggProxy;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hylios.metrics.data.service.PlayersPerGame;
import fr.hyriode.hylios.metrics.data.service.PlayersPerService;
import fr.hyriode.hylios.metrics.data.service.ServiceType;
import fr.hyriode.hylios.metrics.processor.IMetricHandler;
import fr.hyriode.hylios.metrics.processor.IMultiMetricProcessor;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceMetricsHandler implements IMetricHandler {

    private static final IMultiMetricProcessor PLAYERS_PER_GAME = () -> {
        final Map<String, Integer> players = new HashMap<>();

        for (final HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            if (server.getType() == null || server.getGameType() == null) {
                continue;
            }

            final String type = server.getType() + PlayersPerGame.SEPARATOR + server.getGameType();
            final int count = players.getOrDefault(type, 0);

            players.put(type, count + server.getPlayers().size());
        }

        return players.entrySet().stream().map(PlayersPerGame.PROCESSOR).collect(Collectors.toSet());
    };
    private static final IMultiMetricProcessor PLAYERS_PER_LIMBO = () -> {
        final Set<IHyreosMetric> players = new HashSet<>();

        for (final HyggLimbo limbo : HyriAPI.get().getLimboManager().getLimbos()) {
            final int count = limbo.getPlayers().size();
            final String name = limbo.getName();

            final IHyreosMetric metric = new PlayersPerService(ServiceType.LIMBO, name, count);
            players.add(metric);
        }

        return players;
    };
    private static final IMultiMetricProcessor PLAYERS_PER_SERVER = () -> {
        final Set<IHyreosMetric> players = new HashSet<>();

        for (final HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            final int count = server.getPlayers().size();
            final String name = server.getName();

            final IHyreosMetric metric = new PlayersPerService(ServiceType.SERVER, name, count);
            players.add(metric);
        }

        return players;
    };
    private static final IMultiMetricProcessor PLAYERS_PER_PROXY = () -> {
        final Set<IHyreosMetric> players = new HashSet<>();

        for (final HyggProxy proxy : HyriAPI.get().getProxyManager().getProxies()) {
            final int count = proxy.getPlayers().size();
            final String name = proxy.getName();

            final IHyreosMetric metric = new PlayersPerService(ServiceType.PROXY, name, count);
            players.add(metric);
        }

        return players;
    };

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void initialize(List<IHyriPlayer> players) {
        Hylios.get().getLogger().info("Initializing service metrics...");
    }

    @Override
    public Set<IHyreosMetric> process() {
        return Sets.newHashSet(Iterables.concat(
                PLAYERS_PER_GAME.process(),
                PLAYERS_PER_LIMBO.process(),
                PLAYERS_PER_SERVER.process(),
                PLAYERS_PER_PROXY.process()
        ));
    }
}
