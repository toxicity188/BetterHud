package kr.toxicity.hud.placeholder

object Operations {
    val number = Operation<Number>(mapOf(
        "==" to { a, b ->
            a.toDouble() == b.toDouble()
        },
        "!=" to { a, b ->
            a.toDouble() != b.toDouble()
        },
        ">=" to { a, b ->
            a.toDouble() >= b.toDouble()
        },
        "<=" to { a, b ->
            a.toDouble() <= b.toDouble()
        },
        "<" to { a, b ->
            a.toDouble() < b.toDouble()
        },
        ">" to { a, b ->
            a.toDouble() > b.toDouble()
        }
    ))

    val boolean = Operation<Boolean>(mapOf(
        "==" to { a, b ->
            a == b
        },
        "!=" to { a, b ->
            a != b
        }
    ))
    val string = Operation<Double>(mapOf(
        "==" to { a, b ->
            a == b
        },
        "!=" to { a, b ->
            a != b
        }
    ))

    class Operation<T>(
        val map: Map<String, (T, T) -> Boolean>
    )

    private val types: Map<Class<*>, Operation<*>> = mapOf(
        java.lang.Boolean::class.java to boolean,
        java.lang.Number::class.java to number,
        java.lang.String::class.java to string
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> find(clazz: Class<T>): Operation<T>? {
        return types[clazz] as? Operation<T>
    }
}