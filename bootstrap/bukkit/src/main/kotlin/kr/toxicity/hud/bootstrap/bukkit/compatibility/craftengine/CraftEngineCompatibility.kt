package kr.toxicity.hud.bootstrap.bukkit.compatibility.craftengine

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.plugin.ReloadState
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.BukkitBootstrapImpl
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.util.registerListener
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.handle
import kr.toxicity.hud.util.info
import kr.toxicity.hud.util.warn
import net.momirealms.craftengine.bukkit.api.event.AsyncResourcePackCacheEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CraftEngineCompatibility : Compatibility {
    override val website: String = "https://polymart.org/product/7624/craftengine"
    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>> = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener> = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>> = mapOf()
    override val strings: Map<String, HudPlaceholder<String>> = mapOf()
    override val booleans: Map<String, HudPlaceholder<Boolean>> = mapOf()

    override fun start() {
        ConfigManagerImpl.preReload()
        if (ConfigManagerImpl.mergeWithExternalResources) (BOOTSTRAP as BukkitBootstrapImpl).skipInitialReload = true
        registerListener(object : Listener {
            @EventHandler
            fun AsyncResourcePackCacheEvent.generate() {
                if (!ConfigManagerImpl.mergeWithExternalResources) return
                when (val result = PLUGIN.reload()) {
                    is ReloadState.Success -> {
                        result.directory()?.let {
                            when {
                                it.isFile -> cacheData().externalZips().add(it.toPath())
                                it.isDirectory -> cacheData().externalFolders().add(it.toPath())
                            }
                            info("Successfully merged with CraftEngine.")
                        }
                    }
                    is ReloadState.OnReload -> {
                        warn("BetterHud is still on reload!")
                    }
                    is ReloadState.Failure -> {
                        result.throwable.handle("Unable to merge with CraftEngine.")
                    }
                }
            }
        })
    }
}