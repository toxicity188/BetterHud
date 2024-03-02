package kr.toxicity.hud.command

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

enum class SenderType(val clazz: Class<out CommandSender>) {
    PLAYER(Player::class.java),
    CONSOLE(ConsoleCommandSender::class.java)
}