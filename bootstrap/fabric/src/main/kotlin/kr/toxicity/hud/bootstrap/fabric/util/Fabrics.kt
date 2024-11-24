package kr.toxicity.hud.bootstrap.fabric.util

import kr.toxicity.hud.api.player.HudPlayer
import net.fabricmc.loader.api.FabricLoader
import net.luckperms.api.LuckPermsProvider
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.Attributes

private val PERMISSION_GETTER: (ServerPlayer, String) -> Boolean = if (FabricLoader.getInstance().isModLoaded("luckperms")) LuckPermsProvider.get().let {
    { player: ServerPlayer, perm: String ->
        it.userManager.getUser(player.uuid)?.cachedData?.permissionData?.checkPermission(perm)?.asBoolean() ?: false
    }
} else {
    { player: ServerPlayer, _: String ->
        player.hasPermissions(2)
    }
}

val HudPlayer.fabricPlayer
    get() = handle() as ServerPlayer

val ServerPlayer.armor
    get(): Double {
        return attributes.getInstance(Attributes.ARMOR)?.modifiers?.sumOf { modifier ->
            modifier.amount
        } ?: 0.0
    }

fun ServerPlayer.hasPermission(perm: String) = PERMISSION_GETTER(this, perm)