package kr.toxicity.hud.bootstrap.bukkit.util

import kr.toxicity.hud.manager.PlayerManagerImpl
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

val Player.armor
    get(): Double {
        var attribute = getAttribute(Attribute.ARMOR)?.value ?: 0.0
        val inventory = inventory
        fun add(itemStack: ItemStack?) {
            itemStack?.itemMeta?.attributeModifiers?.get(Attribute.ARMOR)?.sumOf { v ->
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