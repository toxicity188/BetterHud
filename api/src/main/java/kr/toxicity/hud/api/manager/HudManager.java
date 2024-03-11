package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.hud.Hud;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HudManager {
    @Nullable Hud getHud(@NotNull String name);
}
