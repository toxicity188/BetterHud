package kr.toxicity.hud.api.trgger;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Function;

public interface HudTrigger<T> {
    @NotNull Function<? super T, UUID> getValueMapper();
    @NotNull Function<? super T, UUID> getKeyMapper();
}
