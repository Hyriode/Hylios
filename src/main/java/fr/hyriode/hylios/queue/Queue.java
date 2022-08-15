package fr.hyriode.hylios.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.event.IHyriEventBus;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hylios.api.queue.QueueGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by AstFaster
 * on 30/07/2022 at 23:26
 */
public abstract class Queue {

    protected final QueueHandler handler;
    protected final IHyriEventBus eventBus;

    public Queue() {
        this.handler = new QueueHandler();
        this.eventBus = HyriAPI.get().getNetworkManager().getEventBus();
    }

    public abstract void sendInfo();

    public abstract void disable();

    protected void drainGroups(HyggServer server) {
        final List<QueueGroup> groups = new ArrayList<>();

        this.handler.drainGroups(groups, server.getSlots() - server.getPlayingPlayers().size());

        for (QueueGroup group : groups) {
            if (this.handler.remove(group)) {
                group.send(server);
            }
        }
    }

    public boolean addGroup(QueueGroup group) {
        final boolean result = this.handler.add(group);

        this.sendInfo();

        return result;
    }

    public boolean removeGroup(QueueGroup group) {
        final boolean result = this.handler.remove(group);

        this.sendInfo();

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

}
