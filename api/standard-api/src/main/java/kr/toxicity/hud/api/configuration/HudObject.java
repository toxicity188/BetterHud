package kr.toxicity.hud.api.configuration;

import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An element that can be display for BetterHud
 */
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

    /**
     * Gets object's frame time
     * @return frame
     */
    long tick();

    /**
     * Gets rendered component of this object.
     * @param player target player
     * @return component supplier
     */
    @ApiStatus.Internal
    @NotNull
    default HudComponentSupplier<?> getComponentsByType(@NotNull HudPlayer player) {
        return getType().invoke(this, player);
    }

    /**
     * Creates object identifier
     * @return identifier
     */
    @ApiStatus.Internal
    default @NotNull Identifier identifier() {
        return new Identifier(this);
    }

    /**
     * Adds this object to player.
     * @param player target player
     * @return whether to success or not
     */
    default boolean add(@NotNull HudPlayer player) {
        var objects = player.getHudObjects();
        return objects.putIfAbsent(identifier(), getComponentsByType(player)) == null;
    }
    /**
     * Removes this object to player.
     * @param player target player
     * @return whether to success or not
     */
    default boolean remove(@NotNull HudPlayer player) {
        var objects = player.getHudObjects();
        return objects.remove(identifier()) != null;
    }

    /**
     * Identifier
     * @param source source
     */
    record Identifier(@NotNull HudObject source) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Identifier(HudObject source1))) return false;
            return source.getClass() == source1.getClass() && Objects.equals(source.getName(), source1.getName());
        }

        @Override
        public int hashCode() {
            return source.getName().hashCode();
        }
    }
}
