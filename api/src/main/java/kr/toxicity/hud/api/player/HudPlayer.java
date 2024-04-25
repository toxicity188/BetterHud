package kr.toxicity.hud.api.player;

import kr.toxicity.hud.api.compass.Compass;
import kr.toxicity.hud.api.component.WidthComponent;
import kr.toxicity.hud.api.configuration.HudObject;
import kr.toxicity.hud.api.configuration.HudObjectType;
import kr.toxicity.hud.api.hud.Hud;
import kr.toxicity.hud.api.popup.Popup;
import kr.toxicity.hud.api.popup.PopupIteratorGroup;
import kr.toxicity.hud.api.popup.PopupUpdater;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents player data.
 */
public interface HudPlayer {
    /**
     * Gets player head.
     * @return player head
     */
    @NotNull HudPlayerHead getHead();
    /**
     * Gets original player.
     * @return bukkit player
     */
    @NotNull Player getBukkitPlayer();

    /**
     * Gets player's last bar's component.
     * @return component
     */
    @NotNull WidthComponent getHudComponent();

    /**
     * Sets player's additional component.
     * @param component
     */
    void setAdditionalComponent(@Nullable WidthComponent component);

    /**
     * Gets player's additional component.
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
     * Gets player's current tick
     * It represents the count of update() calling;
     * @return tick
     */
    long getTick();

    /**
     * Cancel and close player's data.
     */
    void cancel();

    /**
     * Updates player's bossbar.
     */
    void update();

    /**
     * Cancels and Starts player's task.
     */
    void startTick();

    /**
     * Cancels player's task.
     */
    void cancelTick();

    /**
     * Gets a mutable map of popup iterator.
     * @see PopupIteratorGroup
     * @return popup group's map
     */
    @NotNull Map<String, PopupIteratorGroup> getPopupGroupIteratorMap();

    /**
     * Gets a mutable map of player's local variable.
     * @return player's variable map
     */
    @NotNull Map<String, String> getVariableMap();

    /**
     * Gets a mutable map of popup updator.
     * @return popup updator map.
     */
    @NotNull Map<Object, PopupUpdater> getPopupKeyMap();

    /**
     * Gets a player's current bossbar's color
     * @return bar color
     */
    @Nullable BarColor getBarColor();

    /**
     * Gets a current player's hud objects.
     * @return hud objects
     */
    @NotNull Set<HudObject> getHudObjects();

    /**
     * Gets a current player's popup.
     * @return popups
     */
    default @NotNull Set<Popup> getPopups() {
        return getHudObjects().stream().map(o -> o instanceof Popup popup ? popup : null).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Gets a current player's hud.
     * @return hud
     */
    default @NotNull Set<Hud> getHuds() {
        return getHudObjects().stream().map(o -> o instanceof Hud hud ? hud : null).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    /**
     * Gets a current player's compass.
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
     * Sets player's bossbar color.
     * @param color bar color
     */
    void setBarColor(@Nullable BarColor color);
}
