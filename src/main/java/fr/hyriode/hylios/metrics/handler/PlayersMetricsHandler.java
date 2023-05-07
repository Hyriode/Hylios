package fr.hyriode.hylios.metrics.handler;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hylios.api.HyliosMetricsRedisKey;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;
import fr.hyriode.hylios.metrics.data.players.ConnectedPlayers;
import fr.hyriode.hylios.metrics.data.players.HyriPlusPlayers;
import fr.hyriode.hylios.metrics.data.players.RegisteredPlayers;
import fr.hyriode.hylios.metrics.processor.IMetricHandler;
import fr.hyriode.hylios.metrics.processor.IMetricProcessor;

import java.util.Set;

public class PlayersMetricsHandler implements IMetricHandler {

    private static final IMetricProcessor CONNECTED_PLAYERS = () -> new ConnectedPlayers(HyriAPI.get().getNetworkManager().getPlayerCounter().getPlayers());
    private static final IMetricProcessor HYRIPLUS_PLAYERS = () -> new HyriPlusPlayers(IMetricHandler.fetch(HyliosMetricsRedisKey.HYRI_PLUS));
    private static final IMetricProcessor REGISTERED_PLAYERS = () -> new RegisteredPlayers(IMetricHandler.fetch(HyliosMetricsRedisKey.REGISTERED_PLAYERS));

    @Override
    public Set<IHyreosMetric> process() {
        return Set.of(CONNECTED_PLAYERS.process(), HYRIPLUS_PLAYERS.process(), REGISTERED_PLAYERS.process());
    }
}
