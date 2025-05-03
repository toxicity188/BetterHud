package kr.toxicity.hud.bootstrap.bukkit.util

import kr.toxicity.hud.api.bukkit.BukkitBootstrap
import kr.toxicity.hud.api.bukkit.nms.NMSVersion
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.util.BOOTSTRAP
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

val ATTRIBUTE_MAX_HEALTH = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(if ((BOOTSTRAP as BukkitBootstrap).volatileCode().version >= NMSVersion.V1_21_R2) "max_health" else "generic.max_health"))!!
val ATTRIBUTE_ARMOR = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(if ((BOOTSTRAP as BukkitBootstrap).volatileCode().version >= NMSVersion.V1_21_R2) "armor" else "generic.armor"))!!

val Player.armor
    get(): Double {
        var attribute = getAttribute(ATTRIBUTE_ARMOR)?.value ?: 0.0
        val inventory = inventory
        fun add(itemStack: ItemStack?) {
            itemStack?.itemMeta?.attributeModifiers?.get(ATTRIBUTE_ARMOR)?.sumOf { v ->
                v.amount
            }?.let {
                attribute += it
            }
        }
        add(inventory.helmet)
        add(inventory.chestplate)
        add(inventory.leggings)
        add(inventory.boots)
        return attribute
    }

val Player.emptySpace
    get(): Int {
        val inv: Inventory = inventory
        return (0..35).count { i ->
            val item = inv.getItem(i)
            item == null || item.type == Material.AIR
        }
    }
fun Player.storage(material: Material): Int {
    if (material == Material.AIR) return emptySpace
    val inv = inventory
    val max = material.maxStackSize
    return (0..35).sumOf { i ->
        inv.getItem(i)?.run {
            when (type) {
                Material.AIR -> max
                material -> (max - amount).coerceAtLeast(0)
                else -> 0
            }
        } ?: max
    }
}
fun Player.totalAmount(material: Material): Int {
    return inventory.contents.sumOf { content ->
        if (content?.type == material) content.amount else 0
    }
}
fun Player.toHud() = PlayerManagerImpl.getHudPlayer(uniqueId)