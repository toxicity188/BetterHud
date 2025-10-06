package kr.toxicity.hud.nms.v1_21_R5.entity

import net.kyori.adventure.util.TriState
import net.minecraft.world.entity.LivingEntity
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachmentInfo

class CraftLivingEntityView(
    val source: CraftLivingEntity
) : CraftLivingEntity(Bukkit.getServer() as CraftServer, source.unsafeHandle as LivingEntity) {

    override fun getHandle(): LivingEntity {
        return source.unsafeHandle as LivingEntity
    }

    override fun getPersistentDataContainer(): CraftPersistentDataContainer {
        return source.persistentDataContainer
    }

    override fun getLastDamageCause(): EntityDamageEvent? {
        return source.lastDamageCause
    }

    override fun setLastDamageCause(event: EntityDamageEvent?) {
        source.lastDamageCause = event
    }

    override fun permissionValue(permission: Permission): TriState {
        return source.permissionValue(permission)
    }

    override fun permissionValue(permission: String): TriState {
        return source.permissionValue(permission)
    }

    override fun getEffectivePermissions(): Set<PermissionAttachmentInfo?> {
        return source.effectivePermissions
    }

    override fun hasPermission(name: String): Boolean {
        return source.hasPermission(name)
    }

    override fun hasPermission(perm: Permission): Boolean {
        return source.hasPermission(perm)
    }

    override fun isPermissionSet(name: String): Boolean {
        return source.isPermissionSet(name)
    }

    override fun isPermissionSet(perm: Permission): Boolean {
        return source.isPermissionSet(perm)
    }

    override fun recalculatePermissions() {
        source.recalculatePermissions()
    }

    override fun getEquipment(): EntityEquipment? {
        return source.equipment
    }
}