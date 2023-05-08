package fr.hyriode.hylios.metrics.handler;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.player.IHyriPlayer;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.service.IHyggService;
import fr.hyriode.hyggdrasil.api.service.IHyggServiceResources;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hylios.metrics.data.service.PlayersPerGame;
import fr.hyriode.hylios.metrics.data.service.PlayersPerService;
import fr.hyriode.hylios.metrics.data.service.ResourcesPerService;
import fr.hyriode.hylios.metrics.data.service.ServiceType;
import fr.hyriode.hylios.metrics.processor.IMetricHandler;
import fr.hyriode.hylios.metrics.processor.IMultiMetricProcessor;

import java.util.*;
import java.util.function.BiFunction;
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
    private static final BiFunction<ServiceType, Set<? extends IHyggService>, Set<IHyreosMetric>> PLAYERS_PER_SERVICE = (type, services) -> {
        final Set<IHyreosMetric> players = new HashSet<>();

        for (final IHyggService service : services) {
            final int count = service.getPlayers().size();
            final String name = service.getName();

            final IHyreosMetric metric = new PlayersPerService(type, name, count);
            players.add(metric);
        }

        return players;
    };
    private static final BiFunction<ServiceType, Set<? extends IHyggService>, Set<IHyreosMetric>> RESOURCES_PER_SERVICE = (type, services) -> {
        final Set<IHyreosMetric> resources = new HashSet<>();

        for (final IHyggService service : services) {
            final IHyggServiceResources container = service.getContainerResources().fetch();
            final String name = service.getName();

            final IHyreosMetric metric = new ResourcesPerService(type, name, container);
            resources.add(metric);
        }

        return resources;
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
        final Set<? extends IHyggService> limbos = HyriAPI.get().getLimboManager().getLimbos();
        final Set<? extends IHyggService> servers = HyriAPI.get().getServerManager().getServers();
        final Set<? extends IHyggService> proxies = HyriAPI.get().getProxyManager().getProxies();

        return Sets.newHashSet(Iterables.concat(
                PLAYERS_PER_GAME.process(),
                PLAYERS_PER_SERVICE.apply(ServiceType.LIMBO, limbos),
                PLAYERS_PER_SERVICE.apply(ServiceType.SERVER, servers),
                PLAYERS_PER_SERVICE.apply(ServiceType.PROXY, proxies),
                RESOURCES_PER_SERVICE.apply(ServiceType.LIMBO, limbos),
                RESOURCES_PER_SERVICE.apply(ServiceType.SERVER, servers),
                RESOURCES_PER_SERVICE.apply(ServiceType.PROXY, proxies)
        ));
    }
}
