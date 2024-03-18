package kr.toxicity.hud.bedrock

import kr.toxicity.hud.api.bedrock.BedrockAdapter
import org.geysermc.floodgate.api.FloodgateApi
import java.util.*

class FloodgateAdapter: BedrockAdapter {
    override fun isBedrockPlayer(uuid: UUID): Boolean {
        return FloodgateApi.getInstance().isFloodgatePlayer(uuid)
    }
}