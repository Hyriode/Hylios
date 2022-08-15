package fr.hyriode.hylios.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.scheduler.IHyriScheduler;
import fr.hyriode.api.scheduler.IHyriTask;
import fr.hyriode.hyggdrasil.api.protocol.environment.HyggData;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerOptions;
import fr.hyriode.hyggdrasil.api.server.HyggServerRequest;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.api.queue.QueueAPI;
import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueueInfo;
import fr.hyriode.hylios.api.queue.QueuePlayer;
import fr.hyriode.hylios.api.queue.packet.QueueInfoPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 16/04/2022 at 09:14
 */
public class NormalQueue extends Queue {

    private final QueueInfo info;

    private final IHyriTask processingTask;
    private final IHyriTask infoTask;

    public NormalQueue(QueueInfo info) {
        this.info = info;

        final IHyriScheduler scheduler = HyriAPI.get().getScheduler();

        this.processingTask = scheduler.schedule(this::process, 0, 500, TimeUnit.MILLISECONDS);
        this.infoTask = scheduler.schedule(this::sendInfo, 0, 10, TimeUnit.SECONDS);
    }

    private void process() {
        final List<HyggServer> availableServers = this.getAvailableServers();

        availableServers.sort(Comparator.comparingInt(server -> server.getPlayingPlayers().size()));
        Collections.reverse(availableServers);

        this.anticipateServers(availableServers);

        for (HyggServer server : availableServers) {
            final int slots = server.getSlots();
            final int players = server.getPlayingPlayers().size();

            if (server.isAccessible() && players < slots) {
                this.drainGroups(server);
            }
        }

        this.updateInfo();
    }

    private void updateInfo() {
        this.info.setTotalGroups(this.handler.size());
        this.info.setTotalPlayers(this.getSize());
    }

    @Override
    public void sendInfo() {
        this.updateInfo();

        final List<QueueGroup> groups = new ArrayList<>(this.handler);

        for (int i = 0; i < groups.size(); i++) {
            final QueueGroup group = groups.get(i);

            for (QueuePlayer player : group.getPlayers()) {
                final QueueInfoPacket packet = new QueueInfoPacket(player, group, this.info, i + 1);

                HyriAPI.get().getPubSub().send(QueueAPI.CHANNEL, packet);
            }
        }
    }

    private void anticipateServers(List<HyggServer> currentServers) {
        int slots = -1;

        for (HyggServer server : currentServers) {
            final int serverSlots = server.getSlots();

            if (slots == -1) {
                slots = serverSlots;
                continue;
            }

            if (serverSlots != -1 && serverSlots < slots) {
                slots = serverSlots;
            }
        }

        int currentPlayers = this.getSize();
        for (HyggServer server : currentServers) {
            currentPlayers += server.getPlayingPlayers().size();
        }

        final int neededServers = slots == -1 ? 1 : (int) Math.ceil((double) currentPlayers / slots) + 1;

        if (currentPlayers == 0) {
            return;
        }

        for (int i = 0; i < neededServers - currentServers.size(); i++ ) {
            final HyggData data = new HyggData();

            data.add(HyggServer.SUB_TYPE_KEY, this.info.getGameType());

            if (this.info.getMap() != null) {
                data.add(HyggServer.MAP_KEY, this.info.getMap());
            }

            final HyggServerRequest request = new HyggServerRequest()
                    .withServerType(this.info.getGame())
                    .withServerData(data)
                    .withServerOptions(new HyggServerOptions());

            HyriAPI.get().getServerManager().createServer(request, null);
        }
    }

    private List<HyggServer> getAvailableServers() {
        final String map = this.info.getMap();
        final List<HyggServer> servers = new ArrayList<>();

        for (HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            if (server.getType().equals(this.info.getGame()) && server.getSubType().equals(this.info.getGameType()) && (map == null || server.getMap().equals(map)) && !Hylios.get().getAPI().getHostAPI().isHost(server)) {
                servers.add(server);
            }
        }
        return servers;
    }

    @Override
    public void disable() {
        this.processingTask.cancel();
        this.infoTask.cancel();
    }

    public QueueInfo getInfo() {
        return this.info;
    }

}
