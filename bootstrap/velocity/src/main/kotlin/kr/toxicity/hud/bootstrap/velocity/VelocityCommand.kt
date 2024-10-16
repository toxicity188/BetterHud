package kr.toxicity.hud.bootstrap.velocity

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kr.toxicity.hud.api.plugin.ReloadState.Failure
import kr.toxicity.hud.api.plugin.ReloadState.OnReload
import kr.toxicity.hud.api.plugin.ReloadState.Success
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.manager.CompassManagerImpl
import kr.toxicity.hud.manager.HudManagerImpl
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.manager.PopupManagerImpl
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.concurrent.CompletableFuture

class VelocityCommand {

    private data class SimpleCommandData(
        val usage: String,
        val description: String
    )

    private fun help(audience: Audience, page: Int, vararg message: SimpleCommandData) {
        audience.info("----------< $page / ${((message.size - 1) / 6) + 1} >----------".toComponent().color(
            NamedTextColor.WHITE))
        val p = (page * 6).coerceAtMost(message.size)
        message.toList().subList(
            (p - 6).coerceAtLeast(0),
            p
        ).forEach {
            audience.info("/hud ".toComponent().color(NamedTextColor.YELLOW).append(Component.text(it.usage)).append(" - ".toComponent().color(
                NamedTextColor.GRAY)).append(Component.text(it.description).color(NamedTextColor.WHITE)))
        }
        audience.info("----------------------------".toComponent().color(NamedTextColor.WHITE))
    }
    private val main = arrayOf(
        SimpleCommandData("reload", "Reload BetterHud."),
        SimpleCommandData("hud", "Hud-related command."),
        SimpleCommandData("compass", "Compass-related command."),
        SimpleCommandData("popup", "Popup-related command."),
        SimpleCommandData("turn", "Turn-related command."),
        SimpleCommandData("help", "Help command."),
    )
    private val hud = arrayOf(
        SimpleCommandData("hud add", "Adds the hud for a player."),
        SimpleCommandData("hud remove", "Removes the hud for a player."),
        SimpleCommandData("hud help", "Help command."),
    )
    private val compass = arrayOf(
        SimpleCommandData("compass add", "Adds the compass for a player."),
        SimpleCommandData("compass remove", "Removes the compass for a player."),
        SimpleCommandData("compass help", "Help command."),
    )
    private val popup = arrayOf(
        SimpleCommandData("popup add", "Adds the popup for a player."),
        SimpleCommandData("popup remove", "Removes the popup for a player."),
        SimpleCommandData("popup show", "Shows a popup for a player."),
        SimpleCommandData("popup hide", "Hides the popup for a player."),
        SimpleCommandData("popup help", "Help command."),
    )
    private val turn = arrayOf(
        SimpleCommandData("turn on", "Turns on your hud."),
        SimpleCommandData("turn off", "Turns off your hud."),
        SimpleCommandData("turn help", "Help command."),
    )

