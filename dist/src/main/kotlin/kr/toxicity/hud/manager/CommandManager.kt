package kr.toxicity.hud.manager

import kr.toxicity.hud.api.plugin.ReloadState
import kr.toxicity.hud.command.CommandModule
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import org.bukkit.Bukkit

object CommandManager: BetterHudManager {

    private val command = CommandModule("hud")
        .addCommand("reload") {
            aliases = listOf("re", "rl", "리로드")
            description = "Reload this plugin.".toComponent()
            usage = "reload".toComponent()
            permission = listOf("$NAME_SPACE.reload")
            executer = { s, _ ->
                s.info("Try to start reloading. please wait...")
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
        .addCommandModule("hud", {
            aliases = listOf("h")
            permission = listOf("$NAME_SPACE.hud")
        }) {
            addCommand("add") {
                aliases = listOf("a")
                description = "Adds the hud for some player.".toComponent()
                usage = "add <player> <hud>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.hud.add")
                executer = exec@ { s, a ->
                    val player = Bukkit.getPlayer(a[0])?.let {
                        PlayerManager.getHudPlayer(it)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = HudManagerImpl.getHud(a[1]) ?: run {
                        s.warn("This hud doesn't exist: ${a[1]}")
                        return@exec
                    }
                    player.huds.add(hud)
                    s.info("Successfully added.")
                }
            }
            addCommand("remove") {
                aliases = listOf("r")
                description = "Removes the hud for some player.".toComponent()
                usage = "remove <player> <hud>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.hud.remove")
                executer = exec@ { s, a ->
                    val player = Bukkit.getPlayer(a[0])?.let {
                        PlayerManager.getHudPlayer(it)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = HudManagerImpl.getHud(a[1]) ?: run {
                        s.warn("This hud doesn't exist: ${a[1]}")
                        return@exec
                    }
                    player.huds.remove(hud)
                    s.info("Successfully removed.")
                }
            }
        }

        .addCommandModule("popup", {
            aliases = listOf("p")
            permission = listOf("$NAME_SPACE.popup")
        }) {
            addCommand("add") {
                aliases = listOf("a")
                description = "Adds the popup for some player.".toComponent()
                usage = "add <player> <popup>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.popup.add")
                executer = exec@ { s, a ->
                    val player = Bukkit.getPlayer(a[0])?.let {
                        PlayerManager.getHudPlayer(it)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    player.popups.add(hud)
                    s.info("Successfully added.")
                }
            }
            addCommand("remove") {
                aliases = listOf("r")
                description = "Removes the popup for some player.".toComponent()
                usage = "remove <player> <popup>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.popup.remove")
                executer = exec@ { s, a ->
                    val player = Bukkit.getPlayer(a[0])?.let {
                        PlayerManager.getHudPlayer(it)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    player.popups.remove(hud)
                    s.info("Successfully removed.")
                }
            }
        }

    override fun start() {
        PLUGIN.getCommand(NAME_SPACE)?.setExecutor(command.createTabExecutor())
    }

    override fun reload(resource: GlobalResource) {
    }

    override fun end() {
    }
}