package kr.toxicity.hud.resource

import kr.toxicity.hud.util.DATA_FOLDER
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.clearFolder
import kr.toxicity.hud.util.subFolder

class GlobalResource {
    private val build = DATA_FOLDER.subFolder("build").clearFolder().apply {
        PLUGIN.loadAssets("pack", this)
    }
    private val assets = build.subFolder("assets")

    private val mythichud = assets.subFolder("mythichud")

    val bossBar = assets
        .subFolder("minecraft")
        .subFolder("textures")
        .subFolder("gui")
        .subFolder("sprites")
        .subFolder("boss_bar")

    val font = mythichud.subFolder("font")
    val textures = mythichud.subFolder("textures")
}