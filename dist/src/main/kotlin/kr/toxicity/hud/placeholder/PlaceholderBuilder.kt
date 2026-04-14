package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.update.UpdateEvent

interface PlaceholderBuilder<T : Any> {
    val clazz: Class<out T>
    infix fun build(reason: UpdateEvent): Placeholder<T>

    val isNumber get() = Number::class.javaObjectType.isAssignableFrom(clazz)
    val isBoolean get() = Boolean::class.javaObjectType.isAssignableFrom(clazz)
    val isString get() = String::class.javaObjectType.isAssignableFrom(clazz)

    fun assertNumber(message: String) = assertNumber { message }
    fun assertBoolean(message: String) = assertBoolean { message }
    fun assertString(message: String) = assertString { message }

    fun assertNumber(message: PlaceholderBuilder<T>.() -> String): PlaceholderBuilder<T> {
        if (!isNumber) throw RuntimeException(message())
        return this
    }
    fun assertBoolean(message: PlaceholderBuilder<T>.() -> String): PlaceholderBuilder<T> {
        if (!isBoolean) throw RuntimeException(message())
        return this
    }
    fun assertString(message: PlaceholderBuilder<T>.() -> String): PlaceholderBuilder<T> {
        if (!isString) throw RuntimeException(message())
        return this
    }

    class Delegate<T : Any>(
        override val clazz: Class<out T>,
        private val builder: (UpdateEvent) -> Placeholder<T>
    ) : PlaceholderBuilder<T> {
        override fun build(reason: UpdateEvent): Placeholder<T> = builder(reason)
    }
}