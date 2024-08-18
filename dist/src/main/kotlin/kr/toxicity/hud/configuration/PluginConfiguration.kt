package kr.toxicity.hud.configuration

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.util.*
import java.io.File

enum class PluginConfiguration(
    private val dir: String
) {
    CONFIG("config.yml"),
    DATABASE("database.yml"),
    FONT("font.yml"),
    SHADER("shader.yml")
    ;

    fun create(): YamlObject {
        val file = File(DATA_FOLDER, dir)
        val exists = file.exists()
        if (!exists) file.createNewFile()
        val yaml = file.toYaml()
        val newYaml = BOOTSTRAP.resource(dir)?.toYaml().ifNull("Resource '$dir' not found.")
        yaml.merge(newYaml)
        return yaml.apply {
            save(file)
        }
    }
}
