package kr.toxicity.hud.manager

import kr.toxicity.hud.api.plugin.ReloadState
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.command.CommandModule
import kr.toxicity.hud.command.SenderType
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

object CommandManager: BetterHudManager {

    private val command = CommandModule("hud")
        .addCommand("reload") {
            aliases = listOf("re", "rl", "리로드")
            description = "Reload this plugin.".toComponent()
            usage = "reload".toComponent()
            permission = listOf("$NAME_SPACE.reload")
            executer = { s, _ ->
                s.info("Try to start reloading. please wait...")
                CompletableFuture.runAsync {
                    val reload = PLUGIN.reload()
                    when (reload.state) {
                        ReloadState.STILL_ON_RELOAD -> {
                            s.warn("The plugin is still on reload!")
                        }
                        ReloadState.SUCCESS -> {
                            s.info("Reload success. (${reload.time} ms)")
                        }
                        ReloadState.FAIL -> {
                            s.info("Reload failed.")
                            s.info("Check your server log to find the problem.")
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
                        PlayerManager.getHudPlayer(it.uniqueId)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = HudManagerImpl.getHud(a[1]) ?: run {
                        s.warn("This hud doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (player.hudObjects.add(hud)) s.info("Successfully added.")
                    else s.warn("Hud '${a[1]}' is already added in this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> Bukkit.getOnlinePlayers().map {
                            it.name
                        }.filter {
                            it.contains(a[0])
                        }
                        2 -> HudManagerImpl.allNames.filter {
                            it.contains(a[1])
                        }
                        else -> null
                    }
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
                        PlayerManager.getHudPlayer(it.uniqueId)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = HudManagerImpl.getHud(a[1]) ?: run {
                        s.warn("This hud doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (player.hudObjects.remove(hud)) s.info("Successfully removed.")
                    else s.warn("Hud '${a[1]}' is already removed in this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> Bukkit.getOnlinePlayers().map {
                            it.name
                        }.filter {
                            it.contains(a[0])
                        }
                        2 -> HudManagerImpl.allNames.filter {
                            it.contains(a[1])
                        }
                        else -> null
                    }
                }
            }
        }

        .addCommandModule("compass", {
            aliases = listOf("h")
            permission = listOf("$NAME_SPACE.compass")
        }) {
            addCommand("add") {
                aliases = listOf("a")
                description = "Adds the compass for some player.".toComponent()
                usage = "add <player> <compass>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.compass.add")
                executer = exec@ { s, a ->
                    val player = Bukkit.getPlayer(a[0])?.let {
                        PlayerManager.getHudPlayer(it.uniqueId)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val compass = CompassManagerImpl.getCompass(a[1]) ?: run {
                        s.warn("This compass doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (player.hudObjects.add(compass)) s.info("Successfully added.")
                    else s.warn("compass '${a[1]}' is already added in this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> Bukkit.getOnlinePlayers().map {
                            it.name
                        }.filter {
                            it.contains(a[0])
                        }
                        2 -> CompassManagerImpl.allNames.filter {
                            it.contains(a[1])
                        }
                        else -> null
                    }
                }
            }
            addCommand("remove") {
                aliases = listOf("r")
                description = "Removes the compass for some player.".toComponent()
                usage = "remove <player> <compass>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.compass.remove")
                executer = exec@ { s, a ->
                    val player = Bukkit.getPlayer(a[0])?.let {
                        PlayerManager.getHudPlayer(it.uniqueId)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val compass = CompassManagerImpl.getCompass(a[1]) ?: run {
                        s.warn("This compass doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (player.hudObjects.remove(compass)) s.info("Successfully removed.")
                    else s.warn("compass '${a[1]}' is already removed in this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> Bukkit.getOnlinePlayers().map {
                            it.name
                        }.filter {
                            it.contains(a[0])
                        }
                        2 -> CompassManagerImpl.allNames.filter {
                            it.contains(a[1])
                        }
                        else -> null
                    }
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
                        PlayerManager.getHudPlayer(it.uniqueId)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (player.hudObjects.add(hud)) s.info("Successfully added.")
                    else s.warn("Popup '${a[1]}' is already added in this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> Bukkit.getOnlinePlayers().map {
                            it.name
                        }.filter {
                            it.contains(a[0])
                        }
                        2 -> PopupManagerImpl.allNames.filter {
                            it.contains(a[1])
                        }
                        else -> null
                    }
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
                        PlayerManager.getHudPlayer(it.uniqueId)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (player.hudObjects.remove(hud)) s.info("Successfully removed.")
                    else s.warn("Popup '${a[1]}' is already removed in this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> Bukkit.getOnlinePlayers().map {
                            it.name
                        }.filter {
                            it.contains(a[0])
                        }
                        2 -> PopupManagerImpl.allNames.filter {
                            it.contains(a[1])
                        }
                        else -> null
                    }
                }
            }
            addCommand("show") {
                aliases = listOf("r")
                description = "Shows the popup for some player.".toComponent()
                usage = "show <player> <popup>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.popup.show")
                executer = exec@ { s, a ->
                    val player = Bukkit.getPlayer(a[0])?.let {
                        PlayerManager.getHudPlayer(it)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val popup = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    runCatching {
                        if (popup.show(UpdateEvent.EMPTY, player) != null) s.info("Popup is successfully displayed.")
                        else s.warn("Failed to show this popup.")
                    }.onFailure { e ->
                        s.warn("Unable to show this popup in command.")
                        s.warn("Reason: ${e.message}")
                    }
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> Bukkit.getOnlinePlayers().map {
                            it.name
                        }.filter {
                            it.contains(a[0])
                        }
                        2 -> PopupManagerImpl.allNames.filter {
                            it.contains(a[1])
                        }
                        else -> null
                    }
                }
            }
            addCommand("hide") {
                aliases = listOf("r")
                description = "Hides the popup for some player.".toComponent()
                usage = "hide <player> <popup>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.popup.hide")
                executer = exec@ { s, a ->
                    val player = Bukkit.getPlayer(a[0])?.let {
                        PlayerManager.getHudPlayer(it)
                    } ?: run {
                        s.warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val popup = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    runCatching {
                        if (popup.hide(player)) s.info("Popup is successfully removed.")
                        else s.warn("Failed to remove this popup.")
                    }.onFailure { e ->
                        s.warn("Unable to show this popup in command.")
                        s.warn("Reason: ${e.message}")
                    }
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> Bukkit.getOnlinePlayers().map {
                            it.name
                        }.filter {
                            it.contains(a[0])
                        }
                        2 -> PopupManagerImpl.allNames.filter {
                            it.contains(a[1])
                        }
                        else -> null
                    }
                }
            }
        }
        .addCommandModule("turn", {
            aliases = listOf("t")
            permission = listOf("$NAME_SPACE.turn")
            allowedSender = listOf(SenderType.PLAYER)
        }) {
            addCommand("on") {
                description = "Turns on your hud.".toComponent()
                usage = "on".toComponent()
                permission = listOf("$NAME_SPACE.turn.on")
                executer = { sender, _ ->
                    PlayerManager.getHudPlayer((sender as Player).uniqueId)?.let {
                        it.isHudEnabled = true
                        sender.info("Successfully turns on.")
                    } ?: sender.warn("You are not available player!")
                }
            }
            addCommand("off") {
                description = "Turns off your hud.".toComponent()
                usage = "off".toComponent()
                permission = listOf("$NAME_SPACE.turn.off")
                executer = { sender, _ ->
                    PlayerManager.getHudPlayer((sender as Player).uniqueId)?.let {
                        it.isHudEnabled = false
                        sender.info("Successfully turns off.")
                    } ?: sender.warn("You are not available player!")
                }
            }
        }

    override fun start() {
        PLUGIN.getCommand(NAME_SPACE)?.setExecutor(command.createTabExecutor())
    }

    override fun reload(sender: Audience, resource: GlobalResource) {
    }

    override fun end() {
    }
}