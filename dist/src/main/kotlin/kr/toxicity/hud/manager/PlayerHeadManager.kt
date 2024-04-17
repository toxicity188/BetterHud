package kr.toxicity.hud.manager

import kr.toxicity.hud.player.HudHead
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File

object PlayerHeadManager: BetterHudManager {

    private val headMap = HashMap<String, HudHead>()

    override fun start() {

    }

    fun getHead(name: String) = headMap[name]

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        val saveLocation = resource.textures.subFolder("head")
        headMap.clear()
        DATA_FOLDER.subFolder("heads").forEachAllYamlAsync({ _, file, s, configurationSection ->
            runCatching {
                val head = HudHead(s , configurationSection)
                val pixel = head.pixel
                val targetFile = File(saveLocation, "pixel_$pixel.png")
                if (!targetFile.exists()) BufferedImage(pixel, pixel ,BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        color = Color.WHITE
                        fillRect(0, 0, pixel, pixel)
                        dispose()
                    }
                }.save(targetFile)
                headMap[head.name] = head
            }.onFailure { e ->
                warn("Unable to load this head: $s in ${file.name}")
                warn("Reason: ${e.message}")
            }
        }, callback)
    }

    override fun end() {
    }
}