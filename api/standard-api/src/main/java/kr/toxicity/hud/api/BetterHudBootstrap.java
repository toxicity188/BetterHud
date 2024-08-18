package kr.toxicity.hud.api;

import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.scheduler.HudScheduler;
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public interface BetterHudBootstrap {
    @NotNull
    BetterHudLogger logger();
    @NotNull
    File dataFolder();
    @NotNull
    Audience console();
    @NotNull
    BetterHud core();
    @NotNull
    File jarFile();
    @NotNull
    String version();

    /**
     * Gets wrapped scheduler.
     * @return scheduler
     */
    @NotNull
    HudScheduler scheduler();
    @NotNull
    VolatileCodeHandler volatileCode();


    @Nullable
    InputStream resource(@NotNull String path);
    boolean isPaper();
    boolean isFolia();
    boolean isVelocity();
    boolean useLegacyFont();

    void startMetrics();
    void endMetrics();

    void sendResourcePack(@NotNull HudPlayer player);
    void sendResourcePack();

    @NotNull
    String minecraftVersion();
    int mcmetaVersion();
}
