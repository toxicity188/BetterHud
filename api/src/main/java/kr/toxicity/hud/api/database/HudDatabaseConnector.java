package kr.toxicity.hud.api.database;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public interface HudDatabaseConnector {
    @NotNull HudDatabase connect(@NotNull ConfigurationSection section);
}
