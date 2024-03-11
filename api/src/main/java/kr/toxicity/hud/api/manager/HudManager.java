package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.hud.Hud;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public interface HudManager {
    @Nullable Hud getHud(@NotNull String name);
    @NotNull @Unmodifiable Set<String> getAllNames();
    @NotNull @Unmodifiable Set<Hud> getDefaultHuds();
}
