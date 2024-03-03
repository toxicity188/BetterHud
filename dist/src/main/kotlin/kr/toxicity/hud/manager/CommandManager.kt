package kr.toxicity.hud.manager

import kr.toxicity.hud.api.plugin.ReloadState
import kr.toxicity.hud.command.CommandModule
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*

object CommandManager: MythicHudManager {

    private val command = CommandModule("hud")
        .addCommand("reload") {
            aliases = listOf("re", "rl", "리로드")
            description = "Reload this plugin.".toComponent()
            usage = "reload".toComponent()
            permission = listOf("$NAME_SPACE.reload")
            executer = { s, _ ->
                asyncTask {
                    val result = PLUGIN.reload()
                    when (result.state) {
                        ReloadState.STILL_ON_RELOAD -> {
                            s.warn("The plugin is still on reload!")
                        }
                        ReloadState.SUCCESS -> {
                            s.info("Reload success. (${result.time} ms)")
                        }
                    }
                }
            }
        }

    override fun start() {
        PLUGIN.getCommand("mythichud")?.setExecutor(command.createTabExecutor())
    }

    override fun reload(resource: GlobalResource) {
    }

    override fun end() {
    }
}