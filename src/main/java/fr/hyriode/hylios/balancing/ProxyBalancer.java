package fr.hyriode.hylios.balancing;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyggdrasil.api.proxy.HyggProxy;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 22/07/2022 at 13:25
 */
public class ProxyBalancer {

    public ProxyBalancer() {
        HyriAPI.get().getScheduler().schedule(this::process, 10, 5, TimeUnit.SECONDS);
    }

    private void process() {
        final Collection<HyggProxy> proxies = HyriAPI.get().getProxyManager().getProxies();

        int players = 0;
        for (HyggProxy proxy : proxies) {
            players += proxy.getPlayers();
        }

        final int currentProxies = proxies.size();
        final int neededProxies = currentProxies == 0 ? 1 : (int) (Math.ceil((double) players * 1.2 / HyggProxy.MAX_PLAYERS));

        if (neededProxies > currentProxies) {
            for (int i = 0; i < neededProxies - currentProxies; i++) {
                HyriAPI.get().getProxyManager().createProxy(proxy -> System.out.println("Created '" + proxy.getName() + "'."));
            }
        }
    }

}
