package kr.toxicity.hud.pack

import kr.toxicity.hud.util.PLUGIN

enum class PackOverlay(
    val overlayName: String,
    val shaderCoreName: String,
    val minVersion: Int,
    val maxVersion: Int
) {
    V1_21_2("betterhud_1_21_2", "rendertype_text",9, 45),
    V1_21_4("betterhud_1_21_4", "rendertype_text",46, 55),
    V1_21_6("betterhud_1_21_6", "rendertype_text",56, 83),
    V26_1("betterhud_26_1", "rendertype_text",84, 87),
    V26_2("betterhud_26_2", "text",88, 96),
    V26_3("betterhud_26_3", "text",97, 99)
    ;
    fun loadAssets() {
        PLUGIN.loadAssets(overlayName) { n, i ->
            val read = i.readAllBytes()
            PackGenerator.addTask(buildList {
                add(overlayName)
                addAll(n.split('/'))
            }) {
                read
            }
        }
    }
}