package fr.hyriode.hylios.balancing;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyggdrasil.api.proxy.HyggProxy;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 22/07/2022 at 13:25
 */
public class ProxyBalancer {

    public ProxyBalancer() {
        HyriAPI.get().getScheduler().schedule(this::process, 10, 10, TimeUnit.SECONDS);
    }

    private void process() {
        final Set<HyggProxy> proxies = HyriAPI.get().getProxyManager().getProxies();

        if (proxies.size() == 0) {
            this.startProxy();
            return;
        }

        int players = 0;
        for (HyggProxy proxy : proxies) {
            players += proxy.getPlayers().size();
        }

        final int currentProxies = proxies.size();
        final int neededProxies = (int) (Math.ceil((double) players * 1.5 / HyggProxy.MAX_PLAYERS));

        if (neededProxies > currentProxies) {
            for (int i = 0; i < neededProxies - currentProxies; i++) {
                this.startProxy();
            }
        }
    }

    private void startProxy() {
        HyriAPI.get().getProxyManager().createProxy(proxy -> System.out.println("Created '" + proxy.getName() + "'."));
    }

}
