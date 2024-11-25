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

    private infix fun PixelComponent.applyColor(color: TextColor) = PixelComponent(if (color.value() == NamedTextColor.WHITE.value()) component else WidthComponent(
        component.component.build().toBuilder().color(color),
        component.width
    ), pixel)

    infix fun applyColor(color: TextColor): ImageComponent = ImageComponent(
        original,
        parent,
        images.map {
            it applyColor color
        },
        entries.associate {
            it.key to (it.value applyColor color)
        }
    )

    private fun interface ImageMapper : (HudPlayer) -> ImageComponent

    private val childrenMapper: (ImageComponent, UpdateEvent) -> ImageMapper = run {
        original.childrenMapper?.map {
            children[it.first].ifNull("This children doesn't exist in ${original.name}: ${it.first}") to it.second
        }?.let {
            { root, event ->
                it.map { builder ->
                    builder.first mapper event to (builder.second build event)
                }.let { buildList ->
                    ImageMapper ret@ { player ->
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
    }

    private class ImageMapperTree(
        val defaultMapper: ImageMapper,
        val children: Map<String, ImageMapper>
    ) : Map<String, ImageMapper> by children

    infix fun mapper(event: UpdateEvent): (HudPlayer) -> ImageComponent {
        val buildFollow = original.follow?.build(event)
        val mapperTree = ImageMapperTree(
            childrenMapper(this, event),
            entries.associate {
                it.key to it.value.childrenMapper(this, event)
            }
        )
        return { player ->
            (buildFollow?.let {
                mapperTree[it.value(player) as String]
            } ?: mapperTree.defaultMapper)(player)
        }
    }
}