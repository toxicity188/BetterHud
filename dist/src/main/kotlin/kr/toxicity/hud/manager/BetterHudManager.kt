package kr.toxicity.hud.manager

import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.resource.GlobalResource

interface BetterHudManager {
    fun start()
    fun preReload() {}
    fun reload(info: ReloadInfo, resource: GlobalResource)
    fun postReload() {}
    fun end()
}