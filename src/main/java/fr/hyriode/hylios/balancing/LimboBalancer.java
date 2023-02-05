package fr.hyriode.hylios.balancing;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyggdrasil.api.limbo.HyggLimbo;
import fr.hyriode.hyggdrasil.api.protocol.data.HyggData;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hylios.Hylios;
import fr.hyriode.hylios.util.HyliosException;

import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by AstFaster
 * on 15/07/2022 at 12:01
 */
public class LimboBalancer {

    public LimboBalancer() {
        System.out.println("Starting limbos balancing tasks...");

        HyriAPI.get().getScheduler().schedule(this::process, 5, 60, TimeUnit.SECONDS);
    }

    private void process() {
        for (HyggLimbo.Type limboType : HyggLimbo.Type.values()) {
            final List<HyggLimbo> limbos = this.getWorkingLimbos(limboType);
            final int currentLimbos = limbos.size();

            int players = 0;
            for (HyggLimbo limbo : limbos) {
                players += limbo.getPlayers().size();
            }

            final int minLimbos = Hylios.get().getConfig().minLimbos();

            int neededLimbos = (int) Math.ceil((double) players * 1.5 / HyggLimbo.MAX_PLAYERS); // The "perfect" amount of limbos needed

            if (neededLimbos < minLimbos) {
                neededLimbos = minLimbos;
            }

            if (currentLimbos > neededLimbos) {
                limbos.sort(Comparator.comparingLong(HyggLimbo::getStartedTime)); // Compare limbos by their time started time: young servers are prioritized.

                for (int i = 0; i < currentLimbos - neededLimbos; i++) {
                    final HyggLimbo limbo = limbos.get(i);

                    this.evacuateLimbo(limbo); // Evacuate the limbo

                    try { // Wait 1s for proxies to evacuate players
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new HyliosException(e);
                    }

                    HyriAPI.get().getLimboManager().removeLimbo(limbo.getName(), null); // Kill limbo process
                }
            } else if (currentLimbos < neededLimbos) {
                for (int i = 0; i < neededLimbos - currentLimbos; i++) {
                    this.startLimbo(limboType, currentLimbos);
                }
            }
        }
    }

    private void evacuateLimbo(HyggLimbo limbo) {
        final List<HyggLimbo> limbos = this.getWorkingLimbos(limbo.getType());
        final Queue<UUID> players = new LinkedBlockingQueue<>(limbo.getPlayers()); // Create a queue of players to evacuate

        limbos.sort(Comparator.comparingInt(o -> o.getPlayers().size()));  // Sort limbos by lower to greater amount of players

        for (HyggLimbo server : limbos) {
            if (server.getName().equals(limbo.getName())) {
                continue;
            }

            for (int i = 0; i < HyggLimbo.MAX_PLAYERS - server.getPlayers().size(); i++) { // For-each the available slots of the limbo
                if (players.size() == 0) { // No more players to evacuate
                    return;
                }

                final UUID player = players.poll(); // Remove a player from the queue (declared as evacuated)

                HyriAPI.get().getLimboManager().sendPlayerToLimbo(player, server.getName());
            }
        }
    }

    private void startLimbo(HyggLimbo.Type limboType, int currentLimbos) {
        HyriAPI.get().getLimboManager().createLimbo(limboType, new HyggData(), limbo -> System.out.println("Started '" + limbo.getName() + "' (current: " + (currentLimbos) + ")."));
    }

    private List<HyggLimbo> getWorkingLimbos(HyggLimbo.Type type) {
        return HyriAPI.get().getLimboManager().getLimbos(type)
                .stream()
                .filter(limbo -> limbo.getState() == HyggLimbo.State.READY)
                .collect(Collectors.toList());
    }

}
