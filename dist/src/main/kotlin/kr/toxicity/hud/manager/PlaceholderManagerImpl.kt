package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.PlaceholderManager
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.placeholder.PlaceholderContainer
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.api.update.PopupUpdateEvent
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlElement
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.placeholder.Placeholder
import kr.toxicity.hud.placeholder.PlaceholderBuilder
import kr.toxicity.hud.placeholder.PlaceholderSource
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.ifNull
import java.text.DecimalFormat
import java.util.*
import java.util.function.Function
import java.util.regex.Pattern

object PlaceholderManagerImpl : PlaceholderManager, BetterHudManager {

    private val castPattern = Pattern.compile("(\\((?<type>[a-zA-Z]+)\\))")
    private val stringPattern = Pattern.compile("'(?<content>[\\w|\\W]+)'")
    private val equationPatter = Pattern.compile("(@(?<equation>(([()\\-+*./%, ]|[a-zA-Z]|[0-9])+)))")

    private val doubleDecimal = DecimalFormat("#.###")

    private val number = PlaceholderContainerImpl(
        "number",
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
            "number" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    Function { p ->
                        p.variableMap[args[0]]?.toDoubleOrNull() ?: 0.0
                    }
                }
                .build(),
            "popup_index" to HudPlaceholder.of { _, u ->
                val iterator = (u as PopupUpdateEvent).iterator
                Function {
                    iterator.index
                }
            }
        ),
        {
            it.toDoubleOrNull()
        },
        {
            doubleDecimal.format(it)
        },
        mapOf(
            "evaluate" to { n, e ->
                TEquation(e.asString()).evaluate(n.toDouble())
            }
        ),
        { a, b ->
            DecimalFormat(b).format(a)
        }
    )
    private val string = PlaceholderContainerImpl(
        "string",
        java.lang.String::class.java,
        "<none>",
        mapOf(
            "string" to HudPlaceholder.builder<String>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    Function { p ->
                        p.variableMap[args[0]] ?: "<none>"
                    }
                }
                .build(),
        ),
        {
            val matcher = stringPattern.matcher(it)
            if (matcher.find()) matcher.group("content") else null
        },
        {
            it.toString()
        },
        mapOf(
            "trim" to { s, e ->
                if (e.asBoolean()) s.trim() else s
            },
            "join" to { s, e ->
                val j = e.asString()
                buildString {
                    val point = s.codePoints().toArray()
                    point.forEachIndexed { index, c ->
                        appendCodePoint(c)
                        if (index < point.lastIndex) append(j)
                    }
                }
            },
            "replace" to { s, e ->
                val obj = e.asObject()
                val from = obj["from"].ifNull("Cannot find 'from' section.").asString()
                val to = obj["to"].ifNull("Cannot find 'to' section.").asString()
                s.replace(from, to)
            }
        ),
        { a, _ ->
            a.toString()
        }
    )
    private val boolean = PlaceholderContainerImpl(
        "boolean",
        java.lang.Boolean::class.java,
        false,
        mapOf(
            "boolean" to HudPlaceholder.builder<Boolean>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    Function { p ->
                        p.variableMap[args[0]] == "true"
                    }
                }
                .build()
        ),
        {
            when (it) {
                "true" -> true
                "false" -> false
                else -> null
            }
        },
        {
            it.toString()
        },
        mapOf(
            "reversed" to { b, e ->
                if (e.asBoolean()) !b else b
            }
        ),
        { a, _ ->
            a.toString()
        }
    )

    private val types = mapOf(
        "number" to number,
        "boolean" to boolean,
        "string" to string
    )

    class PlaceholderContainerImpl<T, R>(
        val name: String,
        val clazz: Class<R>,
        val defaultValue: T,
        private val defaultMap: Map<String, HudPlaceholder<T>>,
        val parser: (String) -> T?,
        val stringMapper: (R) -> String,
        private val optionApplier: Map<String, (T, YamlElement) -> T> = emptyMap(),
        private val optionStringApplier: (R, String) -> String
    ) : PlaceholderContainer<T> {
        private val map = HashMap(defaultMap)

        fun init() {
            map.clear()
            map += defaultMap
        }

        fun get(key: String, option: YamlObject): HudPlaceholder<T>? {
            val get = map[key] ?: return null
            val appliers = option.mapNotNull { (key, element) ->
                optionApplier[key]?.let {
                    { value: T ->
                        it(value, element)
                    }
                }
            }
            return if (appliers.isEmpty()) get else object : HudPlaceholder<T> by get {
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, T> {
                    val func = get(args, reason)
                    return Function {
                        var value = func.apply(it)
                        appliers.forEach {
                            value = it(value)
                        }
                        value
                    }
                }
            }
        }

        fun stringValue(option: YamlObject): (Any) -> String {
            val applier = option[name]?.asString()?.let {
                { value: Any ->
                    optionStringApplier(clazz.cast(value), it)
                }
            } ?: { value: Any ->
                stringMapper(clazz.cast(value))
            }
            return { any ->
                if (clazz.isAssignableFrom(any.javaClass)) {
                    applier(any)
                } else any.toString()
            }
        }

        override fun addPlaceholder(name: String, placeholder: HudPlaceholder<T>) {
            map[name] = placeholder
        }

        override fun getAllPlaceholders(): Map<String, HudPlaceholder<*>> = Collections.unmodifiableMap(map)
    }

    fun find(target: String, source: PlaceholderSource): PlaceholderBuilder<*> {
        val (option, stringOption) = source.placeholderOption to source.stringPlaceholderFormat
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
            it.get(first, option)?.let { mapper ->
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

        val stringMapper = type?.stringValue(stringOption) ?: get.first.stringValue(stringOption)

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
                        return stringMapper(invoke(player))
                    }
                }
            }
        }
    }

    fun parse(target: String, source: PlaceholderSource): (UpdateEvent) -> (HudPlayer) -> String {
        var skip = false
        val builder = ArrayList<(UpdateEvent) -> (HudPlayer) -> String>()
        val sb = StringBuilder()
        target.forEach { char ->
            if (!skip) {
                when (char) {
                    '/' -> skip = true
                    '[' -> {
                        val build = sb.toString()
                        builder += {
                            {
                                build
                            }
                        }
                        sb.setLength(0)
                    }

                    ']' -> {
                        val result = sb.toString()
                        sb.setLength(0)
                        val find = find(result, source)
                        runCatching {
                            builder += { r ->
                                (find build r).let { b ->
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
            builder += {
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

    override fun reload(info: ReloadInfo, resource: GlobalResource) {
    }

    override fun end() {
    }
}