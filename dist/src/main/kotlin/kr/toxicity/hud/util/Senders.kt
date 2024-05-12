package kr.toxicity.hud.util

import kr.toxicity.hud.manager.ConfigManagerImpl
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

val CommandSender.audience
    get() = PLUGIN.audiences.sender(this)

fun CommandSender.info(message: Component) = audience.info(message)
fun CommandSender.warn(message: Component) = audience.warn(message)
fun CommandSender.info(message: String) = info(message.toComponent())
fun CommandSender.warn(message: String) = warn(message.toComponent())
fun Audience.info(message: Component) = sendMessage(EMPTY_COMPONENT.append(ConfigManagerImpl.info).append(message))
fun Audience.warn(message: Component) = sendMessage(EMPTY_COMPONENT.append(ConfigManagerImpl.warn).append(message))
fun Audience.info(message: String) = info(message.toComponent())
fun Audience.warn(message: String) = warn(message.toComponent())