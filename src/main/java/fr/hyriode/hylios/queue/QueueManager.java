package fr.hyriode.hylios.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.event.IHyriEventBus;
import fr.hyriode.api.party.IHyriParty;
import fr.hyriode.hylios.api.queue.QueueAPI;
import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueueInfo;
import fr.hyriode.hylios.api.queue.QueuePlayer;
import fr.hyriode.hylios.api.queue.event.QueueAddEvent;
import fr.hyriode.hylios.api.queue.event.QueueEventType;
import fr.hyriode.hylios.api.queue.event.QueueRemoveEvent;
import fr.hyriode.hylios.api.queue.event.QueueUpdateGroupEvent;
import fr.hyriode.hylios.api.queue.packet.QueueAddPacket;
import fr.hyriode.hylios.api.queue.packet.group.QueueAddGroupPacket;
import fr.hyriode.hylios.api.queue.packet.group.QueueRemoveGroupPacket;
import fr.hyriode.hylios.api.queue.packet.group.QueueUpdateGroupPacket;
import fr.hyriode.hylios.api.queue.packet.player.QueueAddPlayerPacket;
import fr.hyriode.hylios.api.queue.packet.player.QueueRemovePlayerPacket;
import fr.hyriode.hylios.api.queue.server.SQueueInfo;
import fr.hyriode.hylios.api.queue.server.event.SQueueAddEvent;
import fr.hyriode.hylios.api.queue.server.event.SQueueRemoveEvent;
import fr.hyriode.hylios.api.queue.server.event.SQueueUpdateGroupEvent;
import fr.hyriode.hylios.api.queue.server.packet.SQueueAddPacket;
import fr.hyriode.hylios.api.queue.server.packet.group.SQueueAddGroupPacket;
import fr.hyriode.hylios.api.queue.server.packet.player.SQueueAddPlayerPacket;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by AstFaster
 * on 16/04/2022 at 09:10
 */
public class QueueManager {

    private final IHyriEventBus eventBus;
    private final Map<String, Queue> queues;

    public QueueManager() {
        this.eventBus = HyriAPI.get().getNetworkManager().getEventBus();
        this.queues = new ConcurrentHashMap<>();

        HyriAPI.get().getPubSub().subscribe(QueueAPI.CHANNEL, new QueueReceiver(this));
    }

    public void disable() {
        for (Queue queue : this.queues.values()) {
            queue.disable();
        }
    }

    public void handlePacket(SQueueAddPlayerPacket packet) {
        final QueuePlayer player = packet.getPlayer();

        this.handlePacket(packet, new QueueGroup(player.getUniqueId(), player, new ArrayList<>()));
    }

    public void handlePacket(SQueueAddGroupPacket packet) {
        this.handlePacket(packet, packet.getGroup());
    }

    private void handlePacket(SQueueAddPacket packet, QueueGroup group) {
        final String serverName = packet.getServerName();
        final Queue queue = this.getQueue(serverName);
        final Queue currentQueue = this.getCurrentPlayerQueue(group.getId());

        QueueEventType responseType = QueueEventType.OK;

        if (queue.equals(currentQueue)) {
            responseType = QueueEventType.ALREADY_IN;
        } else {
            if (currentQueue != null) {
                currentQueue.removeGroup(group.getId());
            }

            queue.addGroup(group);
        }

        this.eventBus.publish(new SQueueAddEvent(responseType, group, ((HostQueue) queue).getInfo()));
    }


    public void handlePacket(QueueAddPlayerPacket packet) {
        final QueuePlayer player = packet.getPlayer();

        this.handleAddPacket(packet, new QueueGroup(player.getUniqueId(), player, new ArrayList<>()));
    }

    public void handlePacket(QueueAddGroupPacket packet) {
        this.handleAddPacket(packet, packet.getGroup());
    }

    private void handleAddPacket(QueueAddPacket packet, QueueGroup group) {
        final String game = packet.getGame();
        final String gameType = packet.getGameType();
        final String map = packet.getMap();
        final Queue queue = this.getQueue(game, gameType, map);
        final Queue currentQueue = this.getCurrentPlayerQueue(group.getId());

        QueueEventType responseType = QueueEventType.OK;

        if (queue.equals(currentQueue)) {
            responseType = QueueEventType.ALREADY_IN;
        } else {
            if (currentQueue != null) {
                currentQueue.removeGroup(group.getId());
            }

            final IHyriParty party = HyriAPI.get().getPartyManager().getParty(group.getId());

            if (party != null) {
                HyriAPI.get().getQueueManager().setPartyQueue(party.getId(), game, gameType, map);
            }

            for (QueuePlayer member : group.getPlayers()) {
                HyriAPI.get().getQueueManager().setPlayerQueue(member.getUniqueId(), game, gameType, map);
            }

            queue.addGroup(group);
        }

        this.eventBus.publish(new QueueAddEvent(responseType, group, ((NormalQueue) queue).getInfo()));
    }

