package kr.toxicity.hud.util

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

val Player.armor
    get(): Double {
        var attribute = getAttribute(Attribute.GENERIC_ARMOR)?.value ?: 0.0
        val inventory = inventory
        fun add(itemStack: ItemStack?) {
            itemStack?.itemMeta?.getAttributeModifiers(Attribute.GENERIC_ARMOR)?.sumOf {
                it.amount
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

val Player.textures
    get() = PLUGIN.nms.getTextureValue(this)

val Player.emptySpace
    get(): Int {
        val inv: Inventory = inventory
        var r = 0
        for (i in 0..35) {
            val item = inv.getItem(i)
            if (item == null || item.type == Material.AIR) r++
        }
        return r
    }
fun Player.storage(target : ItemStack?): Int {
    if (target == null || target.type == Material.AIR) return emptySpace
    val inv = inventory
    val max = target.maxStackSize
    return Array(36) { i ->
        inv.getItem(i)?.run {
            if (type == Material.AIR) max
            else if (isSimilar(target)) (max - amount).coerceAtLeast(0)
            else 0
        } ?: max
    }.sum()
}
fun Player.totalAmount(item: ItemStack): Int {
    var i = 0
    for (content in inventory.contents) {
        if (content != null && item.isSimilar(content)) i += content.amount
    }
    return i
}
fun Player.storage(material: Material): Int {
    if (material == Material.AIR) return emptySpace
    val inv = inventory
    val max = material.maxStackSize
    return Array(36) { i ->
        inv.getItem(i)?.run {
            when (type) {
                Material.AIR -> max
                material -> (max - amount).coerceAtLeast(0)
                else -> 0
            }
        } ?: max
    }.sum()
}
fun Player.totalAmount(material: Material): Int {
    var i = 0
    for (content in inventory.contents) {
        if (content.type == material) i += content.amount
    }
    return i
}