package kr.toxicity.hud.bootstrap.bukkit.compatibility.nexo

import com.nexomc.nexo.api.events.resourcepack.NexoPrePackGenerateEvent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.plugin.ReloadState.*
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.BukkitBootstrapImpl
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.util.registerListener
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.info
import kr.toxicity.hud.util.warn
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import team.unnamed.creative.ResourcePack

class NexoCompatibility : Compatibility {
    override val website: String = "https://www.spigotmc.org/resources/72448/"
    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf()
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf()
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()

    override fun start() {
        (BOOTSTRAP as BukkitBootstrapImpl).skipInitialReload = true
        registerListener(object : Listener {
            @EventHandler
            fun generate(event: NexoPrePackGenerateEvent) {
                when (val state = PLUGIN.reload()) {
                    is Success -> {
                        val pack = ResourcePack.resourcePack()
                        state.resourcePack.forEach {
                            pack.unknownFile(it.key) { stream ->
                                stream.write(it.value)
                            }
                        }
                        event.addResourcePack(pack)
                        info("Successfully merged with Oraxen: (${state.time} ms)")
                    }
                    is Failure -> {
                        warn(
                            "Fail to merge the resource pack with Oraxen.",
                            "Reason: ${state.throwable.message ?: state.throwable.javaClass.simpleName}"
                        )
                    }
                    is OnReload -> warn("This plugin is still on reload!")
                }
            }
        })
        info(
            "BetterHud hooks Nexo.",
            "Be sure to set 'pack-type' to 'none' in your config."
        )
    }
}