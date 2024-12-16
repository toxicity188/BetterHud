package kr.toxicity.hud.manager

import kr.toxicity.hud.resource.GlobalResource
import net.kyori.adventure.audience.Audience
import java.util.EnumMap

object EncodeManager : BetterHudManager {

    private val encodeMap = EnumMap<EncodeNamespace, MutableMap<String, Int>>(EncodeNamespace::class.java)

    override fun start() {
    }

    override fun reload(sender: Audience, resource: GlobalResource) {
    }

    override fun postReload() {
        encodeMap.clear()
    }

    override fun end() {
    }

    enum class EncodeNamespace {
        FONT,
        TEXTURES
    }

    fun generateKey(namespace: EncodeNamespace, name: String): String {
        val map = synchronized(encodeMap) {
            encodeMap.computeIfAbsent(namespace) {
                mutableMapOf()
            }
        }
        return synchronized(map) {
            map.computeIfAbsent(name) {
                map.size
            }.encode()
        }
    }

    private fun Int.encode(): String {
        var i = this
        val size = chars.size
        return buildString {
            while (i >= size) {
                append(chars.first())
                i -= size
            }
            append(chars[i])
        }
    }

    private val chars = listOf(
        'a', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'i', 'j', 'k', 'm', 'n', 'l', 'o', 'p',
        'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    )
}