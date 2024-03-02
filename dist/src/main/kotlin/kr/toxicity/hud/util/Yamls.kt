package kr.toxicity.hud.util

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

fun File.toYaml() = YamlConfiguration.loadConfiguration(this)

fun ConfigurationSection.forEachSubConfiguration(block: (String, ConfigurationSection) -> Unit) {
    getKeys(false).forEach {
        getConfigurationSection(it)?.let { config ->
            block(it, config)
        }
    }
}