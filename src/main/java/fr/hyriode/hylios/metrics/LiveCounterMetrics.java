package fr.hyriode.hylios.metrics;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyggdrasil.api.proxy.HyggProxy;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.queue.Queue;
import fr.hyriode.hyreos.api.metrics.HyreosMetric;
import fr.hyriode.hyreos.api.metrics.HyreosMetricsManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 15/10/2022 at 14:29
 */
public class LiveCounterMetrics {

    private final HyreosMetricsManager metricsManager;

    public LiveCounterMetrics() {
        this.metricsManager = Hylios.get().getHyreosAPI().getMetricsManager();
    }

    public void start() {
        HyriAPI.get().getScheduler().schedule(this.process(), 5, 5, TimeUnit.SECONDS);
    }

    private Runnable process() {
        return () -> {
            this.sendPlayersMetrics();

            this.sendProxiesMetrics();
            this.sendServersMetrics();

            this.sendHostsMetrics();
            this.sendQueuesMetrics();
        };
    }

    private void sendPlayersMetrics() {
        final HyreosMetric metric = new HyreosMetric("players");

        metric.addField("value",  HyriAPI.get().getNetworkManager().getPlayerCounter().getPlayers());

        this.metricsManager.writeMetric(metric);
    }

    private void sendProxiesMetrics() {
        final HyreosMetric metric = new HyreosMetric("proxies");

        metric.addField("value", HyriAPI.get().getProxyManager().getProxies().size());

        this.metricsManager.writeMetric(metric);

        this.sendProxiesPlayersMetrics();
    }

    private void sendProxiesPlayersMetrics() {
        final HyreosMetric metric = new HyreosMetric("proxies-players");

        for (HyggProxy proxy : HyriAPI.get().getProxyManager().getProxies()) {
            metric.addField(proxy.getName(), proxy.getPlayers().size());
        }

        if (metric.getFields().size() != 0) {
            this.metricsManager.writeMetric(metric);
        }
    }

    private void sendServersMetrics() {
        // Number of servers
        final HyreosMetric metric = new HyreosMetric("servers");

        metric.addField("value", HyriAPI.get().getServerManager().getServers().size());

        this.metricsManager.writeMetric(metric);

        this.sendServersTypesMetrics();

        // Players
        this.sendServersTypesPlayersMetrics();
        this.sendServersSubTypesPlayersMetrics();
    }

    private void sendServersTypesMetrics() {
        final Map<String, HyreosMetric> metrics = new HashMap<>();

        for (HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            final String type = server.getType();
            final String gameType = server.getGameType();

            if (gameType == null) {
                continue;
            }

            final String key = type + "#" + gameType;
            final HyreosMetric metric = metrics.getOrDefault(key, new HyreosMetric("servers-types").addTag("type", type));

            metric.addField(gameType, ((int) metric.getFields().getOrDefault(gameType, 0)) + 1);

            metrics.put(key, metric);
        }

        for (HyreosMetric metric : metrics.values()) {
            if (metric.getFields().size() != 0) {
                this.metricsManager.writeMetric(metric);
            }
        }
    }

    private void sendServersTypesPlayersMetrics() {
        final HyreosMetric metric = new HyreosMetric("servers-types-players");

        for (HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            final String type = server.getType();

            metric.addField(type, ((int) metric.getFields().getOrDefault(type, 0)) + server.getPlayers().size());
        }

        if (metric.getFields().size() != 0) {
            this.metricsManager.writeMetric(metric);
        }
    }

    private void sendServersSubTypesPlayersMetrics() {
        final Map<String, HyreosMetric> metrics = new HashMap<>();

        for (HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            final String type = server.getType();
            final String gameType = server.getGameType();

            if (gameType == null) {
                continue;
            }

            final String key = type + "#" + gameType;
            final HyreosMetric metric = metrics.getOrDefault(key, new HyreosMetric("servers-subtypes-players").addTag("type", type));

            metric.addField(gameType, ((int) metric.getFields().getOrDefault(gameType, 0)) + server.getPlayers().size());

            metrics.put(key, metric);
        }

        for (HyreosMetric metric : metrics.values()) {
            if (metric.getFields().size() != 0) {
                this.metricsManager.writeMetric(metric);
            }
        }
    }

    private void sendHostsMetrics() {
        final HyreosMetric metric = new HyreosMetric("hosts");

        int count = 0;
        for (HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            if (server.getAccessibility() == HyggServer.Accessibility.HOST) {
                count++;
            }
        }

        metric.addField("value", count);

        this.metricsManager.writeMetric(metric);
    }

    private void sendQueuesMetrics() {
        final HyreosMetric queuesMetric = new HyreosMetric("queues");
        final Collection<Queue> queues = Hylios.get().getQueueManager().getQueues();

        queuesMetric.addField("value", queues.size());

        final HyreosMetric playersMetric = new HyreosMetric("queues-players");

        int count = 0;
        for (Queue queue : queues) {
            count += queue.getHandle().getPlayers().size()  ;
        }

        playersMetric.addField("value", count);

        this.metricsManager.writeMetric(queuesMetric);
        this.metricsManager.writeMetric(playersMetric);
    }

}