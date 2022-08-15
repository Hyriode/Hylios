package fr.hyriode.hylios.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.scheduler.IHyriScheduler;
import fr.hyriode.api.scheduler.IHyriTask;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.api.host.HostData;
import fr.hyriode.hylios.api.queue.QueueAPI;
import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueuePlayer;
import fr.hyriode.hylios.api.queue.event.QueueEventType;
import fr.hyriode.hylios.api.queue.server.SQueueInfo;
import fr.hyriode.hylios.api.queue.server.event.SQueueRemoveEvent;
import fr.hyriode.hylios.api.queue.server.packet.SQueueInfoPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 30/07/2022 at 23:26
 */
public class HostQueue extends Queue {

    private final SQueueInfo info;

    private final IHyriTask processingTask;
    private final IHyriTask infoTask;

    public HostQueue(SQueueInfo info) {
        this.info = info;

        final IHyriScheduler scheduler = HyriAPI.get().getScheduler();

        this.processingTask = scheduler.schedule(this::process, 0, 500, TimeUnit.MILLISECONDS);
        this.infoTask = scheduler.schedule(this::sendInfo, 0, 10, TimeUnit.SECONDS);
    }

    private void updateInfo() {
        this.info.setTotalGroups(this.handler.size());
        this.info.setTotalPlayers(this.getSize());
    }

    private void process() {
        this.updateInfo();

        final HyggServer server = HyriAPI.get().getServerManager().getServer(this.info.getServerName());

        if (server == null) {
            this.disable();
            return;
        }

        if (!server.isAccessible()) {
            return;
        }

        final HostData hostData = Hylios.get().getAPI().getHostAPI().getHostData(server);
        final int slots = server.getSlots();
        final int players = server.getPlayingPlayers().size();

        if (players >= slots) {
            return;
        }

        if (hostData.isWhitelisted()) {
            for (QueueGroup group : this.handler) {
                if (hostData.getWhitelistedPlayers().contains(group.getLeader().getUniqueId()) && players + group.getSize() <= slots) {
                    if (this.handler.remove(group)) {
                        group.send(server);
                    }
                }
            }
        } else {
            this.drainGroups(server);
        }
    }

    @Override
    public void sendInfo() {
        this.updateInfo();

        final List<QueueGroup> groups = new ArrayList<>(this.handler);

        for (int i = 0; i < groups.size(); i++) {
            final QueueGroup group = groups.get(i);

            for (QueuePlayer player : group.getPlayers()) {
                final SQueueInfoPacket packet = new SQueueInfoPacket(player, group, this.info, i + 1);

                HyriAPI.get().getPubSub().send(QueueAPI.CHANNEL, packet);
            }
        }
    }

    private void removeAll() {
        for (QueueGroup group : this.handler) {
            HyriAPI.get().getNetworkManager().getEventBus().publish(new SQueueRemoveEvent(QueueEventType.OK, group, this.info));
        }
    }

    @Override
    public void disable() {
        this.removeAll();

        this.handler.clear();
        this.processingTask.cancel();
        this.infoTask.cancel();
    }

    public SQueueInfo getInfo() {
        return this.info;
    }

}
