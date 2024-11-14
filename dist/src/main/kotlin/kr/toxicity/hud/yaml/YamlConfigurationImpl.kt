package kr.toxicity.hud.yaml

import kr.toxicity.hud.api.yaml.YamlConfiguration

abstract class YamlConfigurationImpl(
    private val path: String,
) : YamlConfiguration {
    override fun path(): String = path
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as YamlConfigurationImpl

        return path == other.path
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}