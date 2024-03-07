package kr.toxicity.hud.manager

import kr.toxicity.hud.resource.GlobalResource

interface BetterHudManager {
    fun start()
    fun reload(resource: GlobalResource)
    fun end()
}