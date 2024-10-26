package kr.toxicity.hud.api;

import kr.toxicity.hud.api.adapter.WorldWrapper;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.scheduler.HudScheduler;
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.List;

public interface BetterHudBootstrap {
    /**
     * Gets a logger.
     * @return logger
     */
    @NotNull
    BetterHudLogger logger();

    /**
     * Gets data folder of BetterHud.
     * @return data folder
     */
    @NotNull
    File dataFolder();

    /**
     * Gets console.
     * @return console
     */
    @NotNull
    Audience console();

    /**
     * Gets a BetterHud's main instance.
     * @return BetterHud
     */
    @NotNull
    BetterHud core();

    /**
     * Gets the file of BetterHud's jar file.
     * @return jar
     */
    @NotNull
    File jarFile();

    /**
     * Gets a minecraft version of this platform.
     * @return platform version.
     */
    @NotNull
    String version();

    /**
     * Gets wrapped scheduler.
     * @return scheduler
     */
    @NotNull
    HudScheduler scheduler();

    /**
     * Gets a volatile code.
     * Bukkit - NMS
     * Velocity - Proxy
     * @return volatile code
     */
    @NotNull
    VolatileCodeHandler volatileCode();

    /**
     * Gets a resource of jar file.
     * @param path file name
     * @return jar file stream or null if not exists
     */
    @Nullable
    InputStream resource(@NotNull String path);

    @NotNull
    URLClassLoader loader();

    /**
     * Whether this platform is Paper or that's fork.
     * @return whether this platform is Paper
     */
    boolean isPaper();

    /**
     * Whether this platform is Folia or that's fork.
     * @return whether this platform is Folia
     */
    boolean isFolia();

    /**
     * Whether this platform is Velocity or that's fork.
     * @return whether this platform is Velocity
     */
    boolean isVelocity();
    /**
     * Whether this platform is Fabric
     * @return whether this platform is Fabric
     */
    boolean isFabric();

    /**
     * Whether to use legacy space font.
     * Bukkit - Under 1.19
     * @return Whether to use legacy font
     */
    boolean useLegacyFont();

    /**
     * Starts BStats metrics.
     */
    void startMetrics();

    /**
     * Ends BStats metrics.
     */
    void endMetrics();

    /**
     * Sends resource pack url packet to that player.
     * @param player target player
     */
    void sendResourcePack(@NotNull HudPlayer player);

    /**
     * Sends resource pack url packet to all player.
     */
    void sendResourcePack();

    /**
     * Target platform's minecraft protocol version.
     * @return version like "1.21.1"
     */
    @NotNull
    String minecraftVersion();

    /**
     * A resource pack version matched at target platform's minecraft.
     * @return resource pack version
     */
    int mcmetaVersion();

    /**
     * Finds some world by given name.
     * @param name world name
     * @return wrapper of world or null if not exists.
     */
    @Nullable WorldWrapper world(@NotNull String name);

    /**
     * Returns all world's wrapper.
     * @return a collection of all world.
     */
    @NotNull
    @Unmodifiable
    List<WorldWrapper> worlds();

    /**
     * Checks whether this build is dev version.
     * @return whether this build is dev version
     */
    default boolean isDevVersion() {
        return version().contains("DEV");
    }
}
