package fr.hyriode.hylios.server;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.server.IHyriServerManager;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerCreationInfo;
import fr.hyriode.hylios.Hylios;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Created by AstFaster
 * on 03/05/2023 at 22:00
 */
public class ServersStarter {

    private static final int MAX_SERVERS_STARTING = Hylios.get().getConfig().maxStartingServers();

    private final Queue<WaitingServer> waitingServers = new ConcurrentLinkedQueue<>();
    private final List<HyggServer> startingServers = new ArrayList<>();

    private final IHyriServerManager serverManager;

    public ServersStarter() {
        this.serverManager = HyriAPI.get().getServerManager();
    }

    public void disable() {
        this.waitingServers.clear();
        this.startingServers.clear();
    }

    public void startServer(HyggServerCreationInfo serverInfo, Consumer<HyggServer> onStarted) {
        if (this.startingServers.size() == MAX_SERVERS_STARTING) {
            this.waitingServers.add(new WaitingServer(serverInfo, onStarted));
            return;
        }

        this.serverManager.createServer(serverInfo, server -> {
            this.startingServers.add(server);
            this.serverManager.waitForState(server.getName(), HyggServer.State.READY, __ -> {
                if (this.startingServers.remove(server)) {
                    onStarted.accept(server);

                    this.checkWaitingServers();
                }
            });
        });
    }

    private void checkWaitingServers() {
        if (this.waitingServers.size() == 0 || this.startingServers.size() >= MAX_SERVERS_STARTING) {
            return;
        }

        final int space = MAX_SERVERS_STARTING - this.startingServers.size();

        for (int i = 0; i < space; i++) {
            if (i >= this.waitingServers.size()) {
                return;
            }

            final WaitingServer waitingServer = this.waitingServers.poll();

            this.startServer(waitingServer.serverInfo(), waitingServer.onStarted());
        }
    }

    private record WaitingServer(HyggServerCreationInfo serverInfo, Consumer<HyggServer> onStarted) {}

}
