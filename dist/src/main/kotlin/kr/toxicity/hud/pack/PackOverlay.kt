package kr.toxicity.hud.pack

import kr.toxicity.hud.util.PLUGIN

enum class PackOverlay(
    val overlayName: String,
    val minVersion: Int,
    val maxVersion: Int
) {
    LEGACY("betterhud_legacy", 9, 34),
    V1_21_2("betterhud_1_21_2", 35, 45),
    V1_21_4("betterhud_1_21_4", 46, 55),
    V1_21_6("betterhud_1_21_6", 56, 99)
    ;
    fun loadAssets() {
        PLUGIN.loadAssets(overlayName) { n, i ->
            val read = i.readAllBytes()
            PackGenerator.addTask(listOf(overlayName) + n.split('/')) {
                read
            }
        }
    }
}