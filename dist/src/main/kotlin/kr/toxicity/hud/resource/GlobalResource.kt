package kr.toxicity.hud.resource

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.NAME_SPACE_ENCODED
import kr.toxicity.hud.util.parseChar
import kr.toxicity.hud.util.toByteArray

class GlobalResource {
    private val assets = listOf("assets")

    private val hud = ArrayList(assets).apply {
        add(NAME_SPACE_ENCODED)
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

    init {
        val key = ConfigManagerImpl.key
        BOOTSTRAP.resource("splitter.png")?.buffered()?.use {
            val read = it.readAllBytes()
            PackGenerator.addTask(ArrayList(textures).apply {
                add("${ConfigManagerImpl.key.splitterKey.value()}.png")
            }) {
                read
            }
        }
        BOOTSTRAP.resource("spaces.ttf")?.buffered()?.use {
            val read = it.readAllBytes()
            PackGenerator.addTask(ArrayList(font).apply {
                add("${ConfigManagerImpl.key.spacesTtfKey.value()}.ttf")
            }) {
                read
            }
        }
        PackGenerator.addTask(ArrayList(font).apply {
            add("${ConfigManagerImpl.key.spaceKey.value()}.json")
        }) {
            JsonObject().apply {
                add("providers", JsonArray().apply {
                    add(JsonObject().apply {
                        addProperty("type", "bitmap")
                        addProperty("file", "${key.splitterKey.asString()}.png")
                        addProperty("ascent", -9999)
                        addProperty("height", - 2)
                        add("chars", JsonArray().apply {
                            add((0xC0000).parseChar())
                        })
                    })
                    val center = 0xD0000
                    add(JsonObject().apply {
                        addProperty("type", "space")
                        add("advances", JsonObject().apply {
                            for (i in -8192..8192) {
                                addProperty((center + i).parseChar(), i)
                            }
                        })
                    })
                })
            }.toByteArray()
        }
        PackGenerator.addTask(ArrayList(font).apply {
            add("${ConfigManagerImpl.key.legacySpaceKey.value()}.json")
        }) {
            JsonObject().apply {
                add("providers", JsonArray().apply {
                    add(JsonObject().apply {
                        addProperty("type", "ttf")
                        addProperty("file", "${key.spacesTtfKey.asString()}.ttf")
                        addProperty("size", 2.5)
                        addProperty("oversample", 1.0)
                        add("shift", JsonArray().apply {
                            add(0.0)
                            add(0.0)
                        })
                        add("skip", JsonArray())
                    })
                })
            }.toByteArray()
        }
    }
}