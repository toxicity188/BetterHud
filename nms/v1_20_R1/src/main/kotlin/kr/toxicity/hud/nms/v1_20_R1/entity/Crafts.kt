package kr.toxicity.hud.nms.v1_20_R1.entity

import net.minecraft.world.entity.Entity
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity

private val HANDLE by lazy {
    CraftEntity::class.java.getDeclaredField("entity").apply {
        isAccessible = true
    }.let {
        { e: CraftEntity ->
            it[e] as Entity
        }
    }
}

val CraftEntity.unsafeHandle
    get() = HANDLE(this)