package fr.hyriode.hylios.api.queue;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.packet.HyriPacket;

/**
 * Created by AstFaster
 * on 15/07/2022 at 14:28
 */
public class QueueAPI {

    /** The channel used to communicate with queue system */
    public static final String CHANNEL = "hylios@queue";

    /**
     * Send a packet to the queue system
     *
     * @param packet The packet to send
     */
    public void sendPacket(HyriPacket packet) {
        HyriAPI.get().getPubSub().send(CHANNEL, packet);
    }

}
