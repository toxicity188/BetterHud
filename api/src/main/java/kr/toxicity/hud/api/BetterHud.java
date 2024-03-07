package kr.toxicity.hud.api;

import kr.toxicity.hud.api.manager.ListenerManager;
import kr.toxicity.hud.api.manager.PlaceholderManager;
import kr.toxicity.hud.api.manager.PopupManager;
import kr.toxicity.hud.api.manager.TriggerManager;
import kr.toxicity.hud.api.nms.NMS;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.plugin.ReloadResult;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public abstract class BetterHud extends JavaPlugin {
    private static BetterHud instance;

    public static final String DEFAULT_NAMESPACE = "betterhud";


    @Override
    public void onLoad() {
        if (instance != null) throw new RuntimeException();
        instance = this;
    }
    public static @NotNull BetterHud getInstance() {
        return Objects.requireNonNull(instance);
    }

    public abstract @NotNull NMS getNMS();
    public abstract @NotNull ReloadResult reload();
    public abstract @NotNull BukkitAudiences getAudiences();
    public abstract void loadAssets(@NotNull String prefix, @NotNull File dir);
    public abstract int getWidth(char target);
    public abstract @NotNull HudPlayer getHudPlayer(@NotNull Player player);

    public abstract @NotNull PlaceholderManager getPlaceholderManager();
    public abstract @NotNull ListenerManager getListenerManager();
    public abstract @NotNull PopupManager getPopupManager();
    public abstract @NotNull TriggerManager getTriggerManager();
    public abstract boolean isOnReload();
}
