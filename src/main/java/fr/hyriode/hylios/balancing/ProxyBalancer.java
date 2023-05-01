package fr.hyriode.hylios.balancing;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.proxy.event.ProxyRestartingEvent;
import fr.hyriode.hyggdrasil.api.event.model.HyggTemplateUpdatedEvent;
import fr.hyriode.hyggdrasil.api.proxy.HyggProxy;
import fr.hyriode.hylios.Hylios;

import java.util.Calendar;
import java.util.Collection;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 22/07/2022 at 13:25
 */
public class ProxyBalancer {

    private boolean templatedUpdated = false;

    public ProxyBalancer() {
        System.out.println("Starting proxies balancing tasks...");

        HyriAPI.get().getScheduler().schedule(this::process, 5, 30, TimeUnit.SECONDS);

        HyriAPI.get().getHyggdrasilManager().getHyggdrasilAPI().getEventBus().subscribe(HyggTemplateUpdatedEvent.class, event -> {
            if (event.getTemplate().equals("proxy")) {
                this.templatedUpdated = true;
            }
        });

        final Calendar calendar = Calendar.getInstance();

        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        calendar.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        HyriAPI.get().getScheduler().schedule(() -> {
            if (this.templatedUpdated) {
                final Set<HyggProxy> proxies = HyriAPI.get().getProxyManager().getProxies();

                for (int i = 0; i < proxies.size(); i++) {
                    this.startProxy(proxies.size());
                }

               HyriAPI.get().getScheduler().schedule(() -> {
                   for (HyggProxy proxy : proxies) {
                        HyriAPI.get().getNetworkManager().getEventBus().publish(new ProxyRestartingEvent(proxy.getName(), 60));
                        HyriAPI.get().getScheduler().schedule(() -> HyriAPI.get().getProxyManager().removeProxy(proxy.getName(), null), 60, TimeUnit.SECONDS);
                   }
               }, 10, TimeUnit.SECONDS);
            }
        }, calendar.getTimeInMillis() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    private void process() {
        final Set<HyggProxy> proxies = HyriAPI.get().getProxyManager().getProxies();
        final int currentProxies = proxies.size();
        final int minProxies = Hylios.get().getConfig().minProxies();

        int players = 0;
        for (HyggProxy proxy : proxies) {
            players += proxy.getPlayers().size();
        }

        int neededProxies = (int) (Math.ceil((double) players * 1.3 / HyggProxy.MAX_PLAYERS));

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
