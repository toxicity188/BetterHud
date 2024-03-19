package kr.toxicity.hud.resource

import kr.toxicity.hud.manager.ConfigManager
import kr.toxicity.hud.util.*

class GlobalResource {
    private val accept = DATA_FOLDER.parentFile.subFolder(ConfigManager.buildFolderLocation).let {
        if (ConfigManager.separateResourcePackNameSpace) it else it.subFolder("assets").clearFolder()
    }

    private val hud = accept.subFolder(NAME_SPACE).clearFolder().apply {
        PLUGIN.loadAssets(NAME_SPACE, this)
    }
    private val minecraft = accept.subFolder("minecraft").clearFolder().apply {
        PLUGIN.loadAssets("minecraft", this)
    }

    val bossBar = minecraft
        .subFolder("textures")
        .subFolder("gui")

    val core = minecraft
        .subFolder("shaders")
        .subFolder("core")

    val font = hud.subFolder("font")
    val textures = hud.subFolder("textures")
}