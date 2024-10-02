package kr.toxicity.hud.api.fabric;

import kr.toxicity.hud.api.BetterHudBootstrap;

public interface FabricBootstrap extends BetterHudBootstrap {
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
