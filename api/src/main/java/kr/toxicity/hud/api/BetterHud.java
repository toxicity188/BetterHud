package kr.toxicity.hud.api;

import kr.toxicity.hud.api.bedrock.BedrockAdapter;
import kr.toxicity.hud.api.manager.*;
import kr.toxicity.hud.api.nms.NMS;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.plugin.ReloadResult;
import kr.toxicity.hud.api.scheduler.HudScheduler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents the main class of BetterHud.
 */
public abstract class BetterHud extends JavaPlugin {
    private static BetterHud instance; // static

    public static final String DEFAULT_NAMESPACE = "betterhud";
    public static final String ADVENTURE_VERSION = "4.17.0";
    public static final String PLATFORM_VERSION = "4.3.3";
    public static final String EXAMINATION_VERSION = "1.3.0";

    @Override
    public final void onLoad() { // Do not use this
        if (instance != null) throw new RuntimeException();
        instance = this;
    }

    /**
     * Gets a main instance of BetterHud.
     * @return BetterHud
     */
    public static @NotNull BetterHud getInstance() {
        return Objects.requireNonNull(instance);
    }

    /**
     * Gets a nms.
     * @return nms
     */
    public abstract @NotNull NMS getNMS();

    /**
     * Executes reload.
     * @return result of reload.
     */
    public final @NotNull ReloadResult reload() {
        return reload(getAudiences().sender(Bukkit.getConsoleSender()));
    }
    /**
     * Executes reload.
     * @param sender log handler.
     * @return result of reload.
     */
    public abstract @NotNull ReloadResult reload(@NotNull Audience sender);

    /**
     * Gets bukkit audiences.
     * @return audiences
     */
    public abstract @NotNull BukkitAudiences getAudiences();

    /**
     * Gets wrapped scheduler.
     * @return scheduler
     */
    public abstract @NotNull HudScheduler getScheduler();

    /**
     * Gets a bedrock adapter.
     * @return adapter
     */
    public abstract @NotNull BedrockAdapter getBedrockAdapter();

    /**
     * Returns whether this bukkit is Folia or that's fork.
     * @return whether this bukkit is Folia
     */
    public abstract boolean isFolia();

    /**
     * Returns whether this bukkit is Paper or that's fork.
     * @return whether this bukkit is Paper
     */
    public abstract boolean isPaper();

    /**
     * Returns whether this plugin merges the first bossbar.
     * @return whether to merge
     */
    public abstract boolean isMergeBossBar();

    /**
     * Loads plugin's resource to some directory
     * @param prefix resource folder
     * @param dir target directory
     */
    public abstract void loadAssets(@NotNull String prefix, @NotNull File dir);
    /**
     * Loads plugin's resource to some directory
     * @param prefix resource folder
     * @param consumer for each callback
     */
    public abstract void loadAssets(@NotNull String prefix, @NotNull BiConsumer<String, InputStream> consumer);

    /**
     * Gets a width of default font's char
     * @param codepoint target codepoint
     * @return width
     */
    public abstract int getWidth(int codepoint);

    /**
     * Gets placeholder manager.
     * @return placeholder manager
     */
    public abstract @NotNull PlaceholderManager getPlaceholderManager();
    /**
     * Gets listener manager.
     * @return listener manager
     */
    public abstract @NotNull ListenerManager getListenerManager();
    /**
     * Gets popup manager.
     * @return popup manager
     */
    public abstract @NotNull PopupManager getPopupManager();
    /**
     * Gets config manager.
     * @return config manager
     */
    public abstract @NotNull ConfigManager getConfigManager();
    /**
     * Gets compass manager.
     * @return compass manager
     */
    public abstract @NotNull CompassManager getCompassManager();
    /**
     * Gets trigger manager.
     * @return trigger manager
     */
    public abstract @NotNull TriggerManager getTriggerManager();
    /**
     * Gets hud manager.
     * @return hud manager
     */
    public abstract @NotNull HudManager getHudManager();
    /**
     * Gets database manager.
     * @return database manager
     */
    public abstract @NotNull DatabaseManager getDatabaseManager();
    /**
     * Gets shader manager.
     * @return shader manager
     */
    public abstract @NotNull ShaderManager getShaderManager();
    /**
     * Gets player manager.
     * @return player manager
     */
    public abstract @NotNull PlayerManager getPlayerManager();

    /**
     * Returns this plugin is currently on reload.
     * @return whether to this plugin is on reload
     */
    public abstract boolean isOnReload();

    /**
     * Returns encoded namespace.
     * @return namespace.
     */
    public abstract @NotNull String getEncodedNamespace();

    /**
     * Returns default font key.
     * @return font key
     */
    public abstract @NotNull Key getDefaultKey();


    /**
     * Get a translated result of key.
     * @param locale locale
     * @param key key
     * @return translated value
     */
    public abstract @Nullable String translate(@NotNull String locale, @NotNull String key);

    /**
     * Gets the player's data from bukkit player.
     * @param player target player
     * @return player's data
     */
    @NotNull
    public final HudPlayer getHudPlayer(@NotNull Player player) {
        return getPlayerManager().getHudPlayer(player);
    }
}
