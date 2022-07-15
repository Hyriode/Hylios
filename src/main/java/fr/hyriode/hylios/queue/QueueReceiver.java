package fr.hyriode.hylios.queue;

import fr.hyriode.api.packet.HyriPacket;
import fr.hyriode.api.packet.IHyriPacketReceiver;
import fr.hyriode.hyggdrasil.api.protocol.packet.HyggPacket;
import fr.hyriode.hyggdrasil.api.protocol.receiver.IHyggPacketReceiver;
import fr.hyriode.hyggdrasil.api.protocol.request.HyggRequestHeader;
import fr.hyriode.hyggdrasil.api.protocol.response.HyggResponse;
import fr.hyriode.hyggdrasil.api.protocol.response.IHyggResponse;
import fr.hyriode.hyggdrasil.api.queue.packet.group.HyggQueueAddGroupPacket;
import fr.hyriode.hyggdrasil.api.queue.packet.group.HyggQueueRemoveGroupPacket;
import fr.hyriode.hyggdrasil.api.queue.packet.group.HyggQueueUpdateGroupPacket;
import fr.hyriode.hyggdrasil.api.queue.packet.player.HyggQueueAddPlayerPacket;
import fr.hyriode.hyggdrasil.api.queue.packet.player.HyggQueueRemovePlayerPacket;
import fr.hyriode.hylios.api.queue.packet.group.QueueAddGroupPacket;
import fr.hyriode.hylios.api.queue.packet.group.QueueRemoveGroupPacket;
import fr.hyriode.hylios.api.queue.packet.group.QueueUpdateGroupPacket;
import fr.hyriode.hylios.api.queue.packet.player.QueueAddPlayerPacket;
import fr.hyriode.hylios.api.queue.packet.player.QueueRemovePlayerPacket;

/**
 * Project: Hyggdrasil
 * Created by AstFaster
 * on 16/04/2022 at 09:08
 */
public class QueueReceiver implements IHyriPacketReceiver {

    private final QueueManager queueManager;

    public QueueReceiver(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public void receive(String channel, HyriPacket packet) {
        if (packet instanceof final QueueAddPlayerPacket queuePacket) {
            this.queueManager.handlePacket(queuePacket);
        } else if (packet instanceof final QueueUpdateGroupPacket queuePacket) {
            this.queueManager.handlePacket(queuePacket);
        } else if (packet instanceof final QueueAddGroupPacket queuePacket) {
            this.queueManager.handlePacket(queuePacket);
        } else if (packet instanceof final QueueRemovePlayerPacket queuePacket) {
            this.queueManager.handlePacket(queuePacket);
        } else if (packet instanceof final QueueRemoveGroupPacket queuePacket) {
            this.queueManager.handlePacket(queuePacket);
        }
    }
}
