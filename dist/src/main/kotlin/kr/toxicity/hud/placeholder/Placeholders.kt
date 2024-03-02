package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.manager.ConfigManager
import kr.toxicity.hud.util.armor
import org.bukkit.attribute.Attribute
import java.util.regex.Pattern

object Placeholders {
    private val castPattern = Pattern.compile("(\\((?<type>[a-zA-Z]+)\\))?")
    private val stringPattern = Pattern.compile("'(?<content>[\\w|\\W]+)'")

    val number = PlaceholderBuilder(
        java.lang.Number::class.java,
        0,
        mapOf(
            "health" to { player, _ ->
                player.bukkitPlayer.health
            },
            "food" to { player, _ ->
                player.bukkitPlayer.foodLevel
            },
            "armor" to { player, _ ->
                player.bukkitPlayer.armor
            },
            "air" to { player, _ ->
                player.bukkitPlayer.remainingAir
            },
            "max_health" to { player, _ ->
                player.bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
            },
            "max_air" to { player, _ ->
                player.bukkitPlayer.maximumAir
            },
            "level" to { player, _ ->
                player.bukkitPlayer.level
            }
        ),
    ) {
        it.toDoubleOrNull()
    }
    val string = PlaceholderBuilder(
        java.lang.String::class.java,
        "<none>",
        mapOf(
            "name" to { player, _ ->
                player.bukkitPlayer.name
            }
        )
    ) {
        val matcher = stringPattern.matcher(it)
        if (matcher.find()) matcher.group("content") else null
    }
    val boolean = PlaceholderBuilder(
        java.lang.Boolean::class.java,
        false,
        mapOf(
            "dead" to { player, _ ->
                player.bukkitPlayer.isDead
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

    class PlaceholderBuilder<T>(
        val clazz: Class<*>,
        val defaultValue: T,
        private val defaultMap: Map<String, (HudPlayer, List<String>) -> T>,
        val parser: (String) -> T?,
    ) {
        val map = HashMap(defaultMap)

        fun init() {
            map.clear()
            map += defaultMap
        }

        fun addParser(name: String, mapper: (HudPlayer, List<String>) -> T) {
            map[name] = mapper
        }
    }

    fun find(target: String): Placeholder<*> {
        val pattern = target.split(':')

        val matcher = castPattern.matcher(pattern[0])
        val first = matcher.replaceAll("")

        val args = if (pattern.size > 1) pattern[pattern.lastIndex].split(',') else emptyList()
        val get = types.values.firstNotNullOfOrNull {
            it.map[first]?.let { mapper ->
                it to mapper
            }
        } ?: types.values.firstNotNullOfOrNull {
            it.parser(first)?.let { value ->
                val func: (HudPlayer, List<String>) -> Any = { _, _ ->
                    value
                }
                it to func
            }
        } ?: throw RuntimeException("this placeholder not found: $first")

        val type = if (matcher.find()) matcher.group("type")?.let {
            types[it]
        } else null

        return object : Placeholder<Any> {
            override val clazz: Class<out Any>
                get() = type?.let {
                    type.clazz
                } ?: get.first.clazz

            override fun invoke(p1: HudPlayer): Any {
                var value: Any = get.second(p1, args)
                type?.let {
                    value = it.parser(value.toString()) ?: it.defaultValue
                }
                return value
            }
        }
    }

    fun parse(t: HudPlayer, target: String): String {
        var skip = false
        var stop = false
        val sb = StringBuilder()
        val parserSb = StringBuilder()
        target.forEach { char ->
            if (!skip) when (char) {
                '/' -> skip = true
                '[' -> {
                    stop = true
                }
                ']' -> {
                    val pattern = parserSb.toString().split(':')
                    val list = pattern[pattern.lastIndex].split(',')
                    string.map[pattern[0]]?.let {
                        sb.append(it(t, list))
                        return@forEach
                    }
                    boolean.map[pattern[0]]?.let {
                        sb.append(it(t, list))
                        return@forEach
                    }
                    number.map[pattern[0]]?.let {
                        sb.append(ConfigManager.numberFormat.format(it(t, list)))
                        return@forEach
                    }
                    parserSb.setLength(0)
                    stop = false
                }
                else -> {
                    if (!stop) sb.append(char)
                    else parserSb.append(char)
                }
            } else {
                skip = false
                sb.append(char)
            }
        }
        return sb.toString()
    }
}