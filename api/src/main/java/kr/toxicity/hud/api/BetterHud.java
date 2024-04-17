package kr.toxicity.hud.api;

import kr.toxicity.hud.api.bedrock.BedrockAdapter;
import kr.toxicity.hud.api.manager.*;
import kr.toxicity.hud.api.nms.NMS;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.plugin.ReloadResult;
import kr.toxicity.hud.api.scheduler.HudScheduler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class BetterHud extends JavaPlugin {
    private static BetterHud instance;

    public static final String DEFAULT_NAMESPACE = "betterhud";
    public static final String ADVENTURE_VERSION = "4.16.0";
    public static final String PLATFORM_VERSION = "4.3.2";
    public static final String EXAMINATION_VERSION = "1.3.0";


    @Override
    public void onLoad() {
        if (instance != null) throw new RuntimeException();
        instance = this;
    }
    public static @NotNull BetterHud getInstance() {
        return Objects.requireNonNull(instance);
    }

    public abstract @NotNull NMS getNMS();
    public abstract void reload(@NotNull Consumer<ReloadResult> consumer);
    public abstract @NotNull BukkitAudiences getAudiences();
    public abstract @NotNull HudScheduler getScheduler();
    public abstract @NotNull BedrockAdapter getBedrockAdapter();
    public abstract boolean isFolia();
    public abstract boolean isPaper();
    public abstract boolean isMergeBossBar();
    public abstract void loadAssets(@NotNull String prefix, @NotNull File dir);
    public abstract int getWidth(char target);
    public abstract @NotNull HudPlayer getHudPlayer(@NotNull Player player);

    public abstract @NotNull PlaceholderManager getPlaceholderManager();
    public abstract @NotNull ListenerManager getListenerManager();
    public abstract @NotNull PopupManager getPopupManager();
    public abstract @NotNull TriggerManager getTriggerManager();
    public abstract @NotNull HudManager getHudManager();
    public abstract @NotNull DatabaseManager getDatabaseManager();
    public abstract boolean isOnReload();
}
