package kr.toxicity.hud.api.player;

import kr.toxicity.hud.api.component.WidthComponent;
import kr.toxicity.hud.api.hud.Hud;
import kr.toxicity.hud.api.popup.Popup;
import kr.toxicity.hud.api.popup.PopupIteratorGroup;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface HudPlayer {
    @NotNull Player getBukkitPlayer();
    @NotNull WidthComponent getHudComponent();
    void setAdditionalComponent(@Nullable WidthComponent component);
    @Nullable WidthComponent getAdditionalComponent();
    long getTick();
    void cancel();
    @NotNull Map<String, PopupIteratorGroup> getPopupGroupIteratorMap();
    @NotNull Map<String, String> getVariableMap();
    @Nullable BarColor getBarColor();

    @NotNull Set<Popup> getPopups();
    @NotNull Set<Hud> getHuds();
    void setBarColor(@Nullable BarColor color);
}
