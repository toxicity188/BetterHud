package kr.toxicity.hud.bootstrap.fabric.util

import kr.toxicity.hud.api.mod.event.EventRegistry
import kr.toxicity.hud.api.mod.event.ModEvent
import kr.toxicity.hud.api.mod.event.PlayerEvent
import kr.toxicity.hud.api.mod.trigger.HudModEventTrigger
import kr.toxicity.hud.api.mod.update.ModUpdateEvent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.luckperms.api.LuckPermsProvider
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permissions
import net.minecraft.world.entity.ai.attributes.Attributes
import java.util.*
import java.util.function.BiConsumer

private val PERMISSION_GETTER: (ServerPlayer, String) -> Boolean = if (FabricLoader.getInstance().isModLoaded("luckperms")) LuckPermsProvider.get().let {
    { player: ServerPlayer, perm: String ->
        it.userManager.getUser(player.uuid)?.cachedData?.permissionData?.checkPermission(perm)?.asBoolean() == true
    }
} else {
    { player: ServerPlayer, _: String ->
        player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
    }
}

inline fun <reified T : ModEvent<*>, R : Any> UpdateEvent.unwrap(block: (T) -> R): R {
    val evt = source()
    return if (evt is ModUpdateEvent) {
        val e = evt.event
        if (e is T) block(e)
        else throw RuntimeException("Unsupported event found: ${e.javaClass.simpleName}")
    } else throw RuntimeException("Unsupported update found: ${javaClass.simpleName}")
}

fun Component.toAdventure() = NonWrappingComponentSerializer.INSTANCE.deserialize(this)
fun net.kyori.adventure.text.Component.toMinecraft(): Component = NonWrappingComponentSerializer.INSTANCE.serialize(this)

fun Component.toMiniMessageString() = MiniMessage.miniMessage().serialize(toAdventure())

fun <T : ModEvent<*>> createFabricTrigger(
    registry: EventRegistry<T>,
    valueMapper: (T) -> UUID? = { if (it is PlayerEvent<*>) it.player().uuid else null },
    keyMapper: (T) -> Any = { UUID.randomUUID() }
): HudModEventTrigger<T> {
    return object : HudModEventTrigger<T> {
        override fun registry(): EventRegistry<T> = registry
        override fun getKey(t: T): Any = keyMapper(t)
        override fun registerEvent(eventConsumer: BiConsumer<UUID, UpdateEvent>) {
            registry.registerTemp {
                valueMapper(it)?.let { uuid ->
                    val wrapper = ModUpdateEvent(
                        it,
                        keyMapper(it)
                    )
                    eventConsumer.accept(uuid, wrapper)
                }
            }
        }
    }
}

val HudPlayer.fabricPlayer
    get() = handle() as ServerPlayer

val ServerPlayer.armor
    get(): Double = attributes.getValue(Attributes.ARMOR)

fun ServerPlayer.hasPermission(perm: String) = PERMISSION_GETTER(this, perm)