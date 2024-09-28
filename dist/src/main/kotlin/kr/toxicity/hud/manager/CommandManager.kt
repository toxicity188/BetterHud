package kr.toxicity.hud.manager

import kr.toxicity.hud.api.adapter.CommandSourceWrapper
import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.PointedLocation
import kr.toxicity.hud.api.player.PointedLocationSource
import kr.toxicity.hud.api.plugin.ReloadState
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.command.CommandModule
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import java.util.concurrent.CompletableFuture

object CommandManager: BetterHudManager {

    val command = CommandModule("hud")
        .addCommand("reload") {
            aliases = listOf("re", "rl", "리로드")
            description = "Reload BetterHud.".toComponent()
            usage = "reload".toComponent()
            permission = listOf("$NAME_SPACE.reload")
            executer = { s, _ ->
                s.audience().info("Trying to reload. please wait...")
                CompletableFuture.runAsync {
                    val reload = PLUGIN.reload()
                    when (reload.state) {
                        ReloadState.STILL_ON_RELOAD -> {
                            s.audience().warn("The plugin is still reloading!")
                        }
                        ReloadState.SUCCESS -> {
                            s.audience().info("Reload successful! (${reload.time} ms)")
                        }
                        ReloadState.FAIL -> {
                            s.audience().info("Reload failed.")
                            s.audience().info("Check your server log to find the problem.")
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
                description = "Adds the hud for a player.".toComponent()
                usage = "add <player> <hud>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.hud.add")
                executer = exec@ { s, a ->
                    val hudPlayer = PlayerManagerImpl.getHudPlayer(a[0]) ?: run {
                        s.audience().warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = HudManagerImpl.getHud(a[1]) ?: run {
                        s.audience().warn("This hud doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (hudPlayer.hudObjects.add(hud)) s.audience().info("Successfully added.")
                    else s.audience().warn("Hud '${a[1]}' is already active for this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
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
                description = "Removes the hud for a player.".toComponent()
                usage = "remove <player> <hud>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.hud.remove")
                executer = exec@ { s, a ->
                    val hudPlayer = PlayerManagerImpl.getHudPlayer(a[0]) ?: run {
                        s.audience().warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = HudManagerImpl.getHud(a[1]) ?: run {
                        s.audience().warn("This hud doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (hudPlayer.hudObjects.remove(hud)) s.audience().info("Successfully removed.")
                    else s.audience().warn("Hud '${a[1]}' is not active for this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
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
                description = "Adds the compass for a player.".toComponent()
                usage = "add <player> <compass>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.compass.add")
                executer = exec@ { s, a ->
                    val hudPlayer = PlayerManagerImpl.getHudPlayer(a[0]) ?: run {
                        s.audience().warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val compass = CompassManagerImpl.getCompass(a[1]) ?: run {
                        s.audience().warn("This compass doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (hudPlayer.hudObjects.add(compass)) s.audience().info("Successfully added.")
                    else s.audience().warn("compass '${a[1]}' is already active for this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
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
                description = "Removes the compass for a player.".toComponent()
                usage = "remove <player> <compass>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.compass.remove")
                executer = exec@ { s, a ->
                    val hudPlayer = PlayerManagerImpl.getHudPlayer(a[0]) ?: run {
                        s.audience().warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val compass = CompassManagerImpl.getCompass(a[1]) ?: run {
                        s.audience().warn("This compass doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (hudPlayer.hudObjects.remove(compass)) s.audience().info("Successfully removed.")
                    else s.audience().warn("compass '${a[1]}' is not active for this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
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
                description = "Adds a popup for a player.".toComponent()
                usage = "add <player> <popup>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.popup.add")
                executer = exec@ { s, a ->
                    val hudPlayer = PlayerManagerImpl.getHudPlayer(a[0]) ?: run {
                        s.audience().warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.audience().warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (hudPlayer.hudObjects.add(hud)) s.audience().info("Successfully added.")
                    else s.audience().warn("Popup '${a[1]}' is already active for this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
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
                description = "Removes a popup from a hudPlayer.".toComponent()
                usage = "remove <player> <popup>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.popup.remove")
                executer = exec@ { s, a ->
                    val hudPlayer = PlayerManagerImpl.getHudPlayer(a[0]) ?: run {
                        s.audience().warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val hud = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.audience().warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    if (hudPlayer.hudObjects.remove(hud)) s.audience().info("Successfully removed.")
                    else s.audience().warn("Popup '${a[1]}' is not active for this player.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
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
                description = "Shows a popup for a player.".toComponent()
                usage = "show <player> <popup>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.popup.show")
                executer = exec@ { s, a ->
                    val hudPlayer = PlayerManagerImpl.getHudPlayer(a[0]) ?: run {
                        s.audience().warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val popup = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.audience().warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    runCatching {
                        if (popup.show(UpdateEvent.EMPTY, hudPlayer) != null) s.audience().info("Popup was successfully displayed.")
                        else s.audience().warn("Failed to show this popup.")
                    }.onFailure { e ->
                        s.audience().warn("Unable to show this popup in command.")
                        s.audience().warn("Reason: ${e.message}")
                    }
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
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
                description = "Hides a popup for a player.".toComponent()
                usage = "hide <player> <popup>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.popup.hide")
                executer = exec@ { s, a ->
                    val hudPlayer = PlayerManagerImpl.getHudPlayer(a[0]) ?: run {
                        s.audience().warn("This player is not online: ${a[0]}")
                        return@exec
                    }
                    val popup = PopupManagerImpl.getPopup(a[1]) ?: run {
                        s.audience().warn("This popup doesn't exist: ${a[1]}")
                        return@exec
                    }
                    runCatching {
                        if (popup.hide(hudPlayer)) s.audience().info("Popup was successfully removed.")
                        else s.audience().warn("Failed to remove this popup.")
                    }.onFailure { e ->
                        s.audience().warn("Unable to show this popup in a command.")
                        s.audience().warn("Reason: ${e.message}")
                    }
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
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
            allowedSender = listOf(CommandSourceWrapper.Type.PLAYER)
        }) {
            addCommand("on") {
                description = "Turns on your hud.".toComponent()
                usage = "on".toComponent()
                permission = listOf("$NAME_SPACE.turn.on")
                executer = { sender, _ ->
                    (sender as HudPlayer).isHudEnabled = true
                    sender.audience().info("Successfully turned the hud on.")
                }
            }
            addCommand("off") {
                description = "Turns off your hud.".toComponent()
                usage = "off".toComponent()
                permission = listOf("$NAME_SPACE.turn.off")
                executer = { sender, _ ->
                    (sender as HudPlayer).isHudEnabled = false
                    sender.audience().info("Successfully turned the hud off.")
                }
            }
        }
        .addCommandModule("pointer", {
            aliases = listOf("p")
            permission = listOf("$NAME_SPACE.pointer")
        }) {
            addCommand("set") {
                description = "Sets the compass pointer location of some player.".toComponent()
                usage = "set <player> <name> <world> <x,y,z> [icon]".toComponent()
                length = 4
                permission = listOf("$NAME_SPACE.pointer.set")
                executer = exec@ { sender, args ->
                    (PlayerManagerImpl.getHudPlayer(args[0]) ?: return@exec sender.audience().warn("Unable to find this player: ${args[0]}")).pointers().add(args[3].split(',').apply {
                        if (size < 3) return@exec sender.audience().warn("Location format must be x,y,z.")
                    }.let {
                        PointedLocation(
                            PointedLocationSource.INTERNAL,
                            args[1],
                            if (args.size > 4) args[4] else null,
                            LocationWrapper(
                                BOOTSTRAP.world(args[2]) ?: return@exec sender.audience().warn("Unable to find this world: ${args[2]}"),
                                it[0].toDoubleOrNull() ?: return@exec sender.audience().warn("X coordinate is not a number: ${it[0]}"),
                                it[1].toDoubleOrNull() ?: return@exec sender.audience().warn("Y coordinate is not a number: ${it[1]}"),
                                it[2].toDoubleOrNull() ?: return@exec sender.audience().warn("Z coordinate is not a number: ${it[2]}"),
                                0F,
                                0F
                            )
                        ).apply {
                            sender.audience().info("Located successfully in ${location.x},${location.y},${location.z} in ${location.world.name}: $name")
                        }
                    })
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
                        }.filter {
                            it.contains(a[0])
                        }
                        3 -> BOOTSTRAP.worlds().map {
                            it.name
                        }.filter {
                            it.contains(a[2])
                        }
                        4 -> listOf("0,0,0")
                        else -> null
                    }
                }
            }
            addCommand("clear") {
                description = "Clears the compass pointer location of some player.".toComponent()
                usage = "clear <player>".toComponent()
                length = 1
                permission = listOf("$NAME_SPACE.pointer.clear")
                executer = exec@ { sender, args ->
                    (PlayerManagerImpl.getHudPlayer(args[0]) ?: return@exec sender.audience().warn("Unable to find this player: ${args[0]}")).pointers().clear()
                    sender.audience().info("Cleared successfully.")
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
                        }.filter {
                            it.contains(a[0])
                        }
                        else -> null
                    }
                }
            }
            addCommand("remove") {
                description = "Removes the compass pointer location of some player.".toComponent()
                usage = "remove <player> <name>".toComponent()
                length = 2
                permission = listOf("$NAME_SPACE.pointer.remove")
                executer = exec@ { sender, args ->
                    if ((PlayerManagerImpl.getHudPlayer(args[0]) ?: return@exec sender.audience().warn("Unable to find this player: ${args[0]}")).pointers().removeIf {
                        it.name == args[1]
                    }) {
                        sender.audience().info("Removed successfully.")
                    } else {
                        sender.audience().warn("This pointer doesn't exist: ${args[0]}")
                    }
                }
                tabCompleter = { _, a ->
                    when (a.size) {
                        1 -> PlayerManagerImpl.allHudPlayer.map {
                            it.name()
                        }.filter {
                            it.contains(a[0])
                        }
                        2 -> PlayerManagerImpl.getHudPlayer(a[1])?.pointers()?.map {
                            it.name
                        } ?: emptyList()
                        else -> null
                    }
                }
            }
        }

    override fun start() {
    }

    override fun reload(sender: Audience, resource: GlobalResource) {
    }

    override fun end() {
    }
}
