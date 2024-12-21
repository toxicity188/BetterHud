package kr.toxicity.hud.api.configuration;

import kr.toxicity.hud.api.compass.Compass;
import kr.toxicity.hud.api.hud.Hud;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.popup.Popup;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * A type of HUD element
 * @see HudObject
 * @param clazz object's class
 * @param name object's name
 * @param function object's render function
 * @param <T> object type
 */
public record HudObjectType<T extends HudObject>(@NotNull Class<T> clazz, @NotNull String name, @NotNull BiFunction<T, HudPlayer, HudComponentSupplier<T>> function) {
    /**
     * Hud type
     * @see Hud
     */
    public static final HudObjectType<Hud> HUD = new HudObjectType<>(
            Hud.class,
            "hud",
            Hud::createRenderer
    );
    /**
     * Popup type
     * @see Popup
     */
    public static final HudObjectType<Popup> POPUP = new HudObjectType<>(
            Popup.class,
            "popup",
            (popup, player) -> {
                popup.show(UpdateEvent.EMPTY, player);
                return HudComponentSupplier.empty(popup);
            }
    );
    /**
     * Compass type
     * @see Compass
     */
    public static final HudObjectType<Compass> COMPASS = new HudObjectType<>(
            Compass.class,
            "compass",
            Compass::indicate
    );

    /**
     * Gets supplier from object
     * @param object target object
     * @param player target player
     * @return supplier
     */
    @ApiStatus.Internal
    public @NotNull HudComponentSupplier<?> invoke(@NotNull HudObject object, @NotNull HudPlayer player) {
        try {
            return function.apply(clazz.cast(object), player);
        } catch (Throwable e) {
            return HudComponentSupplier.empty(object);
        }
    }
}
