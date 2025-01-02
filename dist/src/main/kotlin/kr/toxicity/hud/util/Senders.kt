package kr.toxicity.hud.util

import kr.toxicity.command.BetterCommandSource
import kr.toxicity.command.impl.BetterCommand
import kr.toxicity.hud.manager.CommandManager
import net.kyori.adventure.text.Component

fun BetterCommandSource.info(message: Component) = audience().sendMessage(EMPTY_COMPONENT.append(CommandManager.library.prefix(this, BetterCommand.PrefixType.INFO)).append(message))
fun BetterCommandSource.warn(message: Component) = audience().sendMessage(EMPTY_COMPONENT.append(CommandManager.library.prefix(this, BetterCommand.PrefixType.WARN)).append(message))
fun BetterCommandSource.info(message: String) = info(message.toComponent())
fun BetterCommandSource.warn(message: String) = warn(message.toComponent())