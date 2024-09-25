package kr.toxicity.hud.api.adapter;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper class of command source.
 */
public interface CommandSourceWrapper {

    /**
     * Gets command source as kyori class.
     * @return audience
     */
    @NotNull
    Audience audience();

    /**
     * Returns command source's type
     * @return type
     */
    @NotNull
    Type type();

    /**
     * Checks this command source has some permission.
     * @param perm target permission
     * @return has this permission or not
     */
    boolean hasPermission(@NotNull String perm);

    /**
     * Checks this command source is op.
     * @return is op or not
     */
    boolean isOp();

    /**
     * Type
     */
    enum Type {
        CONSOLE,
        PLAYER
    }
}
