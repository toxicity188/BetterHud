package kr.toxicity.hud.resource

import kr.toxicity.hud.util.*

class GlobalResource {
    private val assets = listOf("assets")

    private val hud = ArrayList(assets).apply {
        add(NAME_SPACE)
    }

    private val minecraft = ArrayList(assets).apply {
        add("minecraft")
    }

    val bossBar = ArrayList(minecraft).apply {
        add("textures")
        add("gui")
    }

    val core = ArrayList(minecraft).apply {
        add("shaders")
        add("core")
    }

    val font = ArrayList(hud).apply {
        add("font")
    }
    val textures = ArrayList(hud).apply {
        add("textures")
    }
}