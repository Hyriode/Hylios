package fr.hyriode.hylios.world;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.world.IHyriWorld;
import fr.hyriode.api.world.generation.IWorldGenerationAPI;
import fr.hyriode.api.world.generation.WorldGenerationData;
import fr.hyriode.api.world.generation.WorldGenerationType;
import fr.hyriode.hyggdrasil.api.protocol.data.HyggData;
import fr.hyriode.hyggdrasil.api.server.HyggServer;
import fr.hyriode.hyggdrasil.api.server.HyggServerCreationInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 26/10/2022 at 09:45
 */
public class WorldGenerationHandler {

    public static final int MIN_WORLDS = 500;

    public WorldGenerationHandler() {
        HyriAPI.get().getScheduler().schedule(this::process, 0, 60, TimeUnit.MINUTES);
    }

    private void process() {
        if (HyriAPI.get().getNetworkManager().getNetwork().getPlayerCounter().getPlayers() > 50) {
            return;
        }

        for (WorldGenerationType type : WorldGenerationType.values()) {
            final List<IHyriWorld> worlds = HyriAPI.get().getWorldGenerationAPI().getWorlds(type);

            if (worlds.size() >= MIN_WORLDS) {
                continue;
            }

            final int neededWorlds = MIN_WORLDS - worlds.size();
            final int neededServers = (int) Math.ceil((double) neededWorlds / 15);

            System.out.println("Starting " + neededServers + " generation servers (type: " + type + "; worlds: " + neededWorlds + ")...");

            int remainingWorlds = neededWorlds;

            for (int i = 0; i < neededServers; i++) {
                final HyggData data = new HyggData();

                data.add(IWorldGenerationAPI.DATA_KEY, HyriAPI.GSON.toJson(new WorldGenerationData(type, Math.min(remainingWorlds, 15))));

                final HyggServerCreationInfo request = new HyggServerCreationInfo(IWorldGenerationAPI.SERVERS_TYPE)
                        .withAccessibility(HyggServer.Accessibility.PUBLIC)
                        .withProcess(HyggServer.Process.TEMPORARY)
                        .withData(data);

                HyriAPI.get().getServerManager().createServer(request, null);

                remainingWorlds -= 15;
            }
        }
    }

}
