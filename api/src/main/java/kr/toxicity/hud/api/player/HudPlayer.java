package kr.toxicity.hud.api.player;

import kr.toxicity.hud.api.component.WidthComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HudPlayer {
    @NotNull Player getBukkitPlayer();
    @NotNull WidthComponent getHudComponent();
    void setAdditionalComponent(@Nullable WidthComponent component);
    @Nullable WidthComponent getAdditionalComponent();

    long getTick();
    void cancel();

    @Nullable BarColor getBarColor();
    void setBarColor(@Nullable BarColor color);
}
