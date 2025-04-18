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

        private static final UUID EMPTY = UUID.randomUUID();

        @Override
        public @NotNull UpdateReason getType() {
            return UpdateReason.EMPTY;
        }

        @Override
        public @NotNull Object getKey() {
            return EMPTY;
        }
    };

    /**
     * Returns update reason of event.
     * @return update event
     */
    @NotNull UpdateReason getType();

    /**
     * Returns the key. Normally random uuid.
     * @return unique key
     */
    @NotNull Object getKey();

    /**
     * Gets a source of this event.
     * You need to call this to get original event, not instance itself.
     * @return source
     */
    default @NotNull UpdateEvent source() {
        return this;
    }
}
