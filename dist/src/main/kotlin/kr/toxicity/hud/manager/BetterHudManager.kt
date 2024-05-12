package kr.toxicity.hud.manager

import kr.toxicity.hud.resource.GlobalResource
import net.kyori.adventure.audience.Audience

interface BetterHudManager {
    fun start()
    fun preReload() {}
    fun reload(sender: Audience, resource: GlobalResource)
    fun postReload() {}
    fun end()
}