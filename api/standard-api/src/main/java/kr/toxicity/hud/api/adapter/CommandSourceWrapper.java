package kr.toxicity.hud.api.adapter;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public interface CommandSourceWrapper {

    @NotNull
    Audience audience();
    @NotNull
    Type type();

    boolean hasPermission(@NotNull String perm);
    boolean isOp();

    enum Type {
        CONSOLE,
        PLAYER
    }
}
