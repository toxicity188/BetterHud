package kr.toxicity.hud.bootstrap.bukkit.compatibility.nexo

import com.nexomc.nexo.api.events.resourcepack.NexoPrePackGenerateEvent
import com.nexomc.nexo.configs.Settings
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.plugin.ReloadState.*
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.BukkitBootstrapImpl
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.util.registerListener
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.pack.PackType
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.handle
import kr.toxicity.hud.util.info
import kr.toxicity.hud.util.warn
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class NexoCompatibility : Compatibility {
    override val website: String = "https://polymart.org/resource/nexo.6901"
    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>> = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener> = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>> = mapOf()
    override val strings: Map<String, HudPlaceholder<String>> = mapOf()
    override val booleans: Map<String, HudPlaceholder<Boolean>> = mapOf()

    override fun start() {
        if (Settings.PACK_SERVER_TYPE.value != "NONE") (BOOTSTRAP as BukkitBootstrapImpl).skipInitialReload = true
        registerListener(object : Listener {
            @EventHandler
            fun NexoPrePackGenerateEvent.generate() {
                ConfigManagerImpl.preReload()
                if (ConfigManagerImpl.packType == PackType.NONE) when (val state = PLUGIN.reload()) {
                    is Success -> {
                        state.resourcePack.forEach {
                            addUnknownFile(it.key, it.value)
                        }
                        info("Successfully merged with Nexo: (${state.time} ms)")
                    }
                    is Failure -> {
                        state.throwable.handle("Fail to merge the resource pack with Nexo.")
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