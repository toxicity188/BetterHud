package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.PlaceholderManager
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.placeholder.PlaceholderContainer
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.placeholder.Placeholder
import kr.toxicity.hud.placeholder.PlaceholderBuilder
import kr.toxicity.hud.resource.GlobalResource
import net.kyori.adventure.audience.Audience
import java.text.DecimalFormat
import java.util.Collections
import java.util.function.Function
import java.util.regex.Pattern

object PlaceholderManagerImpl : PlaceholderManager, BetterHudManager {
    private val castPattern = Pattern.compile("(\\((?<type>[a-zA-Z]+)\\))")
    private val stringPattern = Pattern.compile("'(?<content>[\\w|\\W]+)'")
    private val equationPatter = Pattern.compile("(@(?<equation>(([()\\-+*./% ]|[a-zA-Z]|[0-9])+)))")

    private val doubleDecimal = DecimalFormat("#.###")

    private val number = PlaceholderContainerImpl(
        java.lang.Number::class.java,
        0.0,
        mapOf<String, HudPlaceholder<Number>>(
            "popup_count" to HudPlaceholder.of { _, _ ->
                Function {
                    it.popupGroupIteratorMap.size
                }
            },
            "tick" to HudPlaceholder.of { _, _ ->
                Function {
                    it.tick
                }
            },
            "number" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    return Function { p ->
                        p.variableMap[args[0]]?.toDoubleOrNull() ?: 0.0
                    }
                }
            },
        ), {
            it.toDoubleOrNull()
        }, {
            doubleDecimal.format(it)
        }
    )
    private val string = PlaceholderContainerImpl(
        java.lang.String::class.java,
        "<none>",
        mapOf(
            "string" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    return Function { p ->
                        p.variableMap[args[0]] ?: "<none>"
                    }
                }
            },
        ), {
            val matcher = stringPattern.matcher(it)
            if (matcher.find()) matcher.group("content") else null
        }, {
            it.toString()
        }
    )
    private val boolean = PlaceholderContainerImpl(
        java.lang.Boolean::class.java,
        false,
        mapOf(
            "boolean" to object : HudPlaceholder<Boolean> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Boolean> {
                    return Function { p ->
                        p.variableMap[args[0]] == "true"
                    }
                }
            }
        ), {
            when (it) {
                "true" -> true
                "false" -> false
                else -> null
            }
        }, {
            it.toString()
        }
    )

    private val types = mapOf(
        "number" to number,
        "boolean" to boolean,
        "string" to string
    )

    class PlaceholderContainerImpl<T, R>(
        val clazz: Class<R>,
        val defaultValue: T,
        private val defaultMap: Map<String, HudPlaceholder<T>>,
        val parser: (String) -> T?,
        val stringMapper: (R) -> String
    ) : PlaceholderContainer<T> {
        val map = HashMap(defaultMap)

        fun init() {
            map.clear()
            map += defaultMap
        }

        fun stringValue(any: Any): String {
            return if (clazz.isAssignableFrom(any.javaClass)) stringMapper(clazz.cast(any))
            else any.toString()
        }

        override fun addPlaceholder(name: String, placeholder: HudPlaceholder<T>) {
            map[name] = placeholder
        }

        override fun getAllPlaceholders(): Map<String, HudPlaceholder<*>> = Collections.unmodifiableMap(map)
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

        val pattern = equation.replaceAll("")
        val head = pattern.substringBefore(':')

        val matcher = castPattern.matcher(head)
        val group = if (matcher.find()) matcher.group("type") else null
        val first = matcher.replaceAll("")

        val args = if (pattern.length > head.length + 1) pattern.substring(head.length + 1).split(',') else emptyList()
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

        val type = types[group]

        return object : PlaceholderBuilder<Any> {
            override val clazz: Class<out Any>
                get() = type?.clazz ?: get.first.clazz

            override fun build(reason: UpdateEvent): Placeholder<Any> {
                val second = get.second(args, reason)
                return object : Placeholder<Any> {
                    override val clazz: Class<out Any>
                        get() = type?.clazz ?: get.first.clazz

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

                    override fun stringValue(player: HudPlayer): String {
                        val value = invoke(player)
                        return type?.stringValue(value) ?: get.first.stringValue(value)
                    }
                }
            }
        }
    }

    fun parse(target: String): (UpdateEvent) -> (HudPlayer) -> String {
        var skip = false
        val builder = ArrayList<(UpdateEvent) -> (HudPlayer) -> String>()
        val sb = StringBuilder()
        target.forEach { char ->
            if (!skip) {
                when (char) {
                    '/' -> skip = true
                    '[' -> {
                        val build = sb.toString()
                        builder.add {
                            {
                                build
                            }
                        }
                        sb.setLength(0)
                    }

                    ']' -> {
                        val result = sb.toString()
                        sb.setLength(0)
                        val find = find(result)
                        runCatching {
                            builder.add { r ->
                                find.build(r).let { b ->
                                    {
                                        b.stringValue(it)
                                    }
                                }
                            }
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
                {
                    build
                }
            }
        }
        return { r ->
            builder.map {
                it(r)
            }.let { bb ->
                { p ->
                    val result = StringBuilder()
                    bb.forEach {
                        result.append(it(p))
                    }
                    result.toString()
                }
            }
        }
    }

    override fun getNumberContainer(): PlaceholderContainer<Number> = number
    override fun getBooleanContainer(): PlaceholderContainer<Boolean> = boolean
    override fun getStringContainer(): PlaceholderContainer<String> = string
    override fun start() {
    }

    override fun reload(sender: Audience, resource: GlobalResource) {
    }

    override fun end() {
    }
}