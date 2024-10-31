package kr.toxicity.hud.command

import net.kyori.adventure.text.Component

interface CommandPlayer {
    fun aliases(): List<String>
    fun description(): Component
    fun usage(): Component
    fun requireOp(): Boolean
    fun permission(): List<String>
    fun length(): Int
    fun allowedSender(): List<CommandSourceWrapper.Type>
    fun execute(sender: CommandSourceWrapper, args: List<String>)
    fun tabComplete(sender: CommandSourceWrapper, args: List<String>): List<String>?
}