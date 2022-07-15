package fr.hyriode.hylios.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.scheduler.IHyriScheduler;
import fr.hyriode.api.scheduler.IHyriTask;
import fr.hyriode.hyggdrasil.api.protocol.environment.HyggData;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerRequest;
import fr.hyriode.hylios.api.queue.QueueAPI;
import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueueInfo;
import fr.hyriode.hylios.api.queue.QueuePlayer;
import fr.hyriode.hylios.api.queue.packet.QueueInfoPacket;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Project: Hyggdrasil
 * Created by AstFaster
 * on 16/04/2022 at 09:14
 */
public class Queue {

    private final QueueInfo info;

    private final PriorityQueue handler;

    private final IHyriTask processingTask;
    private final IHyriTask infoTask;

    public Queue(QueueInfo info) {
        this.info = info;
        this.handler = new PriorityQueue();

        final IHyriScheduler scheduler = HyriAPI.get().getScheduler();

        this.processingTask = scheduler.schedule(this::process, 0, 500, TimeUnit.MILLISECONDS);
        this.infoTask = scheduler.schedule(this::sendQueueInfo, 0, 10, TimeUnit.SECONDS);
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
                final List<QueueGroup> groups = new ArrayList<>();

                this.handler.drainGroups(groups, slots - players);

                for (QueueGroup group : groups) {
                    if (this.handler.remove(group)) {
                        group.send(server);
                    }
                }
            }
        }

        this.updateInfo();
    }

    private void updateInfo() {
        this.info.setTotalGroups(this.getSize());
        this.info.setTotalPlayers(this.getPlayersCount());
    }

    private void sendQueueInfo() {
        this.updateInfo();

        final List<QueueGroup> groups = new ArrayList<>(this.handler);

        for (int i = 0; i < groups.size(); i++) {
            final QueueGroup group = groups.get(i);

            for (QueuePlayer player : group.getPlayers()) {
                this.sendQueueInfo(player, group, i + 1);
            }
        }
    }

    private void sendQueueInfo(QueuePlayer player, QueueGroup group, int place) {
        final QueueInfoPacket packet = new QueueInfoPacket(player, group, this.info, place);

        HyriAPI.get().getPubSub().send(QueueAPI.CHANNEL, packet);
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

        final int neededServers = (int) Math.ceil((double) currentPlayers / slots) + 1;

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
                    .withServerData(data);

            HyriAPI.get().getServerManager().createServer(request, null);
        }
    }

    private List<HyggServer> getAvailableServers() {
        final String map = this.info.getMap();
        final List<HyggServer> servers = new ArrayList<>();

        for (HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            if (server.getType().equals(this.info.getGame()) && server.getSubType().equals(this.info.getGameType()) && (map == null || server.getMap().equals(map))) {
                servers.add(server);
            }
        }
        return servers;
    }

    public void disable() {
        this.processingTask.cancel();
        this.infoTask.cancel();
    }

    public boolean addGroup(QueueGroup group) {
        final boolean result = this.handler.add(group);

        this.sendQueueInfo();

        return result;
    }

    public boolean removeGroup(QueueGroup group) {
        final boolean result = this.handler.remove(group);

        this.sendQueueInfo();

        return result;
    }

    public boolean removeGroup(UUID groupId) {
        for (QueueGroup group : this.handler) {
            if (group.getId().equals(groupId)) {
                return this.removeGroup(group);
            }
        }
        return false;
    }

    public boolean removePlayer(UUID playerId) {
        final QueueGroup group = this.getPlayerGroup(playerId);

        if (group != null) {
            this.removeGroup(group);

            final boolean result = group.removePlayer(playerId);

            if (group.getLeader() != null) {
                this.addGroup(group);
            }
            return result;
        }
        return false;
    }

    public boolean containsGroup(UUID groupId) {
        for (QueueGroup group : this.handler) {
            if (group.getId().equals(groupId)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsPlayer(UUID player) {
        for (QueueGroup group : this.handler) {
            if (group.contains(player)) {
                return true;
            }
        }
        return false;
    }

    public QueueGroup getGroup(UUID groupId) {
        for (QueueGroup group : this.handler) {
            if (group.getId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    public QueueGroup getPlayerGroup(UUID playerId) {
        for (QueueGroup group : this.handler) {
            if (group.contains(playerId)) {
                return group;
            }
        }
        return null;
    }

    public int getSize() {
        int size = 0;

        for (QueueGroup group : this.handler) {
            size += group.getSize();
        }

        return size;
    }

    public int getPlayersCount() {
        int count = 0;
        for (QueueGroup group : this.handler) {
            count+= group.getPlayers().size();
        }
        return count;
    }

    public QueueInfo getInfo() {
        return this.info;
    }

}