    public void handlePacket(QueueRemovePlayerPacket packet) {
        final UUID playerId = packet.getPlayerId();
        final Queue queue = this.getCurrentPlayerQueue(playerId);
        final QueueGroup group = queue != null ? queue.getPlayerGroup(playerId) : null;
        final QueueGroup result = group != null ? group.clone() : null;

        QueueEventType responseType = QueueEventType.UNKNOWN;

        if (queue == null) {
            responseType = QueueEventType.NOT_IN_QUEUE;
        } else {
            if (queue.removePlayer(playerId)) {
                HyriAPI.get().getQueueManager().removePlayerQueue(playerId);

                responseType = QueueEventType.OK;
            }
        }

        this.sendRemoveEvents(responseType, queue, result);
    }

    public void handlePacket(QueueRemoveGroupPacket packet) {
        final UUID groupId = packet.getGroupId();
        final Queue queue = this.getCurrentGroupQueue(groupId);
        final QueueGroup group = queue != null ? queue.getGroup(groupId) : null;

        QueueEventType responseType = QueueEventType.UNKNOWN;

        if (queue == null) {
            responseType = QueueEventType.NOT_IN_QUEUE;
        } else {
            if (queue.removeGroup(groupId)) {
                final IHyriParty party = HyriAPI.get().getPartyManager().getParty(group.getId());

                if (party != null) {
                    HyriAPI.get().getQueueManager().removePartyQueue(groupId);
                }

                for (QueuePlayer member : group.getPlayers()) {
                    HyriAPI.get().getQueueManager().removePlayerQueue(member.getUniqueId());
                }

                responseType = QueueEventType.OK;
            }
        }

        this.sendRemoveEvents(responseType, queue, group);
    }

    private void sendRemoveEvents(QueueEventType responseType, Queue queue, QueueGroup group) {
        if (queue == null) {
            this.eventBus.publish(new QueueRemoveEvent(responseType, null, null));
            this.eventBus.publish(new SQueueRemoveEvent(responseType, null, null));
        } else if (queue instanceof final NormalQueue normalQueue) {
            this.eventBus.publish(new QueueRemoveEvent(responseType, group, normalQueue.getInfo()));
        } else if (queue instanceof final HostQueue hostQueue) {
            this.eventBus.publish(new SQueueRemoveEvent(responseType, group, hostQueue.getInfo()));
        }
    }

    public void handlePacket(QueueUpdateGroupPacket packet) {
        final QueueGroup group = packet.getGroup();
        final UUID groupId = group.getId();
        final Queue queue = this.getCurrentGroupQueue(groupId);

        QueueEventType responseType = QueueEventType.OK;

        if (queue == null) {
            responseType = QueueEventType.NOT_IN_QUEUE;
        } else {
            queue.getGroup(groupId).update(packet);
        }

        if (queue == null) {
            this.eventBus.publish(new QueueUpdateGroupEvent(responseType, group, null));
            this.eventBus.publish(new SQueueUpdateGroupEvent(responseType, group, null));
        } else if (queue instanceof final NormalQueue normalQueue) {
            this.eventBus.publish(new QueueUpdateGroupEvent(responseType, group, normalQueue.getInfo()));
        } else if (queue instanceof final HostQueue hostQueue) {
            this.eventBus.publish(new SQueueUpdateGroupEvent(responseType, group, hostQueue.getInfo()));
        }

    }

    private Queue getCurrentGroupQueue(UUID groupId) {
        for (Queue queue : this.queues.values()) {
            if (queue.containsGroup(groupId)) {
                return queue;
            }
        }
        return null;
    }

    private Queue getCurrentPlayerQueue(UUID playerId) {
        for (Queue queue : this.queues.values()) {
            if (queue.containsPlayer(playerId)) {
                return queue;
            }
        }
        return null;
    }

    private Queue getQueue(String game, String gameType, String map) {
        final String queueName = this.createQueueName(game, gameType, map);

        Queue queue = this.queues.get(queueName);

        if (queue == null) {
            queue = this.createQueue(game, gameType, map);
        }
        return queue;
    }

    private Queue getQueue(String serverName) {
        Queue queue = this.queues.get(serverName);

        if (queue == null) {
            queue = new HostQueue(new SQueueInfo(serverName));

            this.queues.put(serverName, queue);
        }
        return queue;
    }

    private NormalQueue createQueue(String game, String gameType, String map) {
        final NormalQueue queue = new NormalQueue(new QueueInfo(game, gameType, map, 0, 0));
        final String name = this.createQueueName(game, gameType, map);

        this.queues.put(name, queue);

        System.out.println("Created '" + name + "' queue.");

        return queue;
    }

    private String createQueueName(String game, String gameType, String map) {
        return map != null ? game + "@" + gameType + "@" + map : game + "@" + gameType;
    }

}
