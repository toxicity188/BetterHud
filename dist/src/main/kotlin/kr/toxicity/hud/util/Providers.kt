package kr.toxicity.hud.util

fun <S> runByTick(tick: Long, frameSupplier: () -> Long, supplier: (Long) -> S): Runner<S> {
    val t = tickProvide(tick) { _: Unit, l ->
        supplier(l)
    }(Unit)
    return Runner {
        t(frameSupplier())
    }
}
fun <S> runByTick(tick: Long, frameSupplier: () -> Long, supplier: Runner<S>): Runner<S> {
    val t = tickProvide(tick) { _: Unit, _ ->
        supplier()
    }(Unit)
    return Runner {
        t(frameSupplier())
    }
}

fun <S, T> tickProvide(source: T) = TickProvider(1) { _: S, _: Long ->
    source
}
fun <S, T> tickProvide(tick: Long, source: (S, Long) -> T) = TickProvider(tick, source)

class TickProvider<S, T>(
    private val tick: Long,
    private val source: (S, Long) -> T
) : (S) -> (Long) -> T {
    override fun invoke(s: S): (Long) -> T {
        return if (tick == 1L) { l: Long ->
            source(s, l)
        } else {
            var previous: T? = null
            { l: Long ->
                (if (tick > 0 && l % tick == 0L) source(s, l) else previous ?: source(s, l)).apply {
                    previous = this
                }
            }
        }
    }
}
