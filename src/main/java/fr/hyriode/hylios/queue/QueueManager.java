package fr.hyriode.hylios.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.event.IHyriEventBus;
import fr.hyriode.api.party.IHyriParty;
import fr.hyriode.hylios.api.queue.QueueAPI;
import fr.hyriode.hylios.api.queue.QueueGroup;
import fr.hyriode.hylios.api.queue.QueueInfo;
import fr.hyriode.hylios.api.queue.QueuePlayer;
import fr.hyriode.hylios.api.queue.event.QueueAddEvent;
import fr.hyriode.hylios.api.queue.event.QueueRemoveEvent;
import fr.hyriode.hylios.api.queue.event.QueueUpdateGroupEvent;
import fr.hyriode.hylios.api.queue.packet.QueueAddPacket;
import fr.hyriode.hylios.api.queue.packet.group.QueueAddGroupPacket;
import fr.hyriode.hylios.api.queue.packet.group.QueueRemoveGroupPacket;
import fr.hyriode.hylios.api.queue.packet.group.QueueUpdateGroupPacket;
import fr.hyriode.hylios.api.queue.packet.player.QueueAddPlayerPacket;
import fr.hyriode.hylios.api.queue.packet.player.QueueRemovePlayerPacket;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static fr.hyriode.hylios.api.queue.event.QueueGroupEvent.IType;
import static fr.hyriode.hylios.api.queue.event.QueueGroupEvent.Type;

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

        IType responseType = Type.OK;

        if (queue.equals(currentQueue)) {
            responseType = Type.ALREADY_IN;
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

        this.eventBus.publish(new QueueAddEvent(responseType, group, queue.getInfo()));
    }

    public void handlePacket(QueueRemovePlayerPacket packet) {
        final UUID playerId = packet.getPlayerId();
        final Queue queue = this.getCurrentPlayerQueue(playerId);
        final QueueGroup group = queue != null ? queue.getPlayerGroup(playerId) : null;

        IType responseType = Type.UNKNOWN;

        if (queue == null) {
            responseType = Type.NOT_IN_QUEUE;
        } else {
            if (queue.removePlayer(playerId)) {
                HyriAPI.get().getQueueManager().removePlayerQueue(playerId);

                responseType = Type.OK;
            }
        }

        this.eventBus.publish(new QueueRemoveEvent(responseType, group, queue != null ? queue.getInfo() : null));
    }

    public void handlePacket(QueueRemoveGroupPacket packet) {
        final UUID groupId = packet.getGroupId();
        final Queue queue = this.getCurrentGroupQueue(groupId);
        final QueueGroup group = queue != null ? queue.getGroup(groupId) : null;

        IType responseType = Type.UNKNOWN;

        if (queue == null) {
            responseType = Type.NOT_IN_QUEUE;
        } else {
            if (queue.removeGroup(groupId)) {
                final IHyriParty party = HyriAPI.get().getPartyManager().getParty(group.getId());

                if (party != null) {
                    HyriAPI.get().getQueueManager().removePartyQueue(groupId);
                }

                for (QueuePlayer member : group.getPlayers()) {
                    HyriAPI.get().getQueueManager().removePlayerQueue(member.getUniqueId());
                }

                responseType = Type.OK;
            }
        }

        this.eventBus.publish(new QueueRemoveEvent(responseType, group, queue != null ? queue.getInfo() : null));
    }

    public void handlePacket(QueueUpdateGroupPacket packet) {
        final QueueGroup group = packet.getGroup();
        final UUID groupId = group.getId();
        final Queue queue = this.getCurrentGroupQueue(groupId);

        IType responseType = Type.OK;

        if (queue == null) {
            responseType = Type.NOT_IN_QUEUE;
        } else {
            queue.getGroup(groupId).update(packet);
        }

        this.eventBus.publish(new QueueUpdateGroupEvent(responseType, group, queue != null ? queue.getInfo() : null));
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

    private Queue createQueue(String game, String gameType, String map) {
        final Queue queue = new Queue(new QueueInfo(game, gameType, map, 0, 0));
        final String name = this.createQueueName(game, gameType, map);

        this.queues.put(name, queue);

        System.out.println("Created '" + name + "' queue.");

        return queue;
    }

    private String createQueueName(String game, String gameType, String map) {
        return map != null ? game + "@" + gameType + "@" + map : game + "@" + gameType;
    }

}
