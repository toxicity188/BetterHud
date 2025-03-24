package kr.toxicity.hud.manager

import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.resource.GlobalResource
import java.io.File

interface BetterHudManager {

    val managerName: String
    val supportExternalPacks: Boolean

    fun start()
    fun preReload() {}
    fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource)
    fun postReload() {}
    fun end()
}