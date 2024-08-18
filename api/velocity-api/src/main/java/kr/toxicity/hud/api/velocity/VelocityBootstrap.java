package kr.toxicity.hud.api.velocity;

import kr.toxicity.hud.api.BetterHudBootstrap;

public interface VelocityBootstrap extends BetterHudBootstrap {
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
        return true;
    }
}
