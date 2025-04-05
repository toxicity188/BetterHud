package kr.toxicity.hud.nms.v1_20_R1.entity

import net.kyori.adventure.util.TriState
import net.minecraft.world.entity.Entity
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_20_R1.CraftServer
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachmentInfo

class CraftEntityView(
    val source: CraftEntity
) : CraftEntity(Bukkit.getServer() as CraftServer, source.unsafeHandle) {

    override fun getHandle(): Entity? {
        return source.unsafeHandle
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
}