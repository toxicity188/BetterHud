package kr.toxicity.hud.animation

enum class AnimationType {
    LOOP {
        override fun <T> choose(list: List<T>, index: Long): T {
            return list[(index % list.size).toInt()]
        }
    },
    PLAY_ONCE {
        override fun <T> choose(list: List<T>, index: Long): T {
            return list[index.coerceAtLeast(0)
                .coerceAtMost(list.lastIndex.toLong())
                .toInt()]
        }
    }
    ;
    abstract fun <T> choose(list: List<T>, index: Long): T
}