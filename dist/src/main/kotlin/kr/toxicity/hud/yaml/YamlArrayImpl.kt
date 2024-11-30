package kr.toxicity.hud.yaml

import kr.toxicity.hud.api.yaml.YamlArray
import kr.toxicity.hud.api.yaml.YamlElement
import kr.toxicity.hud.util.toYaml
import java.util.*

class YamlArrayImpl(
    path: String,
    private val list: List<*>
) : YamlConfigurationImpl(path), YamlArray {

    @Suppress("UNCHECKED_CAST")
    override fun get(): MutableList<Any> = Collections.unmodifiableList(list as MutableList<Any>)


    override fun iterator(): MutableIterator<YamlElement> {
        val returnList = ArrayList<YamlElement>()
        list.forEach {
            if (it != null) returnList += it.toYaml(path())
        }
        return returnList.iterator()
    }
}