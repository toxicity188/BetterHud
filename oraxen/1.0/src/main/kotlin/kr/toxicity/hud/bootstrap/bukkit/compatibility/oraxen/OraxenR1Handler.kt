package kr.toxicity.hud.bootstrap.bukkit.compatibility.oraxen

import io.th0rgal.oraxen.api.events.OraxenPackGeneratedEvent
import io.th0rgal.oraxen.utils.VirtualFile
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.bukkit.pack.BukkitResourcePackHandler
import kr.toxicity.hud.api.plugin.ReloadState.*
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.io.ByteArrayInputStream

class OraxenR1Handler : BukkitResourcePackHandler {
    override fun handle(plugin: Plugin) {
        val api = BetterHudAPI.inst()
        val logger = BetterHudAPI.inst().bootstrap().logger()
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun generate(event: OraxenPackGeneratedEvent) {
                when (val state = api.reload()) {
                    is Success -> {
                        val output = event.output
                        state.resourcePack.forEach {
                            output.add(
                                VirtualFile(
                                    it.key.substringBeforeLast('/'),
                                    it.key.substringAfterLast('/'),
                                    ByteArrayInputStream(it.value).buffered()
                                )
                            )
                        }
                        logger.info("Successfully merged with Oraxen: (${state.time} ms)")
                    }
                    is Failure -> {
                        logger.warn(
                            "Fail to merge the resource pack with Oraxen.",
                            "Reason: ${state.throwable.message ?: state.throwable.javaClass.simpleName}"
                        )
                    }
                    is OnReload -> logger.warn("This plugin is still on reload!")
                }
            }
        }, plugin)
    }
}