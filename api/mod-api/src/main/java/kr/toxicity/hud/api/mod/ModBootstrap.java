package kr.toxicity.hud.api.mod;

import kr.toxicity.hud.api.BetterHudBootstrap;
import kr.toxicity.hud.api.mod.event.EventRegistry;
import kr.toxicity.hud.api.plugin.ReloadState;

/**
 * Welcome to BetterHud Mod API!
 */
public interface ModBootstrap extends BetterHudBootstrap {

    /**
     * BetterHud pre-reload event
     */
    EventRegistry<EventRegistry.Unit> PRE_RELOAD_EVENT = new EventRegistry<EventRegistry.Unit>()
            .register(u -> EventRegistry.clearAll());
    /**
     * BetterHud post-reload event
     */
    EventRegistry<ReloadState> POST_RELOAD_EVENT = new EventRegistry<>();

    @Override
    default boolean isFolia() {
        return false;
    }
    @Override
    default boolean isPaper() {
        return false;
    }
    @Override
    default boolean isVelocity() {
        return false;
    }
    @Override
    default boolean isFabric() {
        return true;
    }
}
