package fr.hyriode.hylios.booster;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.booster.BoosterEnabledEvent;
import fr.hyriode.api.booster.BoosterQueuedEvent;
import fr.hyriode.api.booster.IHyriBooster;
import fr.hyriode.api.event.HyriEventHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created by AstFaster
 * on 20/04/2023 at 01:54
 */
public class BoosterHandler {

    public BoosterHandler() {
        HyriAPI.get().getNetworkManager().getEventBus().register(this);

        for (IHyriBooster booster : HyriAPI.get().getBoosterManager().getBoosters()) {
            if (booster.isEnabled()) {
                continue;
            }

            this.handleEnabledEvent(booster);
        }
    }

    private void handleEnabledEvent(IHyriBooster booster) {
        if (booster.isEnabled()) {
            HyriAPI.get().getNetworkManager().getEventBus().publish(new BoosterEnabledEvent(booster.getIdentifier()));
        } else {
            HyriAPI.get().getScheduler().schedule(() -> {
                final IHyriBooster updatedBooster = HyriAPI.get().getBoosterManager().getBooster(booster.getIdentifier());

                if (updatedBooster == null) {
                    return;
                }

                HyriAPI.get().getNetworkManager().getEventBus().publish(new BoosterEnabledEvent(updatedBooster.getIdentifier()));
            }, booster.getEnabledDate() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @HyriEventHandler
    public void onBoosterQueued(BoosterQueuedEvent event) {
        final IHyriBooster booster = event.getBooster();

        if (booster == null) {
            return;
        }

        this.handleEnabledEvent(booster);
    }

}
