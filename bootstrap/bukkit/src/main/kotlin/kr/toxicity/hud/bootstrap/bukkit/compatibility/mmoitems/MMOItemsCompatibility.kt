package kr.toxicity.hud.bootstrap.bukkit.compatibility.mmoitems

import io.lumine.mythic.lib.api.item.NBTItem
import io.lumine.mythic.lib.skill.handler.SkillHandler
import io.lumine.mythic.lib.skill.result.SkillResult
import io.lumine.mythic.lib.skill.trigger.TriggerType
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.util.bukkitPlayer
import kr.toxicity.hud.util.ifNull
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.Type
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.AbilityListData
import org.bukkit.inventory.ItemStack
import java.util.function.Function

class MMOItemsCompatibility : Compatibility {

    override val website: String = "https://www.spigotmc.org/resources/39267/"

    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "total_amount" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(2)
                .function { args, _ ->
                    val item = mmoItem(Type.get(args[0]).ifNull { "Unable to find this MMOItems type: ${args[0]}" }, args[1]).id
                    Function { p ->
                        p.bukkitPlayer.inventory.sumOf {
                            if (MMOItems.getID(it) == item) it.amount else 0
                        }
                    }
                }
                .build()
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "mainhand_skill" to HudPlaceholder.builder<String>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val name = TriggerType.valueOf(args[0])
                    Function {
                        getAbility(it.bukkitPlayer.inventory.itemInMainHand)[name]?.id ?: "<none>"
                    }
                }
                .build()
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()


    private fun mmoItem(type: Type, name: String) = MMOItems.plugin.getMMOItem(type, name).ifNull { "Unable to find this MMOItem: $name in ${type.id}" }

    private fun getAbility(itemStack: ItemStack): Map<TriggerType, SkillHandler<SkillResult>> {
        val nbt = NBTItem.get(itemStack)
        if (MMOItems.getID(nbt) == null || MMOItems.getType(nbt) == null) return emptyMap()
        return (LiveMMOItem(nbt).getData(ItemStats.ABILITIES) as? AbilityListData)?.abilities?.associate {
            it.trigger to it.handler
        } ?: emptyMap()
    }
}