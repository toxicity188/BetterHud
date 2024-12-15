package kr.toxicity.hud.api.fabric;

import kr.toxicity.hud.api.BetterHudBootstrap;
import kr.toxicity.hud.api.fabric.event.EventRegistry;
import kr.toxicity.hud.api.plugin.ReloadState;

public interface FabricBootstrap extends BetterHudBootstrap {

    EventRegistry<EventRegistry.Unit> PRE_RELOAD_EVENT = new EventRegistry<EventRegistry.Unit>()
            .register(u -> EventRegistry.clearAll());
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
