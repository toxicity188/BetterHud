package kr.toxicity.hud.api.update;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface UpdateEvent {
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
    @NotNull UpdateReason getType();
    @NotNull Object getKey();
}
