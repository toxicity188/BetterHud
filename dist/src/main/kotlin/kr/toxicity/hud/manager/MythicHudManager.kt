package kr.toxicity.hud.manager

import kr.toxicity.hud.resource.GlobalResource

interface MythicHudManager {
    fun start()
    fun reload(resource: GlobalResource)
    fun end()
}