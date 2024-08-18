package kr.toxicity.hud.yaml

import kr.toxicity.hud.api.yaml.YamlElement
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.util.saveToYaml
import kr.toxicity.hud.util.toYaml
import java.io.File
import java.util.Collections
import java.util.LinkedHashMap

class YamlObjectImpl(
    path: String,
    private val map: MutableMap<*, *>
): YamlConfigurationImpl(path), YamlObject {

    override fun save(file: File) {
        get().saveToYaml(file)
    }

    @Suppress("UNCHECKED_CAST")
    override fun merge(`object`: YamlObject) {
        val m = map as MutableMap<String, Any>
        `object`.get().forEach {
            m.compute(it.key) { _, v ->
                v ?: it.value
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun put(key: String, value: Any) {
        (map as MutableMap<String, Any>)[key] = value
    }

    override fun get(path: String): YamlElement? {
        var subMap = map
        val split = path.split('.')
        split.forEachIndexed { i, s ->
            val get = subMap[s]
            if (get is MutableMap<*, *> && i < split.lastIndex) {
                subMap = get
            } else if (get != null) {
                val t = split.subList(0, i + 1).joinToString(".")
                return get.toYaml(if (path.isEmpty()) t else "$path.$t")
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(): MutableMap<String, Any> = Collections.unmodifiableMap(map as MutableMap<String, Any>)

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, YamlElement>> {
        val returnMap = LinkedHashMap<String, YamlElement>()
        map.forEach {
            it.value?.let { value ->
                returnMap[it.key.toString()] = value.toYaml(path())
            }
        }
        return returnMap.iterator()
    }
}