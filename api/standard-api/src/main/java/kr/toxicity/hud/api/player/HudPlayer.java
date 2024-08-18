package kr.toxicity.hud.api.player;

import kr.toxicity.hud.api.adapter.CommandSourceWrapper;
import kr.toxicity.hud.api.adapter.LocationWrapper;
import kr.toxicity.hud.api.adapter.WorldWrapper;
import kr.toxicity.hud.api.compass.Compass;
import kr.toxicity.hud.api.component.WidthComponent;
import kr.toxicity.hud.api.configuration.HudObject;
import kr.toxicity.hud.api.hud.Hud;
import kr.toxicity.hud.api.popup.Popup;
import kr.toxicity.hud.api.popup.PopupIteratorGroup;
import kr.toxicity.hud.api.popup.PopupUpdater;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents HudPlayer data.
 */
public interface HudPlayer extends CommandSourceWrapper {
    @NotNull
    UUID uuid();
    @NotNull
    String name();
    @NotNull
    LocationWrapper location();
    @NotNull
    WorldWrapper world();

    /**
     * Gets HudPlayer head.
     * @return HudPlayer head
     */
    @NotNull HudPlayerHead getHead();
    /**
     * Gets original hudPlayer.
     * @return handle.
     */
    @NotNull
    Object handle();

    /**
     * Gets HudPlayer's last bar's component.
     * @return component
     */
    @NotNull WidthComponent getHudComponent();

    /**
     * Sets HudPlayer's additional component.
     * @param component component
     */
    void setAdditionalComponent(@Nullable WidthComponent component);

    /**
     * Gets HudPlayer's additional component.
     * @return component
     */
    @Nullable WidthComponent getAdditionalComponent();

    /**
     * Returns whether bossbar update is enabled.
     * @return whether to enable.
     */
    boolean isHudEnabled();

    /**
     * Sets whether to update bossbar.
     * @param toEnable whether to update
     */
    void setHudEnabled(boolean toEnable);

    /**
     * Gets HudPlayer's current tick
     * It represents the count of update() calling;
     * @return tick
     */
    long getTick();

    /**
     * Cancel and close HudPlayer's data.
     */
    void cancel();

    /**
     * Updates HudPlayer's bossbar.
     */
    void update();

    /**
     * Cancels and Starts HudPlayer's task.
     */
    void startTick();

    /**
     * Cancels HudPlayer's task.
     */
    void cancelTick();

    /**
     * Gets a mutable map of popup iterator.
     * @see PopupIteratorGroup
     * @return popup group's map
     */
    @NotNull Map<String, PopupIteratorGroup> getPopupGroupIteratorMap();

    /**
     * Gets a mutable map of HudPlayer's local variable.
     * @return HudPlayer's variable map
     */
    @NotNull Map<String, String> getVariableMap();

    /**
     * Gets a mutable map of popup updator.
     * @return popup updator map.
     */
    @NotNull Map<Object, PopupUpdater> getPopupKeyMap();

    /**
     * Gets a HudPlayer's current bossbar's color
     * @return bar color
     */
    @Nullable
    BossBar.Color getBarColor();

    /**
     * Gets a current HudPlayer's hud objects.
     * @return hud objects
     */
    @NotNull Set<HudObject> getHudObjects();

    /**
     * Gets a current HudPlayer's popup.
     * @return popups
     */
    default @NotNull Set<Popup> getPopups() {
        return getHudObjects().stream().map(o -> o instanceof Popup popup ? popup : null).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Gets a current HudPlayer's hud.
     * @return hud
     */
    default @NotNull Set<Hud> getHuds() {
        return getHudObjects().stream().map(o -> o instanceof Hud hud ? hud : null).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    /**
     * Gets a current HudPlayer's compass.
     * @return compass
     */
    default @NotNull Set<Compass> getCompasses() {
        return getHudObjects().stream().map(o -> o instanceof Compass compass ? compass : null).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Gets a pointed location.
     * @return location set
     */
    @NotNull Set<PointedLocation> getPointedLocation();

    /**
     * Resets all hud and popup.
     */
    void resetElements();

    /**
     * Save this data by current database.
     */
    void save();

    /**
     * Sets HudPlayer's bossbar color.
     * @param color bar color
     */
    void setBarColor(@Nullable BossBar.Color color);
}
