package kr.toxicity.hud.animation

enum class AnimationType {
    LOOP {
        override fun <T> choose(list: List<T>, index: Long): T {
            return list[(index % list.size).toInt()]
        }
        override fun choose(array: IntArray, index: Long): Int = array[(index % array.size).toInt()]
    },
    PLAY_ONCE {
        override fun <T> choose(list: List<T>, index: Long): T {
            return list[index.coerceIn(0L, list.lastIndex.toLong()).toInt()]
        }
        override fun choose(array: IntArray, index: Long): Int = array[index.coerceIn(0L, array.lastIndex.toLong()).toInt()]
    }
    ;
    abstract fun <T> choose(list: List<T>, index: Long): T
    abstract fun choose(array: IntArray, index: Long): Int

    companion object {
        fun parse(name: String?): AnimationType = if (name == null) LOOP else runCatching {
            valueOf(name.uppercase().replace('-', '_'))
        }.getOrElse { throw RuntimeException("This animation type doesn't exist: $name") }
    }
}
