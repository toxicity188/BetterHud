package kr.toxicity.hud.api.fabric.event;

import org.jetbrains.annotations.NotNull;

public interface FabricEvent<T extends FabricEvent<?>> {
    @NotNull EventRegistry<T> getRegistry();
}
