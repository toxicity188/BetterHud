package kr.toxicity.hud.util

import kr.toxicity.hud.api.yaml.YamlElement
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.placeholder.ColorOverride
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.placeholder.Conditions
import kr.toxicity.hud.placeholder.PlaceholderSource
import kr.toxicity.hud.yaml.YamlArrayImpl
import kr.toxicity.hud.yaml.YamlElementImpl
import kr.toxicity.hud.yaml.YamlObjectImpl
import net.kyori.adventure.audience.Audience
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream

private val YAML = Yaml()

fun Any.toYaml(path: String): YamlElement = when (this) {
    is Map<* ,*> -> YamlObjectImpl(path, LinkedHashMap<String, Any>().also {
        entries.forEach { e ->
            it[(e.key ?: return@forEach).toString()] = e.value ?: return@forEach
        }
    })
    is List<*> -> YamlArrayImpl(path, this)
    else -> YamlElementImpl(path, this)
}

fun File.toYaml(): YamlObject = inputStream().buffered().use {
    YamlObjectImpl(
        "",
        YAML.load(it) ?: mutableMapOf<String, Any>()
    )
}

fun InputStream.toYaml() = YamlObjectImpl(
    "",
    YAML.load(this) ?: mutableMapOf<String, Any>()
)

fun Map<String, Any>.saveToYaml(file: File) {
    file.bufferedWriter().use {
        it.write(YAML.dumpAsMap(this))
    }
}


fun File.forEachAllYaml(sender: Audience, block: (File, String, YamlObject) -> Unit) {
    forEachAllFolder {
        if (it.extension == "yml") {
            runWithExceptionHandling(sender, "Unable to load this yml file: ${it.name}") {
                it.toYaml().forEach { e ->
                    val v = e.value
                    if (v is YamlObject) block(it, e.key, v)
                }
            }
        } else {
            sender.warn("This is not a yml file: ${it.path}")
        }
    }
}

fun YamlObject.toConditions(source: PlaceholderSource) = get("conditions")?.asObject()?.let {
    Conditions.parse(it, source)
} ?: ConditionBuilder.alwaysTrue
fun YamlObject.toColorOverrides(source: PlaceholderSource) = get("color-overrides")?.asObject()?.let {
    ColorOverride.builder(it, source)
} ?: ColorOverride.empty

fun YamlObject.getTEquation(key: String) = get(key)?.asString()?.let {
    TEquation(it)
}