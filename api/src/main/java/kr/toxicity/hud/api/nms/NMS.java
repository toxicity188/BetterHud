package kr.toxicity.hud.api.nms;

import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface NMS {
    void inject(@NotNull Player player, @NotNull BarColor color);
    void showBossBar(@NotNull Player player, @NotNull BarColor color, @NotNull Component component);
    void removeBossBar(@NotNull Player player);
    @NotNull Player getFoliaAdaptedPlayer(@NotNull Player player);
    @NotNull NMSVersion getVersion();
    @NotNull GameProfile getGameProfile(@NotNull Player player);
}
