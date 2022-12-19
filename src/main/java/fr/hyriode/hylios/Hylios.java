package fr.hyriode.hylios;

import fr.hyriode.api.impl.application.HyriAPIImpl;
import fr.hyriode.api.impl.application.config.HyriAPIConfig;
import fr.hyriode.hylios.balancing.LobbyBalancer;
import fr.hyriode.hylios.balancing.ProxyBalancer;
import fr.hyriode.hylios.config.HyliosConfig;
import fr.hyriode.hylios.game.rotating.RotatingGameTask;
import fr.hyriode.hylios.host.HostManager;
import fr.hyriode.hylios.metrics.LiveCounterMetrics;
import fr.hyriode.hylios.metrics.PacketMetrics;
import fr.hyriode.hylios.metrics.PlayersMetrics;
import fr.hyriode.hylios.party.PartyHandler;
import fr.hyriode.hylios.queue.QueueManager;
import fr.hyriode.hylios.util.IOUtil;
import fr.hyriode.hylios.util.References;
import fr.hyriode.hylios.util.logger.ColoredLogger;
import fr.hyriode.hylios.world.WorldGenerationHandler;
import fr.hyriode.hyreos.api.HyreosAPI;

/**
 * Created by AstFaster
 * on 06/07/2022 at 20:42
 */
public class Hylios {

    private static Hylios instance;

    private ColoredLogger logger;

    private HyliosConfig config;
    private HyriAPIImpl hyriAPI;
    private HyreosAPI hyreosAPI;

    private LobbyBalancer lobbyBalancer;
    private ProxyBalancer proxyBalancer;
    private QueueManager queueManager;
    private HostManager hostManager;
    private WorldGenerationHandler generationHandler;
    private PartyHandler partyHandler;

    public void start() {
        instance = this;

        ColoredLogger.printHeaderMessage();
        IOUtil.createDirectory(References.LOG_FOLDER);

        this.logger = new ColoredLogger(References.NAME, References.LOG_FILE);

        System.out.println("Starting Hylios...");

        this.config = HyliosConfig.load();
        this.hyriAPI = new HyriAPIImpl(new HyriAPIConfig.Builder()
                .withRedisConfig(this.config.getRedisConfig())
                .withMongoDBConfig(this.config.getMongoDBConfig())
                .withDevEnvironment(false)
                .withHyggdrasil(true)
                .build(), References.NAME);
        this.hyreosAPI = new HyreosAPI(this.hyriAPI.getRedisConnection().clone().getPool());
        this.hyreosAPI.start();
        this.lobbyBalancer = new LobbyBalancer();
        this.proxyBalancer = new ProxyBalancer();
        this.queueManager = new QueueManager();
        this.hostManager = new HostManager();
        this.generationHandler = new WorldGenerationHandler();
        this.partyHandler = new PartyHandler();

        new LiveCounterMetrics().start();
        new PlayersMetrics().start();
        new PacketMetrics().start();

        new RotatingGameTask().start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        System.out.println("Stopping Hylios...");

        this.queueManager.disable();
        this.hyreosAPI.stop();
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

    public HyreosAPI getHyreosAPI() {
        return this.hyreosAPI;
    }

    public LobbyBalancer getLobbyBalancer() {
        return this.lobbyBalancer;
    }

    public ProxyBalancer getProxyBalancer() {
        return this.proxyBalancer;
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

    public PartyHandler getPlayerHandler() {
        return this.partyHandler;
    }

}
