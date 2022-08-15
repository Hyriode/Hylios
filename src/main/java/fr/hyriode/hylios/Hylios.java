package fr.hyriode.hylios;

import fr.hyriode.api.impl.application.HyriAPIImpl;
import fr.hyriode.api.impl.application.config.HyriAPIConfig;
import fr.hyriode.hylios.api.HyliosAPI;
import fr.hyriode.hylios.balancing.LobbyBalancer;
import fr.hyriode.hylios.balancing.ProxyBalancer;
import fr.hyriode.hylios.config.HyliosConfig;
import fr.hyriode.hylios.game.rotating.RotatingGameTask;
import fr.hyriode.hylios.host.HostManager;
import fr.hyriode.hylios.queue.QueueManager;
import fr.hyriode.hylios.util.IOUtil;
import fr.hyriode.hylios.util.References;
import fr.hyriode.hylios.util.logger.ColoredLogger;

/**
 * Created by AstFaster
 * on 06/07/2022 at 20:42
 */
public class Hylios {

    private static Hylios instance;

    private ColoredLogger logger;

    private HyliosConfig config;
    private HyliosAPI api;
    private HyriAPIImpl hyriAPI;

    private LobbyBalancer lobbyBalancer;
    private ProxyBalancer proxyBalancer;
    private QueueManager queueManager;
    private HostManager hostManager;

    public void start() {
        instance = this;

        ColoredLogger.printHeaderMessage();
        IOUtil.createDirectory(References.LOG_FOLDER);

        this.logger = new ColoredLogger(References.NAME, References.LOG_FILE);

        System.out.println("Starting Hylios...");

        this.config = HyliosConfig.load();
        this.api = new HyliosAPI();
        this.hyriAPI = new HyriAPIImpl(new HyriAPIConfig.Builder()
                .withRedisConfig(this.config.getRedisConfig())
                .withMongoDBConfig(this.config.getMongoDBConfig())
                .withDevEnvironment(false)
                .withHyggdrasil(true)
                .build(), References.NAME);
        this.lobbyBalancer = new LobbyBalancer();
        this.proxyBalancer = new ProxyBalancer();
        this.queueManager = new QueueManager();
        this.hostManager = new HostManager();

        new RotatingGameTask().start();

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

    public HyliosAPI getAPI() {
        return this.api;
    }

    public HyriAPIImpl getHyriAPI() {
        return this.hyriAPI;
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

}
