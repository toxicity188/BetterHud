package kr.toxicity.hud.image

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.util.ifNull
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

class ImageComponent(
    private val original: HudImage,
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
        }, children.values.maxOfOrNull {
            it.max
        } ?: 0)

    private fun PixelComponent.applyColor(color: TextColor) = PixelComponent(if (color.value() == NamedTextColor.WHITE.value()) component else WidthComponent(
        component.component.build().toBuilder().color(color),
        component.width
    ), pixel)

    fun applyColor(color: TextColor): ImageComponent = ImageComponent(
        original,
        images.map {
            it.applyColor(color)
        },
        children.entries.associate {
            it.key to it.value.applyColor(color)
        }
    )

    private fun interface ImageMapper : (HudPlayer) -> ImageComponent

    private val childrenMapper: (UpdateEvent) -> ImageMapper = original.childrenMapper?.map {
        children[it.first].ifNull("This children doesn't exist in ${original.name}: ${it.first}") to it.second
    }?.let {
        { event: UpdateEvent ->
            it.map { builder ->
                builder.first to builder.second.build(event)
            }.let { buildList ->
                ImageMapper { player ->
                    buildList.firstOrNull { pair ->
                        pair.second(player)
                    }?.first ?: this
                }
            }
        }
    } ?: {
        ImageMapper {
            this
        }
    }

    private class ImageMapperTree(
        val defaultMapper: ImageMapper,
        val children: Map<String, ImageMapper>
    ) : Map<String, ImageMapper> by children

    fun imageMapper(event: UpdateEvent): (HudPlayer) -> ImageComponent {
        val buildFollow = original.follow?.build(event)
        val mapperTree = ImageMapperTree(
            childrenMapper(event),
            children.entries.associate {
                it.key to it.value.childrenMapper(event)
            }
        )
        return { player ->
            (buildFollow?.let {
                mapperTree[it.value(player) as String]
            } ?: mapperTree.defaultMapper)(player)
        }
    }
}