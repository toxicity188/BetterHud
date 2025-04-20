package kr.toxicity.hud.bootstrap.bukkit.compatibility.oraxen

import io.th0rgal.oraxen.api.events.OraxenPackGeneratedEvent
import io.th0rgal.oraxen.config.Settings
import io.th0rgal.oraxen.utils.VirtualFile
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
import java.io.ByteArrayInputStream

class OraxenCompatibility : Compatibility {

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
        if (Settings.UPLOAD.toBool()) (BOOTSTRAP as BukkitBootstrapImpl).skipInitialReload = true
        registerListener(object : Listener {
            @EventHandler
            fun OraxenPackGeneratedEvent.generate() {
                ConfigManagerImpl.preReload()
                if (ConfigManagerImpl.packType == PackType.NONE) when (val state = PLUGIN.reload()) {
                    is Success -> {
                        val output = output
                        state.resourcePack.forEach {
                            output.add(
                                VirtualFile(
                                    it.key.substringBeforeLast('/'),
                                    it.key.substringAfterLast('/'),
                                    ByteArrayInputStream(it.value).buffered()
                                )
                            )
                        }
                        info("Successfully merged with Oraxen: (${state.time} ms)")
                    }
                    is Failure -> {
                        state.throwable.handle("Fail to merge the resource pack with Oraxen.")
                    }
                    is OnReload -> warn("This plugin is still on reload!")
                }
            }
        })
        info(
            "BetterHud hooks Oraxen.",
            "Be sure to set 'pack-type' to 'none' in your config."
        )
    }
}