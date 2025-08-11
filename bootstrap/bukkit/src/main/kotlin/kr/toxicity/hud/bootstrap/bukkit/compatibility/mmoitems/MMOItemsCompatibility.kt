package kr.toxicity.hud.bootstrap.bukkit.compatibility.mmoitems

import io.lumine.mythic.lib.api.item.NBTItem
import io.lumine.mythic.lib.skill.trigger.TriggerType
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.cooldown
import kr.toxicity.hud.bootstrap.bukkit.util.bukkitPlayer
import kr.toxicity.hud.util.ifNull
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.Type
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.api.player.PlayerData
import net.Indyuce.mmoitems.stat.data.AbilityData
import net.Indyuce.mmoitems.stat.data.AbilityListData
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.ItemStat
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Function

class MMOItemsCompatibility : Compatibility {

    override val website: String = "https://www.spigotmc.org/resources/39267/"

    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "cooldown_by_name" to { yaml ->
                val hotbar = yaml.getAsInt("hotbar", 0).takeIf { it in 0..8 }.ifNull { "hotbar range must be 0..8" }
                val name = yaml["name"]?.asString().ifNull { "Unable to find \"name\" configuration" }
                HudListener { player ->
                    player.cooldownOf(
                        hotbar,
                        name,
                        { it.ability.name }
                    ) { cooldown, data ->
                        val param = data.getParameter("cooldown")
                        if (param == 0.0) 0.0 else cooldown / param
                    }
                }.run {
                    { this }
                }
            },
            "cooldown_by_trigger" to { yaml ->
                val hotbar = yaml.getAsInt("hotbar", 0).takeIf { it in 0..8 }.ifNull { "hotbar range must be 0..8" }
                val type = yaml["type"]?.asString()?.asSkillTrigger().ifNull { "Unable to find \"type\" configuration" }
                HudListener { player ->
                    player.cooldownOf(
                        hotbar,
                        type,
                        { it.trigger }
                    ) { cooldown, data ->
                        val param = data.getParameter("cooldown")
                        if (param == 0.0) 0.0 else cooldown / param
                    }
                }.run {
                    { this }
                }
            }
        )
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
                .build(),
            "cooldown_by_name" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(2)
                .function { args, _ ->
                    val hotbar = args[0].toIntOrNull().takeIf { it in 0..8 }.ifNull { "hotbar range must be 0..8" }
                    val name = args[1]
                    Function { p ->
                        p.cooldownOf(
                            hotbar,
                            name,
                            { it.ability.name }
                        ) { cooldown, _ ->
                            cooldown
                        }
                    }
                }
                .build(),
            "cooldown_by_type" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(2)
                .function { args, _ ->
                    val hotbar = args[0].toIntOrNull().takeIf { it in 0..8 }.ifNull { "hotbar range must be 0..8" }
                    val type = args[1].asSkillTrigger().ifNull { "Unable to find \"type\" configuration" }
                    Function { p ->
                        p.cooldownOf(
                            hotbar,
                            type,
                            { it.trigger }
                        ) { cooldown, _ ->
                            cooldown
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
                    val name = args[0].asSkillTrigger()
                    Function {
                        it.bukkitPlayer.inventory.itemInMainHand.asMMOAbility()[name]?.handler?.id ?: "<none>"
                    }
                }
                .build(),
            "offhand_skill" to HudPlaceholder.builder<String>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val name = args[0].asSkillTrigger()
                    Function {
                        it.bukkitPlayer.inventory.itemInOffHand.asMMOAbility()[name]?.handler?.id ?: "<none>"
                    }
                }
                .build()
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()


    private fun <T> HudPlayer.cooldownOf(hotbar: Int, key: T, keyMapper: (AbilityData) -> T, mapper: (Double, AbilityData) -> Double): Double {
        val bukkit = bukkitPlayer
        return bukkit.inventory.getItem(hotbar)?.asMMOStat(ItemStats.ABILITIES) { data: AbilityListData ->
            data.abilities.associateBy(keyMapper)
        }?.let { map ->
            map[key]
        }?.let {
            val mmo = bukkit.toMMO() ?: return@let 0.0
            mapper(mmo.cooldown(it.ability), it)
        } ?: 0.0
    }

    private fun mmoItem(type: Type, name: String) = MMOItems.plugin.getMMOItem(type, name).ifNull { "Unable to find this MMOItem: $name in ${type.id}" }

    private fun Player.toMMO(): PlayerData? {
        return MMOItems.plugin.playerDataManager.getOrNull(uniqueId)
    }

    private fun String.asSkillTrigger() = TriggerType.valueOf(this)

    private fun ItemStack.asMMOAbility(): Map<TriggerType, AbilityData> = asMMOStat(ItemStats.ABILITIES) { data: AbilityListData ->
        data.abilities.associateBy { it.trigger }
    } ?: emptyMap()

    private inline fun <reified T : StatData, R> ItemStack.asMMOStat(key: ItemStat<*, *>, mapper: (T) -> R): R? = asMMOItem()?.let { mmo ->
        (mmo.getData(key) as? T)?.let {
            mapper(it)
        }
    }

    private fun ItemStack.asMMOItem() = NBTItem.get(this).takeIf { nbt ->
        MMOItems.getID(nbt) != null && MMOItems.getType(nbt) != null
    }?.let { nbt ->
        LiveMMOItem(nbt)
    }
}