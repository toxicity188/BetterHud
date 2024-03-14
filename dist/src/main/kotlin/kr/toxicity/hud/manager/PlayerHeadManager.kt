package kr.toxicity.hud.manager

import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.DATA_FOLDER
import kr.toxicity.hud.util.subFolder

object PlayerHeadManager: BetterHudManager {



    override fun start() {

    }

    override fun reload(resource: GlobalResource) {
        val saveLocation = resource.textures.subFolder("head")
        DATA_FOLDER.subFolder("heads")

    }

    override fun end() {
    }
}