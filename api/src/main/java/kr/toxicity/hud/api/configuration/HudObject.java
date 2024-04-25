package kr.toxicity.hud.api.configuration;

import kr.toxicity.hud.api.component.WidthComponent;
import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HudObject {
    /**
     * Gets an internal name of object.
     * @return id
     */
    @NotNull String getName();
    /**
     * Returns this object is default object or not.
     * @return whether to default
     */
    boolean isDefault();
    /**
     * Gets the type instance.
     * @return type instance
     */
    @NotNull HudObjectType<?> getType();

    @NotNull
    default List<WidthComponent> getComponentsByType(@NotNull HudPlayer player) {
        return getType().invoke(this, player);
    }
}
