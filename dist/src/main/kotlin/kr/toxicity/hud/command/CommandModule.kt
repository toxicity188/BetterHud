package kr.toxicity.hud.command

import kr.toxicity.hud.util.NAME_SPACE
import kr.toxicity.hud.util.info
import kr.toxicity.hud.util.toComponent
import kr.toxicity.hud.util.warn
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
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
                sender.info("----------< ${index + 1} / ${moduleMap.size / 6 + 1} >----------".toComponent().color(NamedTextColor.WHITE))
                moduleMap.values.toList().subList(index * 6, ((index + 1) * 6).coerceAtMost(moduleMap.size)).forEach {
                    sender.info("/$name ".toComponent().color(NamedTextColor.YELLOW).append(it.usage()).append(" - ".toComponent().color(NamedTextColor.GRAY)).append(it.description()))
                }
                sender.info("----------------------------".toComponent().color(NamedTextColor.WHITE))
            }
            tabCompleter = { _, _ ->
                (0..(moduleMap.size / 6)).map {
                    (it + 1).toString()
                }
            }
        }
    }

    fun execute(sender: CommandSender, args: List<String>) {
        val totalArgs = args.ifEmpty { listOf("help") }
        (moduleMap[totalArgs[0]] ?: moduleMap.entries.firstOrNull { entry ->
            entry.value.aliases().contains(totalArgs[0])
        }?.value)?.let {
            if (it.requireOp() && !sender.isOp) {
                sender.warn("You are not OP!".toComponent())
                return
            }
            if (it.permission().isNotEmpty() && it.permission().none { permission -> sender.hasPermission(permission) }) {
                sender.warn("You do not have permission to do that!".toComponent())
                return
            }
            if (it.allowedSender().isNotEmpty() && it.allowedSender().none { type -> type.clazz.isAssignableFrom(sender.javaClass)}) {
                sender.warn("You are not list as an allowed sender type!".toComponent())
                return
            }
            val subList = totalArgs.subList(1, totalArgs.size)
            if (subList.size < it.length()) {
                sender.warn("Usage : /$name ".toComponent().append(it.usage()))
                return
            }
            it.execute(sender, subList)
        } ?: sender.info("Unknown command. try ".toComponent().append("/$name help".toComponent().color(NamedTextColor.RED)).append(" to find the command you want.".toComponent()))
    }

    fun tabComplete(sender: CommandSender, args: List<String>): List<String>? {
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

    fun createTabExecutor(): TabExecutor = object : TabExecutor {
        override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<String>): Boolean {
            execute(p0, p3.toList())
            return true
        }

        override fun onTabComplete(
            p0: CommandSender,
            p1: Command,
            p2: String,
            p3: Array<String>
        ): List<String>? {
            return tabComplete(p0, p3.toList())
        }
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
        var allowedSender = SenderType.entries.toList()
        var executer: (CommandSender, List<String>) -> Unit = { _, _ ->
        }
        var tabCompleter: (CommandSender, List<String>) -> List<String>? = { _, _ ->
            null
        }
        fun build() {
            moduleMap[subName] = object : CommandPlayer {
                override fun aliases(): List<String> = aliases
                override fun description(): Component = description
                override fun usage(): Component = usage
                override fun requireOp(): Boolean = requireOp
                override fun permission(): List<String> = permission
                override fun allowedSender(): List<SenderType> = allowedSender
                override fun length(): Int = length
                override fun execute(sender: CommandSender, args: List<String>) {
                    executer(sender, args)
                }
                override fun tabComplete(sender: CommandSender, args: List<String>): List<String>? = tabCompleter(sender, args)

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
