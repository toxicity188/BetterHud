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
) : HudElement, ConditionSource by ConditionSource.Impl(setting).memoize(), PlaceholderSource by PlaceholderSource.Impl(setting) {
    val listener = setting["listener"]?.asObject()?.let {
        ListenerManagerImpl.getListener(it)
    }
    val scale = setting.getAsDouble("scale", 1.0).apply {
        if (this <= 0.0) throw RuntimeException("scale cannot be <= 0.0: $id")
    }

    private val childrenList = when (val child = setting["children"]) {
        is YamlArray -> child.map {
            it.asString()
        }
        is YamlElement -> listOf(child.asString())
        null -> emptyList()
    }

    val children by lazy {
        fun String.toImagePair() = this to ImageManager.getImage(this).ifNull("This children image doesn't exist in $id: $this")
        when {
            childrenList.isEmpty() -> emptyMap()
            childrenList.size == 1 -> if (childrenList[0] == "*") ImageManager.allImage.filter {
                it.id != id && !it.childrenList.contains(id)
            }.associateBy {
                it.id
            } else mapOf(childrenList[0].toImagePair())
            else -> childrenList.associate {
                it.toImagePair()
            }
        }
    }

    val follow = setting["follow"]?.asString()?.let {
        PlaceholderManagerImpl.find(it, this).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string in image $id: $it")
        }
    }
    val childrenMapper = setting["children-mapper"]?.asObject()?.map {
        it.key to Conditions.parse(it.value.asObject(), this)
    }
}