package kr.toxicity.hud.util

data class MinecraftVersion(
    val first: Int,
    val second: Int,
    val third: Int
): Comparable<MinecraftVersion> {
    companion object {
        val version1_20_6 = MinecraftVersion(1, 20, 6)
        val version1_20_5 = MinecraftVersion(1, 20, 5)
    }

    constructor(string: String): this(string.split('.'))
    constructor(string: List<String>): this(
        string[0].toInt(),
        string[1].toInt(),
        string[2].toInt()
    )
    override fun compareTo(other: MinecraftVersion): Int {
        return Comparator.comparing { v: MinecraftVersion ->
            v.first
        }.thenComparing { v: MinecraftVersion ->
            v.second
        }.thenComparing { v: MinecraftVersion ->
            v.third
        }.compare(this, other)
    }
}