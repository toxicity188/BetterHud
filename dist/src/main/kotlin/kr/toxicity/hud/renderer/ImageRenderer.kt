package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit

class ImageRenderer(
    image: HudImage,
    color: TextColor,
    components: List<PixelComponent>,
    follow: String?,
    private val conditions: ConditionBuilder
) {
    private val type = image.type
    private val components = components.map {
        val comp = it.component
        PixelComponent(WidthComponent(Component.text().append(comp.component.build().color(color)), comp.width), it.pixel)
    }

    private val followPlayer = follow?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }

    val listener: (UpdateEvent) -> HudListener = image.listener ?: HudListener.EMPTY.let {
        { _ ->
            it
        }
    }
    fun getComponent(reason: UpdateEvent): (HudPlayer, Int) -> PixelComponent {
        val cond = conditions.build(reason)
        val listen = listener(reason)
        val follow = followPlayer?.build(reason)
        return build@ { player, frame ->
            var target = player
            follow?.let {
                target = Bukkit.getPlayer(it.value(player).toString())?.let { p ->
                    PlayerManagerImpl.getHudPlayer(p.uniqueId)
                } ?: return@build EMPTY_PIXEL_COMPONENT
            }
            if (cond(target)) type.getComponent(listen, frame, components, target) else EMPTY_PIXEL_COMPONENT
        }
    }

    fun max() = components.maxOfOrNull {
        it.component.width
    } ?: 0
}