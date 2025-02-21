package kr.toxicity.hud.bootstrap.fabric.util

import kr.toxicity.hud.api.fabric.event.EventRegistry
import kr.toxicity.hud.api.fabric.event.FabricEvent
import kr.toxicity.hud.api.fabric.event.PlayerEvent
import kr.toxicity.hud.api.fabric.trigger.HudFabricEventTrigger
import kr.toxicity.hud.api.fabric.update.FabricUpdateEvent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.impl.NonWrappingComponentSerializer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.luckperms.api.LuckPermsProvider
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.Attributes
import java.util.*
import java.util.function.BiConsumer

private val PERMISSION_GETTER: (ServerPlayer, String) -> Boolean = if (FabricLoader.getInstance().isModLoaded("luckperms")) LuckPermsProvider.get().let {
    { player: ServerPlayer, perm: String ->
        it.userManager.getUser(player.uuid)?.cachedData?.permissionData?.checkPermission(perm)?.asBoolean() == true
    }
} else {
    { player: ServerPlayer, _: String ->
        player.hasPermissions(2)
    }
}

inline fun <reified T : FabricEvent<*>, R : Any> UpdateEvent.unwrap(block: (T) -> R): R {
    val evt = source()
    return if (evt is FabricUpdateEvent) {
        val e = evt.event
        if (e is T) block(e)
        else throw RuntimeException("Unsupported event found: ${e.javaClass.simpleName}")
    } else throw RuntimeException("Unsupported update found: ${javaClass.simpleName}")
}

fun Component.toMiniMessageString() = MiniMessage.miniMessage().serialize(NonWrappingComponentSerializer.INSTANCE.deserialize(this))

fun <T : FabricEvent<*>> createFabricTrigger(
    registry: EventRegistry<T>,
    valueMapper: (T) -> UUID? = { if (it is PlayerEvent<*>) it.player().uuid else null },
    keyMapper: (T) -> Any = { UUID.randomUUID() }
): HudFabricEventTrigger<T> {
    return object : HudFabricEventTrigger<T> {
        override fun registry(): EventRegistry<T> = registry
        override fun getKey(t: T): Any = keyMapper(t)
        override fun registerEvent(eventConsumer: BiConsumer<UUID, UpdateEvent>) {
            registry.registerTemp {
                valueMapper(it)?.let { uuid ->
                    val wrapper = FabricUpdateEvent(
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