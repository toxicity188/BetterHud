package kr.toxicity.hud.resource

import kr.toxicity.hud.util.*

class GlobalResource {
    private val build = DATA_FOLDER.subFolder("build").clearFolder().apply {
        PLUGIN.loadAssets("pack", this)
    }
    private val assets = build.subFolder("assets")

    private val hud = assets.subFolder(NAME_SPACE)

    val bossBar = assets
        .subFolder("minecraft")
        .subFolder("textures")
        .subFolder("gui")

    val core = assets
        .subFolder("minecraft")
        .subFolder("shaders")
        .subFolder("core")

    val font = hud.subFolder("font")
    val textures = hud.subFolder("textures")
}