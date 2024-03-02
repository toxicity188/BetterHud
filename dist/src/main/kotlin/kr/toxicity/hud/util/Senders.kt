package kr.toxicity.hud.util

import kr.toxicity.hud.manager.ConfigManager
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

val CommandSender.audience
    get() = PLUGIN.audiences.sender(this)

fun CommandSender.info(message: Component) = audience.sendMessage(EMPTY_COMPONENT.append(ConfigManager.info).append(message))
fun CommandSender.warn(message: Component) = audience.sendMessage(EMPTY_COMPONENT.append(ConfigManager.warn).append(message))
fun CommandSender.info(message: String) = info(message.toComponent())
fun CommandSender.warn(message: String) = warn(message.toComponent())