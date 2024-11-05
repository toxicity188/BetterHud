package kr.toxicity.hud.bootstrap.bukkit.compatibility.oraxen

import io.th0rgal.oraxen.api.events.resourcepack.OraxenPrePackGenerateEvent
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.bukkit.pack.BukkitResourcePackHandler
import kr.toxicity.hud.api.plugin.ReloadState.*
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import team.unnamed.creative.ResourcePack

class OraxenR2Handler : BukkitResourcePackHandler {
    override fun handle(plugin: Plugin) {
        val api = BetterHudAPI.inst()
        val logger = BetterHudAPI.inst().bootstrap().logger()
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun generate(event: OraxenPrePackGenerateEvent) {
                when (val state = api.reload()) {
                    is Success -> {
                        val pack = ResourcePack.resourcePack()
                        state.resourcePack.forEach {
                            pack.unknownFile(it.key) { stream ->
                                stream.write(it.value)
                            }
                        }
                        event.addResourcePack(pack)
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