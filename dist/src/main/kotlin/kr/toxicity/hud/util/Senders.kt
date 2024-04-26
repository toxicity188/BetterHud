package kr.toxicity.hud.util

import kr.toxicity.hud.manager.ConfigManagerImpl
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

val CommandSender.audience
    get() = PLUGIN.audiences.sender(this)

fun CommandSender.info(message: Component) = audience.sendMessage(EMPTY_COMPONENT.append(ConfigManagerImpl.info).append(message))
fun CommandSender.warn(message: Component) = audience.sendMessage(EMPTY_COMPONENT.append(ConfigManagerImpl.warn).append(message))
fun CommandSender.info(message: String) = info(message.toComponent())
fun CommandSender.warn(message: String) = warn(message.toComponent())