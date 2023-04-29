package fr.hyriode.hylios.balancing;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyggdrasil.api.proxy.HyggProxy;
import fr.hyriode.hylios.Hylios;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 22/07/2022 at 13:25
 */
public class ProxyBalancer {

    public ProxyBalancer() {
        System.out.println("Starting proxies balancing tasks...");

        HyriAPI.get().getScheduler().schedule(this::process, 5, 30, TimeUnit.SECONDS);
    }

    private void process() {
        final Set<HyggProxy> proxies = HyriAPI.get().getProxyManager().getProxies();
        final int currentProxies = proxies.size();
        final int minProxies = Hylios.get().getConfig().minProxies();

        int players = 0;
        for (HyggProxy proxy : proxies) {
            players += proxy.getPlayers().size();
        }

        int neededProxies = (int) (Math.ceil((double) players * 1.5 / HyggProxy.MAX_PLAYERS));

        System.out.println(neededProxies);

        if (neededProxies < minProxies) {
            neededProxies = minProxies;
        }

        if (neededProxies > currentProxies) {
            for (int i = 0; i < neededProxies - currentProxies; i++) {
                this.startProxy(currentProxies);
            }
        }
    }

    private void startProxy(int currentProxies) {
        HyriAPI.get().getProxyManager().createProxy(proxy -> System.out.println("Started '" + proxy.getName() + "' (current: " + currentProxies + ")."));
    }

}
