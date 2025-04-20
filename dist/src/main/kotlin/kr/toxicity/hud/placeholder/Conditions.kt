package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.util.forEachSubConfiguration
import kr.toxicity.hud.util.handleFailure
import kr.toxicity.hud.util.ifNull

object Conditions {
    fun parse(section: YamlObject, source: PlaceholderSource): ConditionBuilder {
        var value: ConditionBuilder = ConditionBuilder.alwaysTrue
        section.forEachSubConfiguration { s, yamlObject ->
            runCatching {
                val new = parse0(yamlObject, source)
                value = when (val gate = yamlObject["gate"]?.asString() ?: "and") {
                    "and" -> value and new
                    "or" -> value or new
                    else -> {
                        throw RuntimeException("this gate doesn't exist: $gate")
                    }
                }
            }.handleFailure {
                "Unable to load this condition: $s"
            }
        }
        return value
    }

    @Suppress("UNCHECKED_CAST")
    private fun parse0(section: YamlObject, source: PlaceholderSource): ConditionBuilder {
        val first = PlaceholderManagerImpl.find(section["first"]?.asString().ifNull { "first value not set." }, source)
        val second = PlaceholderManagerImpl.find(section["second"]?.asString().ifNull { "second value not set." }, source)
        val operationValue = section["operation"]?.asString().ifNull { "operation value not set" }

        if (first.clazz != second.clazz) throw RuntimeException("type mismatch: ${first.clazz.simpleName} and ${second.clazz.simpleName}")

        val operation = (Operations.find(first.clazz) ?: throw RuntimeException("unable to load valid operation. you need to call developer."))[section["operation"]?.asString().ifNull { operationValue }]
            .ifNull { "unsupported operation: $operationValue" } as (Any, Any) -> Boolean
        return ConditionBuilder { updateEvent ->
            val o1 = first build updateEvent
            val o2 = second build updateEvent
            ({ p ->
                operation(o1.value(p), o2.value(p))
            })
        }
    }
}