package kr.toxicity.hud.api;

import kr.toxicity.hud.api.manager.*;
import kr.toxicity.hud.api.plugin.ReloadResult;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents the main class of BetterHud.
 */
public interface BetterHud {

    String DEFAULT_NAMESPACE = "betterhud";
    String ADVENTURE_VERSION = "4.17.0";
    String PLATFORM_VERSION = "4.3.4";
    String EXAMINATION_VERSION = "1.3.0";

    int BSTATS_ID_BUKKIT = 21287;
    int BSTATS_ID_VELOCITY = 23460;


    /**
     * Supports legacy api.
     * @return BetterHud main instance.
     */
    static @NotNull BetterHud getInstance() {
        return BetterHudAPI.inst();
    }

    /**
     * Executes reload.
     * @return result of reload.
     */
    default @NotNull ReloadResult reload() {
        return reload(bootstrap().console());
    }

    /**
     * Gets a bootstrap.
     * @return bootstrap.
     */
    @NotNull BetterHudBootstrap bootstrap();

    /**
     * Executes reload.
     * @param sender log handler.
     * @return result of reload.
     */
    @NotNull ReloadResult reload(@NotNull Audience sender);


    /**
     * Returns whether this plugin merges the first bossbar.
     * @return whether to merge
     */
    boolean isMergeBossBar();

    /**
     * Loads plugin's resource to some directory
     * @param prefix resource folder
     * @param dir target directory
     */
    void loadAssets(@NotNull String prefix, @NotNull File dir);
    /**
     * Loads plugin's resource to some directory
     * @param prefix resource folder
     * @param consumer for each callback
     */
    void loadAssets(@NotNull String prefix, @NotNull BiConsumer<String, InputStream> consumer);

    /**
     * Gets a width of default font's char
     * @param codepoint target codepoint
     * @return width
     */
    int getWidth(int codepoint);

    /**
     * Gets placeholder manager.
     * @return placeholder manager
     */
    @NotNull PlaceholderManager getPlaceholderManager();
    /**
     * Gets listener manager.
     * @return listener manager
     */
    @NotNull ListenerManager getListenerManager();
    /**
     * Gets popup manager.
     * @return popup manager
     */
    @NotNull PopupManager getPopupManager();
    /**
     * Gets config manager.
     * @return config manager
     */
    @NotNull ConfigManager getConfigManager();
    /**
     * Gets compass manager.
     * @return compass manager
     */
    @NotNull CompassManager getCompassManager();
    /**
     * Gets trigger manager.
     * @return trigger manager
     */
    @NotNull TriggerManager getTriggerManager();
    /**
     * Gets hud manager.
     * @return hud manager
     */
    @NotNull HudManager getHudManager();
    /**
     * Gets database manager.
     * @return database manager
     */
    @NotNull DatabaseManager getDatabaseManager();
    /**
     * Gets shader manager.
     * @return shader manager
     */
    @NotNull ShaderManager getShaderManager();
    /**
     * Gets player manager.
     * @return player manager
     */
    @NotNull PlayerManager getPlayerManager();
    /**
     * Gets text manager.
     * @return text manager
     */
    @NotNull TextManager getTextManager();

    /**
     * Returns this plugin is currently on reload.
     * @return whether to this plugin is on reload
     */
    boolean isOnReload();

    /**
     * Returns encoded namespace.
     * @return namespace.
     */
    @NotNull String getEncodedNamespace();

    /**
     * Returns default font key.
     * @return font key
     */
    @NotNull Key getDefaultKey();


    /**
     * Get a translated result of key.
     * @param locale locale
     * @param key key
     * @return translated value
     */
    @Nullable String translate(@NotNull String locale, @NotNull String key);

    /**
     * Adds some task when reload is started.
     * @param runnable reload start task
     */
    void addReloadStartTask(@NotNull Runnable runnable);

    /**
     * Adds some task when reload is ended.
     * @param runnable reload end task
     */
    void addReloadEndTask(@NotNull Consumer<ReloadResult> runnable);
}
