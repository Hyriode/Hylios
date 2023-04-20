package fr.hyriode.hylios.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.packet.HyriChannel;
import fr.hyriode.api.player.IHyriPlayerSession;
import fr.hyriode.api.queue.IHyriQueue;
import fr.hyriode.api.queue.event.QueueDisabledEvent;
import fr.hyriode.api.queue.packet.JoinQueuePacket;
import fr.hyriode.api.queue.packet.LeaveQueuePacket;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static fr.hyriode.api.queue.IHyriQueue.Type;

/**
 * Created by AstFaster
 * on 16/04/2022 at 09:10
 */
public class QueueManager {

    private final Set<Queue> queues;

    public QueueManager() {
        this.queues = ConcurrentHashMap.newKeySet();

        HyriAPI.get().getPubSub().subscribe(HyriChannel.QUEUES, new QueueReceiver(this));

        // Delete old queues
        for (IHyriQueue queue : HyriAPI.get().getQueueManager().getQueues()) {
            for (UUID player : queue.getPlayers()) {
                final IHyriPlayerSession session = IHyriPlayerSession.get(player);

                if (session == null) {
                    continue;
                }

                session.setQueue(null);
                session.update();
            }

            HyriAPI.get().getNetworkManager().getEventBus().publish(new QueueDisabledEvent(queue));
            HyriAPI.get().getQueueManager().deleteQueue(queue.getId());
        }
    }

    public void disable() {
        for (Queue queue : this.queues) {
            queue.disable();
        }
    }

    void removeQueue(Queue queue) {
        this.queues.remove(queue);
    }

    public void onJoin(JoinQueuePacket packet) {
        final Type type = packet.getQueueType();
        final UUID player = packet.getPlayerId();
        final Queue playerQueue = this.getPlayerQueue(player);

        if (playerQueue != null) {
            playerQueue.removePlayer(player, false);
        }

        if (type == Type.GAME) {
            final String game = packet.getGame();
            final String gameType = packet.getGameType();
            final String map = packet.getMap();

            Queue gameQueue = this.getGameQueue(game, gameType, map);
            if (gameQueue == null) {
                gameQueue = new Queue(game, gameType, map);

                this.queues.add(gameQueue);
            }

            gameQueue.addPlayer(player);
        } else if (type == Type.SERVER) {
            final String server = packet.getServer();

            Queue serverQueue = this.getServerQueue(server);
            if (serverQueue == null) {
                serverQueue = new Queue(server);

                this.queues.add(serverQueue);
            }

            serverQueue.addPlayer(player);
        }
    }

    public void onLeave(LeaveQueuePacket packet) {
        final UUID player = packet.getPlayerId();
        final Queue playerQueue = this.getPlayerQueue(player);

        if (playerQueue == null) {
            return;
        }

        playerQueue.removePlayer(player, true);
    }

    private Queue getPlayerQueue(UUID playerId) {
        for (Queue queue : this.queues) {
            if (queue.containsPlayer(playerId)) {
                return queue;
            }
        }
        return null;
    }

    private Queue getServerQueue(String server) {
        for (Queue queue : this.queues) {
            final IHyriQueue handle = queue.getHandle();

            if (handle.getType() == Type.SERVER && handle.getServer() != null && handle.getServer().equals(server)) {
                return queue;
            }
        }
        return null;
    }

    public Queue getGameQueue(String game, String gameType, String map) {
        for (Queue queue : this.queues) {
            final IHyriQueue handle = queue.getHandle();

            if (handle.getType() == Type.GAME && handle.getGame().equals(game) && handle.getGameType().equals(gameType) && (map == null || Objects.equals(handle.getMap(), map))) {
                return queue;
            }
        }
        return null;
    }

    public Set<Queue> getQueues() {
        return this.queues;
    }

}
