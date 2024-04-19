package kr.toxicity.hud.manager

import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.HudHead
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.awt.Color
import java.awt.image.BufferedImage

object PlayerHeadManager: BetterHudManager {

    private val headMap = HashMap<String, HudHead>()

    override fun start() {

    }

    fun getHead(name: String) = synchronized(headMap) {
        headMap[name]
    }

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        val saveLocation = ArrayList(resource.textures).apply {
            add("head")
        }
        synchronized(headMap) {
            headMap.clear()
        }
        DATA_FOLDER.subFolder("heads").forEachAllYamlAsync({ file, s, configurationSection ->
            runCatching {
                val head = HudHead(s , configurationSection)
                val pixel = head.pixel
                val targetFile = ArrayList(saveLocation).apply {
                    add("pixel_$pixel.png")
                }
                PackGenerator.addTask(targetFile) {
                    BufferedImage(pixel, pixel, BufferedImage.TYPE_INT_ARGB).apply {
                        createGraphics().run {
                            color = Color.WHITE
                            fillRect(0, 0, pixel, pixel)
                            dispose()
                        }
                    }.toByteArray()
                }
                headMap.putSync(head.name) {
                    head
                }
            }.onFailure { e ->
                warn("Unable to load this head: $s in ${file.name}")
                warn("Reason: ${e.message}")
            }
        }, callback)
    }

    override fun end() {
    }
}