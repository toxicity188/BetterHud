package kr.toxicity.hud.bootstrap.fabric.util

import kr.toxicity.hud.api.player.HudPlayer
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.Attributes

val HudPlayer.fabricPlayer
    get() = handle() as ServerPlayer

val ServerPlayer.armor
    get(): Double {
        val attribute = Attributes.ARMOR.unwrapKey().orElse(null) ?: return 0.0
        return armorSlots.sumOf {
            it.item.defaultInstance.components.get(DataComponents.ATTRIBUTE_MODIFIERS)?.modifiers?.sumOf { modifier ->
                if (modifier.attribute.`is`(attribute)) modifier.modifier.amount else 0.0
            } ?: 0.0
        }
    }