package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.util.JavaBoolean
import kr.toxicity.hud.util.JavaNumber
import kr.toxicity.hud.util.JavaString

interface PlaceholderBuilder<T : Any> {
    val clazz: Class<out T>
    infix fun build(reason: UpdateEvent): Placeholder<T>

    val isNumber get() = JavaNumber::class.java.isAssignableFrom(clazz)
    val isBoolean get() = JavaBoolean::class.java.isAssignableFrom(clazz)
    val isString get() = JavaString::class.java.isAssignableFrom(clazz)

    fun assertNumber(message: String) = assertNumber { message }
    fun assertBoolean(message: String) = assertBoolean { message }
    fun assertString(message: String) = assertString { message }

    fun assertNumber(message: PlaceholderBuilder<T>.() -> String): PlaceholderBuilder<T> {
        assert(isNumber) {
            message()
        }
        return this
    }
    fun assertBoolean(message: PlaceholderBuilder<T>.() -> String): PlaceholderBuilder<T> {
        assert(isBoolean) {
            message()
        }
        return this
    }
    fun assertString(message: PlaceholderBuilder<T>.() -> String): PlaceholderBuilder<T> {
        assert(isString) {
            message()
        }
        return this
    }

    class Delegate<T : Any>(
        override val clazz: Class<out T>,
        private val builder: (UpdateEvent) -> Placeholder<T>
    ) : PlaceholderBuilder<T> {
        override fun build(reason: UpdateEvent): Placeholder<T> = builder(reason)
    }
}