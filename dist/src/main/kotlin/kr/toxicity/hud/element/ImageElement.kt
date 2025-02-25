package kr.toxicity.hud.element

import kr.toxicity.hud.api.yaml.YamlArray
import kr.toxicity.hud.api.yaml.YamlElement
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.image.NamedLoadedImage
import kr.toxicity.hud.image.enums.ImageType
import kr.toxicity.hud.manager.ImageManager
import kr.toxicity.hud.manager.ListenerManagerImpl
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.placeholder.Conditions
import kr.toxicity.hud.placeholder.ConditionSource
import kr.toxicity.hud.placeholder.PlaceholderSource
import kr.toxicity.hud.util.ifNull

class ImageElement(
    override val id: String,
    val image: List<NamedLoadedImage>,
    val type: ImageType,
    setting: YamlObject
) : HudElement, ConditionSource by ConditionSource.Impl(setting), PlaceholderSource by PlaceholderSource.Impl(setting) {
    val listener = setting["listener"]?.asObject()?.let {
        ListenerManagerImpl.getListener(it)
    }
    val scale = setting.getAsDouble("scale", 1.0).apply {
        if (this <= 0.0) throw RuntimeException("scale cannot be <= 0.0: $id")
    }

    private val childrenMap = when (val child = setting["children"]) {
        is YamlArray -> child.associate {
            it.asString().let { s -> s to s }
        }
        is YamlObject -> child.associate {
            it.key to it.value.asString()
        }
        is YamlElement -> child.asString().let {
            mapOf(it to it)
        }
        null -> emptyMap()
    }

    val children by lazy {
        fun String.toImage() = ImageManager.getImage(this).ifNull("This children image doesn't exist in $id: $this")
        when {
            childrenMap.isEmpty() -> emptyMap()
            childrenMap.size == 1 -> if (childrenMap.values.first() == "*") ImageManager.allImage.filter {
                it.id != id && !it.childrenMap.containsKey(id)
            }.associateBy {
                it.id
            } else childrenMap.entries.first().run {
                mapOf(key to value.toImage())
            }
            else -> childrenMap.entries.associate {
                it.key to it.value.toImage()
            }
        }
    }

    val follow = setting["follow"]?.asString()?.let {
        PlaceholderManagerImpl.find(it, this)
            .assertString("This placeholder is not a string in image $id: $it")
    }
    val childrenMapper = setting["children-mapper"]?.asObject()?.map {
        it.key to Conditions.parse(it.value.asObject(), this)
    }
}