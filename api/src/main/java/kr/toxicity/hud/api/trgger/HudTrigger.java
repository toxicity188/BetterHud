package kr.toxicity.hud.api.trgger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

public interface HudTrigger<T> {
    @Nullable UUID getValue(T t);
    @NotNull Object getKey(T t);
}
