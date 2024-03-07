package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.PlaceholderManager
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.placeholder.PlaceholderContainer
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.BukkitEventUpdateEvent
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.placeholder.Placeholder
import kr.toxicity.hud.placeholder.PlaceholderBuilder
import kr.toxicity.hud.placeholder.PlaceholderTask
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityEvent
import org.bukkit.potion.PotionEffectType
import java.util.function.Function
import java.util.regex.Pattern

object PlaceholderManagerImpl: PlaceholderManager, MythicHudManager {
    private val castPattern = Pattern.compile("(\\((?<type>[a-zA-Z]+)\\))?")
    private val stringPattern = Pattern.compile("'(?<content>[\\w|\\W]+)'")
    private val equationPatter = Pattern.compile("(@(?<equation>(([()\\-+*/% ]|[a-zA-Z]|[0-9])+)))")

    private val updateTask = ArrayList<PlaceholderTask>()

    private val number: PlaceholderContainerImpl<Number> = PlaceholderContainerImpl(
        java.lang.Number::class.java,
        0.0,
        mapOf(
            "health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.health
                }
            },
            "food" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.foodLevel
                }
            },
            "armor" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.armor
                }
            },
            "air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.remainingAir
                }
            },
            "max_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
                }
            },
            "max_air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.maximumAir
                }
            },
            "level" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.level
                }
            },
            "hotbar_slot" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.inventory.heldItemSlot
                }
            },
            "number" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 0
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    return Function { p ->
                        p.variableMap[args[0]]?.toDoubleOrNull() ?: 0.0
                    }
                }
            },
            "potion_effect_duration" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 0
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    val potion = (runCatching {
                        NamespacedKey.fromString(args[0])?.let { key ->
                            Registry.EFFECT.get(key)
                        }
                    }.onFailure {
                        @Suppress("DEPRECATION")
                        PotionEffectType.getByName(args[0])
                    }.getOrNull() ?: throw RuntimeException("this potion effect doesn't exist: ${args[0]}"))
                    return Function { p ->
                        p.bukkitPlayer.getPotionEffect(potion)?.duration ?: 0
                    }
                }
            },
            "entity_max_health" to HudPlaceholder.of { _, e ->
                if (e is BukkitEventUpdateEvent) {
                    val event = e.event
                    if (event is EntityEvent) {
                        Function { _ ->
                            (event.entity as? LivingEntity)?.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 0
                        }
                    } else throw RuntimeException("Unsupported event.")
                } else throw RuntimeException("Unsupported event.")
            },
            "entity_health" to HudPlaceholder.of { _, e ->
                if (e is BukkitEventUpdateEvent) {
                    val event = e.event
                    if (event is EntityEvent) {
                        Function { _ ->
                            (event.entity as? LivingEntity)?.health ?: 0
                        }
                    } else throw RuntimeException("Unsupported event.")
                } else throw RuntimeException("Unsupported event.")
            }
        ),
    ) {
        it.toDoubleOrNull()
    }
    private val string = PlaceholderContainerImpl(
        java.lang.String::class.java,
        "<none>",
        mapOf(
            "name" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.name
                }
            },
            "gamemode" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.gameMode.name
                }
            },
            "string" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    return Function { p ->
                        p.variableMap[args[0]] ?: "<none>"
                    }
                }
            },
            "entity_name" to HudPlaceholder.of { _, e ->
                if (e is BukkitEventUpdateEvent) {
                    val event = e.event
                    if (event is EntityEvent) {
                        Function { _ ->
                            event.entity.name
                        }
                    } else throw RuntimeException("Unsupported event.")
                } else throw RuntimeException("Unsupported event.")
            }
        )
    ) {
        val matcher = stringPattern.matcher(it)
        if (matcher.find()) matcher.group("content") else null
    }
    private val boolean = PlaceholderContainerImpl(
        java.lang.Boolean::class.java,
        false,
        mapOf(
            "dead" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.isDead
                }
            },
            "boolean" to object : HudPlaceholder<Boolean> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Boolean> {
                    return Function { p ->
                        p.variableMap[args[0]] == "true"
                    }
                }
            }
        )
    ) {
        when (it) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }

    private val types = mapOf(
        "number" to number,
        "boolean" to boolean,
        "string" to string
    )

    class PlaceholderContainerImpl<T>(
        val clazz: Class<*>,
        val defaultValue: T,
        private val defaultMap: Map<String, HudPlaceholder<T>>,
        val parser: (String) -> T?,
    ): PlaceholderContainer<T> {
        val map = HashMap(defaultMap)

        fun init() {
            map.clear()
            map += defaultMap
        }

        override fun addPlaceholder(name: String, placeholder: HudPlaceholder<T>) {
            map[name] = placeholder
        }
    }

    fun find(target: String): PlaceholderBuilder<*> {
        val equation = equationPatter.matcher(target)
        val numberMapper: (Double) -> Double = if (equation.find()) {
            TEquation(equation.group("equation")).let { mapper ->
                { t ->
                    mapper.evaluate(t)
                }
            }
        } else {
            {
                it
            }
        }

        val pattern = equation.replaceAll("").split(':')

        val matcher = castPattern.matcher(pattern[0])
        val first = matcher.replaceAll("")

        val args = if (pattern.size > 1) pattern[pattern.lastIndex].split(',') else emptyList()
        val get = types.values.firstNotNullOfOrNull {
            it.map[first]?.let { mapper ->
                it to mapper
            }
        } ?: types.values.firstNotNullOfOrNull {
            it.parser(first)?.let { value ->
                val func: HudPlaceholder<Any> = HudPlaceholder.of { _, _ ->
                    Function {
                        value
                    }
                }
                it to func
            }
        } ?: throw RuntimeException("this placeholder not found: $first")
        if (get.second.requiredArgsLength > args.size) throw RuntimeException("the placeholder '$first' requires an argument sized by at least ${get.second.requiredArgsLength}.")

        val type = if (matcher.find()) matcher.group("type")?.let {
            types[it]
        } else null

        return object : PlaceholderBuilder<Any> {
            override val clazz: Class<out Any>
                get() = type?.let {
                    type.clazz
                } ?: get.first.clazz

            override fun build(reason: UpdateEvent): Placeholder<Any> {
                val second = get.second(args, reason)
                return object : Placeholder<Any> {
                    override val clazz: Class<out Any>
                        get() = type?.let {
                            type.clazz
                        } ?: get.first.clazz

                    override fun invoke(p1: HudPlayer): Any {
                        var value: Any = second.apply(p1)
                        type?.let {
                            value = it.parser(value.toString()) ?: it.defaultValue
                        }
                        (value as? Number)?.let {
                            value = numberMapper(it.toDouble())
                        }
                        return value
                    }
                }
            }
        }
    }

    fun parse(r: UpdateEvent, target: String): (HudPlayer) -> String {
        var skip = false
        val builder = ArrayList<(HudPlayer) -> String>()
        val sb = StringBuilder()
        target.forEach { char ->
            if (!skip) {
                when (char) {
                    '/' -> skip = true
                    '[' -> {
                        val build = sb.toString()
                        builder.add {
                            build
                        }
                        sb.setLength(0)
                    }
                    ']' -> {
                        val pattern = sb.toString().split(':')
                        val list = pattern[pattern.lastIndex].split(',')
                        sb.setLength(0)
                        string.map[pattern[0]]?.let {
                            val func = it(list, r)
                            builder.add { p ->
                                func.apply(p).toString()
                            }
                            return@forEach
                        }
                        boolean.map[pattern[0]]?.let {
                            val func = it(list, r)
                            builder.add { p ->
                                func.apply(p).toString()
                            }
                            return@forEach
                        }
                        number.map[pattern[0]]?.let {
                            val func = it(list, r)
                            builder.add { p ->
                                func.apply(p).toString()
                            }
                            return@forEach
                        }
                    }
                    else -> {
                        sb.append(char)
                    }
                }
            } else {
                skip = false
                sb.append(char)
            }
        }
        if (sb.isNotEmpty()) {
            val build = sb.toString()
            builder.add {
                build
            }
        }
        return { p ->
            val result = StringBuilder()
            builder.forEach {
                result.append(it(p))
            }
            result.toString()
        }
    }

    override fun getNumberContainer(): PlaceholderContainer<Number> = number
    override fun getBooleanContainer(): PlaceholderContainer<Boolean> = boolean
    override fun getStringContainer(): PlaceholderContainer<String> = string
    override fun start() {

    }

    override fun reload(resource: GlobalResource) {
        updateTask.clear()
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {

            DATA_FOLDER.subFolder("placeholders").forEachAllYaml { file, s, configurationSection ->
                runCatching {
                    val variable = configurationSection.getString("variable").ifNull("variable not set.")
                    val placeholder = configurationSection.getString("placeholder").ifNull("placeholder not set.")
                    val update = configurationSection.getInt("update", 1).coerceAtLeast(1)
                    updateTask.add(object : PlaceholderTask {
                        override val tick: Int
                            get() = update

                        override fun invoke(p1: HudPlayer) {
                            runCatching {
                                p1.variableMap[variable] = PlaceholderAPI.setPlaceholders(p1.bukkitPlayer, placeholder)
                            }
                        }
                    })
                }.onFailure { e ->
                    warn("Unable to read this placeholder task: $s in ${file.name}")
                    warn("Reason: ${e.message}")
                }
            }
        }
    }

    fun update(hudPlayer: HudPlayer) {
        val task = updateTask.filter {
            hudPlayer.tick % it.tick == 0L
        }
        if (task.isNotEmpty()) task {
            task.forEach {
                it(hudPlayer)
            }
        }
    }

    override fun end() {
    }
}