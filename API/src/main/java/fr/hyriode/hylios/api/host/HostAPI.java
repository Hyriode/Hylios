package fr.hyriode.hylios.api.host;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyggdrasil.api.protocol.environment.HyggData;
import fr.hyriode.hyggdrasil.api.server.HyggServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AstFaster
 * on 29/07/2022 at 19:56
 */
public class HostAPI {

    public static final String CHANNEL = "hylios@host";
    public static final String DATA_KEY = "host";

    public HostData getHostData(HyggData serverData) {
        final String data = serverData.get(DATA_KEY);

        return data == null ? null : HyriAPI.GSON.fromJson(data, HostData.class);
    }

    public HostData getHostData(HyggServer server) {
        return this.getHostData(server.getData());
    }

    public boolean isHost(HyggServer server) {
        return server.getData().get(DATA_KEY) != null;
    }

    public List<HyggServer> getHosts() {
        final List<HyggServer> hosts = new ArrayList<>();

        for (HyggServer server : HyriAPI.get().getServerManager().getServers()) {
            if (this.isHost(server)) {
                hosts.add(server);
            }
        }
        return hosts;
    }

}
