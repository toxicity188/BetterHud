package kr.toxicity.hud.command

import kr.toxicity.hud.api.adapter.CommandSourceWrapper
import kr.toxicity.hud.util.NAME_SPACE
import kr.toxicity.hud.util.info
import kr.toxicity.hud.util.toComponent
import kr.toxicity.hud.util.warn
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.*

class CommandModule(
    val name: String
) {

    private val moduleMap = TreeMap<String, CommandPlayer>()
    init {
        addCommand("help") {
            aliases = listOf("h", "도움말")
            description = "Check the command list.".toComponent()
            usage = "help".toComponent()
            permission = listOf("$NAME_SPACE.help")
            executer = { sender, args ->
                val index = (if (args.isNotEmpty()) {
                    try {
                        args[0].toInt() - 1
                    } catch (ex: NumberFormatException) {
                        0
                    }
                } else 0).coerceAtLeast(0).coerceAtMost(moduleMap.size / 6)
                sender.audience().info("----------< ${index + 1} / ${moduleMap.size / 6 + 1} >----------".toComponent().color(NamedTextColor.WHITE))
                moduleMap.values.toList().subList(index * 6, ((index + 1) * 6).coerceAtMost(moduleMap.size)).forEach {
                    sender.audience().info("/$name ".toComponent().color(NamedTextColor.YELLOW).append(it.usage()).append(" - ".toComponent().color(NamedTextColor.GRAY)).append(it.description()))
                }
                sender.audience().info("----------------------------".toComponent().color(NamedTextColor.WHITE))
            }
            tabCompleter = { _, _ ->
                (0..(moduleMap.size / 6)).map {
                    (it + 1).toString()
                }
            }
        }
    }

    fun execute(sender: CommandSourceWrapper, args: List<String>) {
        val totalArgs = args.ifEmpty { listOf("help") }
        (moduleMap[totalArgs[0]] ?: moduleMap.entries.firstOrNull { entry ->
            entry.value.aliases().contains(totalArgs[0])
        }?.value)?.let {
            if (it.requireOp() && !sender.isOp) {
                sender.audience().warn("You are not OP!".toComponent())
                return
            }
            if (it.permission().isNotEmpty() && it.permission().none { permission -> sender.hasPermission(permission) }) {
                sender.audience().warn("You do not have permission to do that!".toComponent())
                return
            }
            if (it.allowedSender().isNotEmpty() && it.allowedSender().none { type -> sender.type() == type }) {
                sender.audience().warn("You are not list as an allowed sender type!".toComponent())
                return
            }
            val subList = totalArgs.subList(1, totalArgs.size)
            if (subList.size < it.length()) {
                sender.audience().warn("Usage : /$name ".toComponent().append(it.usage()))
                return
            }
            it.execute(sender, subList)
        } ?: sender.audience().info("Unknown command. try ".toComponent().append("/$name help".toComponent().color(NamedTextColor.RED)).append(" to find the command you want.".toComponent()))
    }

    fun tabComplete(sender: CommandSourceWrapper, args: List<String>): List<String>? {
        val newList = args.toList().subList(1, args.size)
        return if (newList.isEmpty()) moduleMap.keys.toList() else moduleMap[args[0]]?.tabComplete(sender, args.subList(1, args.size))
    }

    fun addCommand(name: String, block: Builder.() -> Unit): CommandModule {
        Builder(name).apply(block).build()
        return this
    }
    fun addCommandModule(name: String, block: Builder.() -> Unit, block2: CommandModule.() -> Unit): CommandModule {
        ModuleBuilder(name).apply {
            builder.block()
            module.block2()
        }.build()
        return this
    }

    inner class Builder(
        private val subName: String
    ) {
        var aliases: List<String> = emptyList()
        var description: Component = "Unknown description".toComponent()
        var usage: Component = "Unknown usage".toComponent()
        var permission: List<String> = emptyList()
        var length: Int = 0
        var requireOp: Boolean = false
        var allowedSender = CommandSourceWrapper.Type.entries.toList()
        var executer: (CommandSourceWrapper, List<String>) -> Unit = { _, _ ->
        }
        var tabCompleter: (CommandSourceWrapper, List<String>) -> List<String>? = { _, _ ->
            null
        }
        fun build() {
            moduleMap[subName] = object : CommandPlayer {
                override fun aliases(): List<String> = aliases
                override fun description(): Component = description
                override fun usage(): Component = usage
                override fun requireOp(): Boolean = requireOp
                override fun permission(): List<String> = permission
                override fun allowedSender(): List<CommandSourceWrapper.Type> = allowedSender
                override fun length(): Int = length
                override fun execute(sender: CommandSourceWrapper, args: List<String>) {
                    executer(sender, args)
                }
                override fun tabComplete(sender: CommandSourceWrapper, args: List<String>): List<String>? = tabCompleter(sender, args)

            }
        }
    }
    inner class ModuleBuilder(
        private val subName: String
    ) {
        val module = CommandModule("$name $subName")

        val builder = Builder(subName).apply {
            description = "${subName}-related command.".toComponent()
            usage = subName.toComponent()
            executer = { sender, args ->
                module.execute(sender, args)
            }
            tabCompleter = { sender, args ->
                module.tabComplete(sender, args)
            }
        }

        fun build() {
            builder.build()
        }
    }
}
