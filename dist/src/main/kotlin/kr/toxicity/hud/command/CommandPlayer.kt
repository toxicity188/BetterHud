package kr.toxicity.hud.command

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

interface CommandPlayer {
    fun aliases(): List<String>
    fun description(): Component
    fun usage(): Component
    fun requireOp(): Boolean
    fun permission(): List<String>
    fun length(): Int
    fun allowedSender(): List<SenderType>
    fun execute(sender: CommandSender, args: List<String>)
    fun tabComplete(sender: CommandSender, args: List<String>): List<String>?
}