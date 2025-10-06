package kr.toxicity.hud.nms.v1_21_R6.entity

import net.kyori.adventure.util.TriState
import net.minecraft.world.entity.LivingEntity
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.potion.PotionEffect
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector

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

    override fun getTargetBlockExact(maxDistance: Int): Block? {
        return source.getTargetBlockExact(maxDistance)
    }

    override fun rayTraceBlocks(maxDistance: Double): RayTraceResult? {
        return source.rayTraceBlocks(maxDistance)
    }

    override fun addPotionEffect(effect: PotionEffect): Boolean {
        return source.addPotionEffect(effect)
    }

    override fun getEquipment(): EntityEquipment {
        return source.equipment
    }

    override fun getShieldBlockingDelay(): Int {
        return source.shieldBlockingDelay
    }

    override fun setShieldBlockingDelay(delay: Int) {
        source.shieldBlockingDelay = delay
    }

    override fun <T : Projectile?> launchProjectile(projectile: Class<out T?>): T & Any {
        return source.launchProjectile(projectile)
    }

    override fun <T : Projectile?> launchProjectile(
        projectile: Class<out T?>,
        velocity: Vector?
    ): T & Any {
        return source.launchProjectile(projectile, velocity)
    }
}