package kr.toxicity.hud.bootstrap.bukkit.util

import org.bukkit.Bukkit

data class MinecraftVersion(
    val first: Int,
    val second: Int,
    val third: Int
): Comparable<MinecraftVersion> {
    companion object {
        val current = MinecraftVersion(Bukkit.getBukkitVersion()
            .substringBefore('-'))

        val version1_21_1 = MinecraftVersion(1, 21, 1)
        val version1_21 = MinecraftVersion(1, 21, 0)
        val version1_20_6 = MinecraftVersion(1, 20, 6)
        val version1_20_5 = MinecraftVersion(1, 20, 5)
        val version1_20_4 = MinecraftVersion(1, 20, 4)
        val version1_20_3 = MinecraftVersion(1, 20, 3)
        val version1_20_2 = MinecraftVersion(1, 20, 2)
        val version1_20_1 = MinecraftVersion(1, 20, 1)
        val version1_20 = MinecraftVersion(1, 20, 0)
        val version1_19_4 = MinecraftVersion(1, 19, 4)
        val version1_19_3 = MinecraftVersion(1, 19, 3)
        val version1_19_2 = MinecraftVersion(1, 19, 2)
        val version1_19_1 = MinecraftVersion(1, 19, 1)
        val version1_19 = MinecraftVersion(1, 19, 0)
        val version1_18_2 = MinecraftVersion(1, 18, 2)
        val version1_18_1 = MinecraftVersion(1, 18, 1)
        val version1_18 = MinecraftVersion(1, 18, 0)
        val version1_17_1 = MinecraftVersion(1, 17, 1)
        val version1_17 = MinecraftVersion(1, 17, 0)

        private val comparator = Comparator.comparing { v: MinecraftVersion ->
            v.first
        }.thenComparing { v: MinecraftVersion ->
            v.second
        }.thenComparing { v: MinecraftVersion ->
            v.third
        }
    }

    constructor(string: String): this(string.split('.'))
    constructor(string: List<String>): this(
        if (string.isNotEmpty()) string[0].toInt() else 0,
        if (string.size > 1) string[1].toInt() else 0,
        if (string.size > 2) string[2].toInt() else 0
    )
    override fun compareTo(other: MinecraftVersion): Int {
        return comparator.compare(this, other)
    }

    override fun toString(): String {
        return if (third == 0) "$first.$second" else "$first.$second.$third"
    }
}