package fr.hyriode.hylios.api.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hylios.api.queue.packet.group.QueueUpdateGroupPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by AstFaster
 * on 16/04/2022 at 09:19
 */
public class QueueGroup {

    private final UUID id;
    private QueuePlayer leader;
    private List<QueuePlayer> players;

    private int priority;

    public QueueGroup(UUID id, QueuePlayer leader, List<QueuePlayer> players) {
        this.id = id;
        this.leader = leader;
        this.players = players;
        this.priority = leader.getPriority();

        this.players.add(this.leader);

        this.calculatePriority();
    }

    public void update(QueueUpdateGroupPacket packet) {
        final QueueGroup group = packet.getGroup();

        this.leader = group.getLeader();
        this.players = group.getPlayers();

        this.calculatePriority();
    }

    public boolean contains(UUID playerId) {
        for (QueuePlayer player : this.players) {
            if (player.getUniqueId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(QueuePlayer player) {
        return this.players.contains(player);
    }

    public boolean addPlayer(QueuePlayer player) {
        if (this.contains(player)) {
            return false;
        }

        try {
            return this.players.contains(player);
        } finally {
            this.calculatePriority();
        }
    }

    public boolean removePlayer(UUID playerId) {
        final QueuePlayer player = this.getPlayer(playerId);

        return player != null && this.removePlayer(player);
    }

    public boolean removePlayer(QueuePlayer player) {
        if (this.leader != null && this.leader.getUniqueId().equals(player.getUniqueId())) {
            this.leader = null;
        }

        try {
            return this.players.remove(player);
        } finally {
            this.calculatePriority();
        }
    }

    public UUID getId() {
        return this.id;
    }

    public QueuePlayer getLeader() {
        return this.leader;
    }

    public QueuePlayer getPlayer(UUID uniqueId) {
        for (QueuePlayer player : this.players) {
            if (player.getUniqueId().equals(uniqueId)) {
                return player;
            }
        }
        return null;
    }

    public List<QueuePlayer> getPlayers() {
        return this.players;
    }

    public List<UUID> getPlayersIds() {
        final List<UUID> ids = new ArrayList<>();

        for (QueuePlayer player : this.players) {
            ids.add(player.getUniqueId());
        }
        return ids;
    }

    public int getSize() {
        return this.players.size();
    }

    public int getPriority() {
        return this.priority;
    }

    private void calculatePriority() {
        for (QueuePlayer player : this.players) {
            this.priority = Math.min(player.getPriority(), this.priority);
        }
    }

    public void send(HyggServer server) {
        for (QueuePlayer member : this.players) {
            HyriAPI.get().getQueueManager().removePlayerQueue(member.getUniqueId());
        }

        HyriAPI.get().getQueueManager().removePartyQueue(this.id);

        if (this.players.size() == 1) {
            final UUID leaderId = this.leader.getUniqueId();

            HyriAPI.get().getServerManager().sendPlayerToServer(leaderId, server.getName());
        } else {
            HyriAPI.get().getServerManager().sendPartyToServer(this.id, server.getName());
        }
    }

    @Override
    public QueueGroup clone() {
        return new QueueGroup(this.id, this.leader, this.players);
    }

}
