package kr.toxicity.hud.resource

import kr.toxicity.command.BetterCommandSource
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.util.*

class GlobalResource(
    val info: ReloadInfo
) : BetterCommandSource by info.sender {
    private val assets = listOf("assets")

    private val hud = assets + NAME_SPACE_ENCODED
    private val minecraft = assets + "minecraft"

    val bossBar = minecraft + listOf("textures", "gui")
    val shaders = minecraft +  "shaders"
    val core = shaders + "core"

    val font = hud + "font"
    val textures = hud + "textures"

    init {
        val key = ConfigManagerImpl.key
        BOOTSTRAP.resource("splitter.png")?.buffered()?.use {
            val read = it.readAllBytes()
            PackGenerator.addTask(textures + "${ConfigManagerImpl.key.splitterKey.value()}.png") {
                read
            }
        }
        PackGenerator.addTask(font + "${ConfigManagerImpl.key.spaceKey.value()}.json") {
            val center = 0xD0000
            jsonObjectOf(
                "providers" to jsonArrayOf(
                    jsonObjectOf(
                        "type" to "bitmap",
                        "file" to "${key.splitterKey.asString()}.png",
                        "ascent" to -9999,
                        "height" to -2,
                        "chars" to jsonArrayOf(0xC0000.parseChar())
                    ),
                    jsonObjectOf(
                        "type" to "space",
                        "advances" to jsonObjectOf(*(-8192..8192).map { i ->
                            (center + i).parseChar() to i
                        }.toTypedArray())
                    )
                )
            ).toByteArray()
        }
    }
}