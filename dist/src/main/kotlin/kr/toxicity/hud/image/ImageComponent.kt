package kr.toxicity.hud.image

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.element.ImageElement
import kr.toxicity.hud.util.applyColor
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.shadow
import net.kyori.adventure.text.format.TextColor

class ImageComponent(
    private val original: ImageElement,
    private val parent: ImageComponent?,
    val images: List<PixelComponent>,
    val children: Map<String, ImageComponent>,
) : Map<String, ImageComponent> by children {
    val type = original.type
    val listener = original.listener ?: HudListener.EMPTY.let {
        { _ ->
            it
        }
    }
    val max
        get(): Int = maxOf(images.maxOf {
            it.component.width
        }, values.maxOfOrNull {
            it.max
        } ?: 0)

    infix fun choose(index: Long) = original.animationType.choose(images, index)

    fun apply(shadow: Int, color: TextColor): ImageComponent = ImageComponent(
        original,
        parent,
        images.map {
            it.copy() shadow shadow applyColor color
        },
        entries.associate { (name, component) ->
            name to component.apply(shadow, color)
        }
    )

    private fun interface ImageMapper : (HudPlayer) -> ImageComponent

    private val childrenMapper: (ImageComponent, UpdateEvent) -> ImageMapper = original.childrenMapper?.map { (name, condition) ->
        children[name].ifNull { "This children doesn't exist in ${original.id}: $name" } to condition
    }?.let {
        { root, event ->
            it.map { (component, condition) ->
                component mapper event to (condition build event)
            }.let { buildList ->
                ImageMapper { player ->
                    buildList.firstOrNull { pair ->
                        pair.second(player)
                    }?.first?.invoke(player) ?: root
                }
            }
        }
    } ?: { root, _ ->
        ImageMapper {
            root
        }
    }

    private class ImageMapperTree(
        val defaultMapper: ImageMapper,
        val children: Map<String, ImageMapper>
    ) : Map<String, ImageMapper> by children

    infix fun mapper(event: UpdateEvent): (HudPlayer) -> ImageComponent {
        val buildFollow = original.follow?.build(event)
        val mapperTree = ImageMapperTree(
            childrenMapper(this, event),
            entries.associate { (k, v) ->
                k to v.childrenMapper(v, event)
            }
        )
        return { player ->
            (buildFollow?.let {
                mapperTree[it.value(player) as String]
            } ?: mapperTree.defaultMapper)(player)
        }
    }
}