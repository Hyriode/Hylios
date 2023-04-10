package fr.hyriode.hylios.party;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.event.HyriEventHandler;
import fr.hyriode.api.party.IHyriParty;
import fr.hyriode.api.player.IHyriPlayerSession;
import fr.hyriode.api.player.event.PlayerJoinNetworkEvent;
import fr.hyriode.api.player.event.PlayerQuitNetworkEvent;
import fr.hyriode.api.scheduler.IHyriTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static fr.hyriode.api.party.IHyriParty.*;

/**
 * Created by AstFaster
 * on 19/12/2022 at 21:26
 */
public class PartyHandler {

    private final Map<UUID, DisconnectionData> disconnections = new HashMap<>();

    public PartyHandler() {
        HyriAPI.get().getNetworkManager().getEventBus().register(this);
    }

    @HyriEventHandler
    public void onJoin(PlayerJoinNetworkEvent event) {
        final UUID player = event.getPlayerId();
        final DisconnectionData data = this.disconnections.remove(player);

        if (data != null) {
            data.task().cancel();

            final IHyriPlayerSession session = IHyriPlayerSession.get(player);

            // Re-set the player party to his old one
            if (session != null) {
                session.setParty(data.party());
                session.update();
            }
        }
    }

    @HyriEventHandler
    public void onQuit(PlayerQuitNetworkEvent event) {
        final UUID partyId = event.getLastParty();
        final UUID player = event.getPlayerId();

        if (partyId == null) {
            return;
        }

        // Remove the player from his party 5 minutes after his disconnection
        this.disconnections.put(player, new DisconnectionData(partyId, HyriAPI.get().getScheduler().schedule(() -> {
            final IHyriParty party = get(partyId);

            if (party == null) {
                return;
            }

            if (party.isLeader(player)) {
                party.disband(DisbandReason.LEADER_DISCONNECT_TIMEOUT);
            } else {
                party.removeMember(player, RemoveReason.DISCONNECT_TIMEOUT);
            }

            this.disconnections.remove(player);
        }, 5, TimeUnit.MINUTES)));
    }

    private record DisconnectionData(UUID party, IHyriTask task) {}

}
