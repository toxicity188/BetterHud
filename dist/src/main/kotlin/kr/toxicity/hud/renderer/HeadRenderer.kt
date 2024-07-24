package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.layout.LayoutAlign
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

class HeadRenderer(
    private val components: List<Component>,
    private val pixel: Int,
    private val x: Int,
    private val align: LayoutAlign,
    follow: String?,
    private val conditions: ConditionBuilder,
) {
    private val followPlayer = follow?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }
    private val nextPixel = (-pixel * 8).toSpaceComponent() + NEGATIVE_ONE_SPACE_COMPONENT

    fun getHead(event: UpdateEvent): (HudPlayer) -> PixelComponent {
        val cond = conditions.build(event)
        val playerPlaceholder = followPlayer?.build(event)
        return build@{ player ->
            var targetPlayer = player
            var targetPlayerHead: HudPlayerHead = player.head
            playerPlaceholder?.let {
                val value = it.value(player)
                val pair = getHead(value.toString())
                pair.first?.let { player ->
                    targetPlayer = player
                }
                targetPlayerHead = pair.second
            }
            if (cond(targetPlayer)) {
                var comp = EMPTY_WIDTH_COMPONENT
                var i = 0
                targetPlayerHead.colors.forEachSync { next ->
                    val index = i++
                    comp += WidthComponent(Component.text().append(components[index / 8]).color(next), pixel)
                    comp += if (index < 63 && index % 8 == 7) nextPixel else NEGATIVE_ONE_SPACE_COMPONENT
                }
                comp.toPixelComponent(
                    when (align) {
                        LayoutAlign.LEFT -> x
                        LayoutAlign.CENTER -> x - comp.width / 2
                        LayoutAlign.RIGHT -> x - comp.width
                    }
                )
            } else EMPTY_PIXEL_COMPONENT
        }
    }

    private fun getHead(placeholderValue: String): Pair<HudPlayer?, HudPlayerHead> {
        return Bukkit.getPlayer(placeholderValue)?.let { bukkitPlayer ->
            PlayerManagerImpl.getHudPlayer(bukkitPlayer.uniqueId)
        } to PlayerHeadManager.provideHead(placeholderValue)
    }
}