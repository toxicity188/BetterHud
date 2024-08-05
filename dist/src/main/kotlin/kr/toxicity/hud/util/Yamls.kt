package kr.toxicity.hud.util

import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.placeholder.Conditions
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

fun File.toYaml() = YamlConfiguration.loadConfiguration(this)
fun InputStream.toYaml() = InputStreamReader(this).buffered().use {
    YamlConfiguration.loadConfiguration(it)
}

fun ConfigurationSection.forEachSubConfiguration(block: (String, ConfigurationSection) -> Unit) {
    getKeys(false).forEach {
        getConfigurationSection(it)?.let { config ->
            block(it, config)
        } ?: warn("Invalid section format: $it")
    }
}

fun ConfigurationSection.toConditions() = getConfigurationSection("conditions")?.let {
    Conditions.parse(it)
} ?: ConditionBuilder.alwaysTrue

fun ConfigurationSection.getTEquation(name: String) = getString(name)?.toEquation()