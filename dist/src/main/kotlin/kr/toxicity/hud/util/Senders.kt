package kr.toxicity.hud.util

import kr.toxicity.hud.manager.ConfigManagerImpl
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

fun Audience.info(message: Component) = sendMessage(EMPTY_COMPONENT.append(ConfigManagerImpl.info).append(message))
fun Audience.warn(message: Component) = sendMessage(EMPTY_COMPONENT.append(ConfigManagerImpl.warn).append(message))
fun Audience.info(message: String) = info(message.toComponent())
fun Audience.warn(message: String) = warn(message.toComponent())