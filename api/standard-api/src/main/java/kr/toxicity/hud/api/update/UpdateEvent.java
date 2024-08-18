package kr.toxicity.hud.api.update;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents the update event of popup.
 */
public interface UpdateEvent {
    /**
     * Empty event.
     */
    UpdateEvent EMPTY = new UpdateEvent() {
        @Override
        public @NotNull UpdateReason getType() {
            return UpdateReason.EMPTY;
        }

        @Override
        public @NotNull Object getKey() {
            return UUID.randomUUID();
        }
    };

    /**
     * Returns update reason of event.
     * @return update event
     */
    @NotNull UpdateReason getType();

    /**
     * Returns the key. normally random uuid.
     * @return unique key
     */
    @NotNull Object getKey();
}
