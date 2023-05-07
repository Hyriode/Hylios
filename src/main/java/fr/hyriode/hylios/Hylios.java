package fr.hyriode.hylios;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.impl.application.HyriAPIImpl;
import fr.hyriode.api.impl.application.config.HyriAPIConfig;
import fr.hyriode.hylios.balancing.LimboBalancer;
import fr.hyriode.hylios.balancing.LobbyBalancer;
import fr.hyriode.hylios.balancing.ProxyBalancer;
import fr.hyriode.hylios.booster.BoosterHandler;
import fr.hyriode.hylios.config.HyliosConfig;
import fr.hyriode.hylios.game.rotating.RotatingGameTask;
import fr.hyriode.hylios.host.HostManager;
import fr.hyriode.hylios.influx.InfluxDB;
import fr.hyriode.hylios.metrics.MetricsManager;
import fr.hyriode.hylios.party.PartyHandler;
import fr.hyriode.hylios.queue.QueueManager;
import fr.hyriode.hylios.util.IOUtil;
import fr.hyriode.hylios.util.References;
import fr.hyriode.hylios.util.logger.ColoredLogger;
import fr.hyriode.hylios.world.WorldGenerationHandler;

/**
 * Created by AstFaster
 * on 06/07/2022 at 20:42
 */
public class Hylios {

    private static Hylios instance;

    private ColoredLogger logger;

    private HyliosConfig config;
    private HyriAPIImpl hyriAPI;
    private InfluxDB influxDB;

    private LobbyBalancer lobbyBalancer;
    private ProxyBalancer proxyBalancer;
    private LimboBalancer limboBalancer;
    private QueueManager queueManager;
    private HostManager hostManager;
    private WorldGenerationHandler generationHandler;
    private PartyHandler partyHandler;
    private BoosterHandler boosterHandler;
    private MetricsManager metricsManager;

    public void start() {
        instance = this;

        ColoredLogger.printHeaderMessage();
        IOUtil.createDirectory(References.LOG_FOLDER);

        this.logger = new ColoredLogger(References.NAME, References.LOG_FILE);

        System.out.println("Starting Hylios...");

        this.config = HyliosConfig.load();
        this.hyriAPI = new HyriAPIImpl(new HyriAPIConfig.Builder()
                .withRedisConfig(this.config.redis())
                .withMongoDBConfig(this.config.mongo())
                .withDevEnvironment(false)
                .withHyggdrasil(true)
                .build(), References.NAME);
        this.influxDB = new InfluxDB(this.config.influx());

        this.lobbyBalancer = new LobbyBalancer();
        this.proxyBalancer = new ProxyBalancer();
        this.limboBalancer = new LimboBalancer();
        this.queueManager = new QueueManager();
        this.hostManager = new HostManager();
        this.generationHandler = new WorldGenerationHandler();
        this.partyHandler = new PartyHandler();
        this.boosterHandler = new BoosterHandler();
        this.metricsManager = new MetricsManager();
        this.metricsManager.start();

        new RotatingGameTask().start();

        HyriAPI.get().getScheduler().runAsync(() -> this.metricsManager.initialize());
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        System.out.println("Stopping Hylios...");

        this.queueManager.disable();
    }

    public static Hylios get() {
        return instance;
    }

    public ColoredLogger getLogger() {
        return this.logger;
    }

    public HyliosConfig getConfig() {
        return this.config;
    }

    public HyriAPIImpl getHyriAPI() {
        return this.hyriAPI;
    }

    public InfluxDB getInfluxDB() {
        return this.influxDB;
    }

    public LobbyBalancer getLobbyBalancer() {
        return this.lobbyBalancer;
    }

    public ProxyBalancer getProxyBalancer() {
        return this.proxyBalancer;
    }

    public LimboBalancer getLimboBalancer() {
        return this.limboBalancer;
    }

    public QueueManager getQueueManager() {
        return this.queueManager;
    }

    public HostManager getHostManager() {
        return this.hostManager;
    }

    public WorldGenerationHandler getGenerationHandler() {
        return this.generationHandler;
    }

    public PartyHandler getPartyHandler() {
        return this.partyHandler;
    }

    public BoosterHandler getBoosterHandler() {
        return this.boosterHandler;
    }

    public MetricsManager getMetricsManager() {
        return this.metricsManager;
    }
}
