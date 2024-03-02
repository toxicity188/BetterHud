package kr.toxicity.hud.util

import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
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