    private val brigadier = LiteralArgumentBuilder.literal<CommandSource>("betterhud")
        //Reload
        .then(LiteralArgumentBuilder.literal<CommandSource>("reload")
            .requires {
                it.hasPermission("$NAME_SPACE.reload")
            }.executes {
                it.source.info("Trying to reload. please wait...")
                CompletableFuture.runAsync {
                    when (val reload = PLUGIN.reload()) {
                        is OnReload -> {
                            it.source.warn("The plugin is still reloading!")
                        }
                        is Success -> {
                            it.source.info("Reload successful! (${reload.time} ms)")
                        }
                        is Failure -> {
                            it.source.info("Reload failed.")
                            it.source.info("Cause: ${reload.throwable.javaClass.simpleName}: ${reload.throwable.message}")
                        }
                    }
                }
                0
            }
        )
        //Hud
        .then(LiteralArgumentBuilder.literal<CommandSource>("hud")
            .requires {
                it.hasPermission("$NAME_SPACE.hud")
            }
            .then(LiteralArgumentBuilder.literal<CommandSource>("add")
                .requires {
                    it.hasPermission("$NAME_SPACE.hud.add")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { _, suggestionsBuilder ->
                        PlayerManagerImpl.allHudPlayer.forEach {
                            suggestionsBuilder.suggest(it.name())
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("hud", StringArgumentType.string())
                        .suggests { _, suggestionsBuilder ->
                            HudManagerImpl.allNames.forEach {
                                suggestionsBuilder.suggest(it)
                            }
                            suggestionsBuilder.buildFuture()
                        }
                        .executes {
                            val p = it.getArgument("player", String::class.java)
                            val h = it.getArgument("hud", String::class.java)
                            val hudPlayer = PlayerManagerImpl.getHudPlayer(p) ?: run {
                                it.source.warn("This player is not online: $p")
                                return@executes 0
                            }
                            val hud = HudManagerImpl.getHud(h) ?: run {
                                it.source.warn("This hud doesn't exist: $h")
                                return@executes 0
                            }
                            if (hudPlayer.hudObjects.add(hud)) it.source.info("Successfully added.")
                            else it.source.warn("Hud '${h}' is already active for this player.")
                            0
                        })
                    .executes {
                        it.source.info("usage: /hud hud add <player> <hud>")
                        0
                    }
                )
                .executes {
                    it.source.info("usage: /hud hud add <player> <hud>")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("remove")
                .requires {
                    it.hasPermission("$NAME_SPACE.hud.remove")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { _, suggestionsBuilder ->
                        PlayerManagerImpl.allHudPlayer.forEach {
                            suggestionsBuilder.suggest(it.name())
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("hud", StringArgumentType.string())
                        .suggests { _, suggestionsBuilder ->
                            HudManagerImpl.allNames.forEach {
                                suggestionsBuilder.suggest(it)
                            }
                            suggestionsBuilder.buildFuture()
                        }
                        .executes {
                            val p = it.getArgument("player", String::class.java)
                            val h = it.getArgument("hud", String::class.java)
                            val hudPlayer = PlayerManagerImpl.getHudPlayer(p) ?: run {
                                it.source.warn("This player is not online: $p")
                                return@executes 0
                            }
                            val hud = HudManagerImpl.getHud(h) ?: run {
                                it.source.warn("This hud doesn't exist: $h")
                                return@executes 0
                            }
                            if (hudPlayer.hudObjects.remove(hud)) it.source.info("Successfully removed.")
                            else it.source.warn("Hud '${h}' is not active for this player.")
                            0
                        })
                    .executes {
                        it.source.info("usage: /hud hud remove <player> <hud>")
                        0
                    }
                )
                .executes {
                    it.source.info("usage: /hud hud remove <player> <hud>")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("help")
                .requires {
                    it.hasPermission("$NAME_SPACE.hud.help")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, Int>("page", IntegerArgumentType.integer())
                    .suggests { _, suggestionsBuilder ->
                        for (i in 1..hud.size) {
                            suggestionsBuilder.suggest(i)
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes {
                        help(it.source, it.getArgument("page", Int::class.java), *hud)
                        0
                    }
                ).executes {
                    help(it.source, 1, *hud)
                    0
                }
            ).executes {
                help(it.source, 1, *hud)
                0
            }
        )
        //Compass
        .then(LiteralArgumentBuilder.literal<CommandSource>("compass")
            .requires {
                it.hasPermission("$NAME_SPACE.compass")
            }
            .then(LiteralArgumentBuilder.literal<CommandSource>("add")
                .requires {
                    it.hasPermission("$NAME_SPACE.compass.add")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { _, suggestionsBuilder ->
                        PlayerManagerImpl.allHudPlayer.forEach {
                            suggestionsBuilder.suggest(it.name())
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("compass", StringArgumentType.string())
                        .suggests { _, suggestionsBuilder ->
                            CompassManagerImpl.allNames.forEach {
                                suggestionsBuilder.suggest(it)
                            }
                            suggestionsBuilder.buildFuture()
                        }
                        .executes {
                            val p = it.getArgument("player", String::class.java)
                            val h = it.getArgument("compass", String::class.java)
                            val hudPlayer = PlayerManagerImpl.getHudPlayer(p) ?: run {
                                it.source.warn("This player is not online: $p")
                                return@executes 0
                            }
                            val compass = CompassManagerImpl.getCompass(h) ?: run {
                                it.source.warn("This compass doesn't exist: $h")
                                return@executes 0
                            }
                            if (hudPlayer.hudObjects.add(compass)) it.source.info("Successfully added.")
                            else it.source.warn("Compass '${h}' is already active for this player.")
                            0
                        })
                    .executes {
                        it.source.info("usage: /hud compass add <player> <compass>")
                        0
                    }
                )
                .executes {
                    it.source.info("usage: /hud compass add <player> <compass>")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("remove")
                .requires {
                    it.hasPermission("$NAME_SPACE.compass.remove")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { _, suggestionsBuilder ->
                        PlayerManagerImpl.allHudPlayer.forEach {
                            suggestionsBuilder.suggest(it.name())
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("compass", StringArgumentType.string())
                        .suggests { _, suggestionsBuilder ->
                            CompassManagerImpl.allNames.forEach {
                                suggestionsBuilder.suggest(it)
                            }
                            suggestionsBuilder.buildFuture()
                        }
                        .executes {
                            val p = it.getArgument("player", String::class.java)
                            val h = it.getArgument("compass", String::class.java)
                            val hudPlayer = PlayerManagerImpl.getHudPlayer(p) ?: run {
                                it.source.warn("This player is not online: $p")
                                return@executes 0
                            }
                            val compass = CompassManagerImpl.getCompass(h) ?: run {
                                it.source.warn("This compass doesn't exist: $h")
                                return@executes 0
                            }
                            if (hudPlayer.hudObjects.remove(compass)) it.source.info("Successfully removed.")
                            else it.source.warn("Compass '${h}' is not active for this player.")
                            0
                        })
                    .executes {
                        it.source.info("usage: /hud compass remove <player> <compass>")
                        0
                    }
                )
                .executes {
                    it.source.info("usage: /hud compass remove <player> <compass>")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("help")
                .requires {
                    it.hasPermission("$NAME_SPACE.compass.help")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, Int>("page", IntegerArgumentType.integer())
                    .suggests { _, suggestionsBuilder ->
                        for (i in 1..compass.size) {
                            suggestionsBuilder.suggest(i)
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes {
                        help(it.source, it.getArgument("page", Int::class.java), *compass)
                        0
                    }
                ).executes {
                    help(it.source, 1, *compass)
                    0
                }
            ).executes {
                help(it.source, 1, *compass)
                0
            }
        )
        //Popup
        .then(LiteralArgumentBuilder.literal<CommandSource>("popup")
            .requires {
                it.hasPermission("$NAME_SPACE.popup")
            }
            .then(LiteralArgumentBuilder.literal<CommandSource>("add")
                .requires {
                    it.hasPermission("$NAME_SPACE.popup.add")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { _, suggestionsBuilder ->
                        PlayerManagerImpl.allHudPlayer.forEach {
                            suggestionsBuilder.suggest(it.name())
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("popup", StringArgumentType.string())
                        .suggests { _, suggestionsBuilder ->
                            PopupManagerImpl.allNames.forEach {
                                suggestionsBuilder.suggest(it)
                            }
                            suggestionsBuilder.buildFuture()
                        }
                        .executes {
                            val p = it.getArgument("player", String::class.java)
                            val h = it.getArgument("popup", String::class.java)
                            val hudPlayer = PlayerManagerImpl.getHudPlayer(p) ?: run {
                                it.source.warn("This player is not online: $p")
                                return@executes 0
                            }
                            val popup = PopupManagerImpl.getPopup(h) ?: run {
                                it.source.warn("This popup doesn't exist: $h")
                                return@executes 0
                            }
                            if (hudPlayer.hudObjects.add(popup)) it.source.info("Successfully added.")
                            else it.source.warn("Popup '${h}' is already active for this player.")
                            0
                        })
                    .executes {
                        it.source.info("usage: /hud popup add <player> <popup>")
                        0
                    }
                )
                .executes {
                    it.source.info("usage: /hud popup add <player> <popup>")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("remove")
                .requires {
                    it.hasPermission("$NAME_SPACE.popup.remove")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { _, suggestionsBuilder ->
                        PlayerManagerImpl.allHudPlayer.forEach {
                            suggestionsBuilder.suggest(it.name())
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("popup", StringArgumentType.string())
                        .suggests { _, suggestionsBuilder ->
                            PopupManagerImpl.allNames.forEach {
                                suggestionsBuilder.suggest(it)
                            }
                            suggestionsBuilder.buildFuture()
                        }
                        .executes {
                            val p = it.getArgument("player", String::class.java)
                            val h = it.getArgument("popup", String::class.java)
                            val hudPlayer = PlayerManagerImpl.getHudPlayer(p) ?: run {
                                it.source.warn("This player is not online: $p")
                                return@executes 0
                            }
                            val popup = PopupManagerImpl.getPopup(h) ?: run {
                                it.source.warn("This popup doesn't exist: $h")
                                return@executes 0
                            }
                            if (hudPlayer.hudObjects.remove(popup)) it.source.info("Successfully removed.")
                            else it.source.warn("Popup '${h}' is not active for this player.")
                            0
                        })
                    .executes {
                        it.source.info("usage: /hud remove add <player> <popup>")
                        0
                    }
                )
                .executes {
                    it.source.info("usage: /hud remove add <player> <popup>")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("show")
                .requires {
                    it.hasPermission("$NAME_SPACE.popup.show")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { _, suggestionsBuilder ->
                        PlayerManagerImpl.allHudPlayer.forEach {
                            suggestionsBuilder.suggest(it.name())
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("popup", StringArgumentType.string())
                        .suggests { _, suggestionsBuilder ->
                            PopupManagerImpl.allNames.forEach {
                                suggestionsBuilder.suggest(it)
                            }
                            suggestionsBuilder.buildFuture()
                        }
                        .executes {
                            val p = it.getArgument("player", String::class.java)
                            val h = it.getArgument("popup", String::class.java)
                            val hudPlayer = PlayerManagerImpl.getHudPlayer(p) ?: run {
                                it.source.warn("This player is not online: $p")
                                return@executes 0
                            }
                            val popup = PopupManagerImpl.getPopup(h) ?: run {
                                it.source.warn("This popup doesn't exist: $h")
                                return@executes 0
                            }
                            runCatching {
                                if (popup.show(UpdateEvent.EMPTY, hudPlayer) != null) it.source.info("Popup was successfully displayed.")
                                else it.source.warn("Failed to show this popup.")
                            }.onFailure { e ->
                                it.source.warn("Unable to show this popup in command.")
                                it.source.warn("Reason: ${e.message}")
                            }
                            0
                        })
                    .executes {
                        it.source.info("usage: /hud popup show <player> <popup>")
                        0
                    }
                )
                .executes {
                    it.source.info("usage: /hud popup show <player> <popup>")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("hide")
                .requires {
                    it.hasPermission("$NAME_SPACE.hide.show")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { _, suggestionsBuilder ->
                        PlayerManagerImpl.allHudPlayer.forEach {
                            suggestionsBuilder.suggest(it.name())
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("popup", StringArgumentType.string())
                        .suggests { _, suggestionsBuilder ->
                            PopupManagerImpl.allNames.forEach {
                                suggestionsBuilder.suggest(it)
                            }
                            suggestionsBuilder.buildFuture()
                        }
                        .executes {
                            val p = it.getArgument("player", String::class.java)
                            val h = it.getArgument("popup", String::class.java)
                            val hudPlayer = PlayerManagerImpl.getHudPlayer(p) ?: run {
                                it.source.warn("This player is not online: $p")
                                return@executes 0
                            }
                            val popup = PopupManagerImpl.getPopup(h) ?: run {
                                it.source.warn("This popup doesn't exist: $h")
                                return@executes 0
                            }
                            runCatching {
                                if (popup.hide(hudPlayer)) it.source.info("Popup was successfully removed.")
                                else it.source.warn("Failed to remove this popup.")
                            }.onFailure { e ->
                                it.source.warn("Unable to show this popup in a command.")
                                it.source.warn("Reason: ${e.message}")
                            }
                            0
                        })
                    .executes {
                        it.source.info("usage: /hud hide show <player> <popup>")
                        0
                    }
                )
                .executes {
                    it.source.info("usage: /hud hide show <player> <popup>")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("help")
                .requires {
                    it.hasPermission("$NAME_SPACE.popup.help")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, Int>("page", IntegerArgumentType.integer())
                    .suggests { _, suggestionsBuilder ->
                        for (i in 1..popup.size) {
                            suggestionsBuilder.suggest(i)
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes {
                        help(it.source, it.getArgument("page", Int::class.java), *popup)
                        0
                    }
                ).executes {
                    help(it.source, 1, *popup)
                    0
                }
            ).executes {
                help(it.source, 1, *popup)
                0
            }
        )
        //Turn
        .then(LiteralArgumentBuilder.literal<CommandSource>("turn")
            .requires {
                it.hasPermission("$NAME_SPACE.turn")
            }
            .then(LiteralArgumentBuilder.literal<CommandSource>("on")
                .requires {
                    it is Player && it.hasPermission("$NAME_SPACE.turn.on")
                }
                .executes {
                    PlayerManagerImpl.getHudPlayer((it.source as Player).uniqueId)?.let { p ->
                        p.isHudEnabled = true
                        it.source.info("Successfully turned the hud on.")
                    } ?: it.source.info("Unable to turns on your hud.")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("off")
                .requires {
                    it is Player && it.hasPermission("$NAME_SPACE.turn.off")
                }
                .executes {
                    PlayerManagerImpl.getHudPlayer((it.source as Player).uniqueId)?.let { p ->
                        p.isHudEnabled = true
                        it.source.info("Successfully turned the hud off.")
                    } ?: it.source.info("Unable to turns off your hud.")
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("help")
                .requires {
                    it.hasPermission("$NAME_SPACE.turn.help")
                }
                .then(RequiredArgumentBuilder.argument<CommandSource, Int>("page", IntegerArgumentType.integer())
                    .suggests { _, suggestionsBuilder ->
                        for (i in 1..turn.size) {
                            suggestionsBuilder.suggest(i)
                        }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes {
                        help(it.source, it.getArgument("page", Int::class.java), *turn)
                        0
                    }
                ).executes {
                    help(it.source, 1, *turn)
                    0
                }
            ).executes {
                help(it.source, 1, *turn)
                0
            }
        )
        //Help
        .then(LiteralArgumentBuilder.literal<CommandSource>("help")
            .requires {
                it.hasPermission("$NAME_SPACE.help")
            }
            .then(RequiredArgumentBuilder.argument<CommandSource, Int>("page", IntegerArgumentType.integer())
                .suggests { _, suggestionsBuilder ->
                    for (i in 1..main.size) {
                        suggestionsBuilder.suggest(i)
                    }
                    suggestionsBuilder.buildFuture()
                }
                .executes {
                    help(it.source, it.getArgument("page", Int::class.java), *main)
                    0
                }
            ).executes {
                help(it.source, 1, *main)
                0
            }
        ).executes {
            help(it.source, 1, *main)
            0
        }
        .build()

    fun register(proxyServer: ProxyServer) {
        BrigadierCommand(LiteralArgumentBuilder.literal<CommandSource>("hud").redirect(brigadier)).add(proxyServer)
        BrigadierCommand(brigadier).add(proxyServer)
    }
    private fun BrigadierCommand.add(server: ProxyServer) {
        server.commandManager.register(
            server.commandManager.metaBuilder(this).build(),
            this
        )
    }
}
