package fr.hyriode.hylios.server;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.scheduler.IHyriTask;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerCreationInfo;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.server.template.Template;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by AstFaster
 * on 03/05/2023 at 16:59
 */
public class ServersPool {

    private static final int MAX_IDLING = 60; // Time to wait for a server to start

    private final AtomicInteger startingServers = new AtomicInteger(0);

    private IHyriTask task;

    private final int minSize;

    private final String game;
    private final String gameType;

    private final Template.Mode serverTemplate;

    public ServersPool(String game, String gameType) {
        this.game = game;
        this.gameType = gameType;
        this.serverTemplate = Hylios.get().getTemplateManager().getTemplate(this.game).getMode(this.gameType);
        this.minSize = this.serverTemplate.getPoolSize();
    }

    protected void initProcess() {
        this.task = HyriAPI.get().getScheduler().schedule(() -> {
            // Min servers process
            for (int i = 0; i < this.minSize - this.currentServers(); i++) {
                this.newServer();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    protected void stopProcess() {
        this.task.cancel();
        this.startingServers.set(0);
    }

    public void requestServersFor(int players) {
        final int currentServers = this.currentServers();
        final int slots = this.getServers().stream()
                .filter(server -> server.getSlots() != -1)
                .findFirst()
                .map(HyggServer::getSlots)
                .orElse(-1);
        final int neededServers = slots == -1 ? 1 : (int) Math.ceil((double) players / slots);

        if (currentServers < neededServers) {
            for (int i = 0; i < neededServers - currentServers; i++) {
                this.newServer();
            }
        }
    }

    private void newServer() {
        final Template.Resources resources = this.serverTemplate.getResources();
        final HyggServerCreationInfo request = new HyggServerCreationInfo(this.game)
                .withGameType(this.gameType)
                .withMaxMemory(resources.getMaxMemory())
                .withMinMemory(resources.getMinMemory())
                .withCpus(resources.getCpus());

        final IHyriTask safeTask = HyriAPI.get().getScheduler().schedule(this.startingServers::decrementAndGet, MAX_IDLING, TimeUnit.SECONDS);

        Hylios.get().getServersStarter().startServer(request, server -> {
            this.startingServers.decrementAndGet();

            safeTask.cancel();
        });

        this.startingServers.incrementAndGet();
    }

    public List<HyggServer> getServers() {
        return this.serversStream().collect(Collectors.toList());
    }

    public List<HyggServer> getReadyServers() {
        return this.serversStream()
                .filter(server -> server.getState() == HyggServer.State.READY)
                .collect(Collectors.toList());
    }

    private Stream<HyggServer> serversStream() {
        return HyriAPI.get().getServerManager().getServers(this.game)
                .stream()
                .filter(server -> Objects.equals(server.getGameType(), this.gameType));
    }

    public String getGame() {
        return this.game;
    }

    public String getGameType() {
        return this.gameType;
    }

    public int minSize() {
        return this.minSize;
    }

    public int currentServers() {
        return this.getReadyServers().size() + this.startingServers.get();
    }

}
