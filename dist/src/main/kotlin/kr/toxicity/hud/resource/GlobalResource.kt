package kr.toxicity.hud.resource

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.manager.ConfigManager
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.util.*

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
        val key = ConfigManager.key
        PLUGIN.getResource("splitter.png")?.buffered()?.use {
            val read = it.readAllBytes()
            PackGenerator.addTask(ArrayList(textures).apply {
                add(KeyResource.splitter)
                add("${KeyResource.splitter}.png")
            }) {
                read
            }
        }
        PLUGIN.getResource("spaces.ttf")?.buffered()?.use {
            val read = it.readAllBytes()
            PackGenerator.addTask(ArrayList(font).apply {
                add(KeyResource.spaces)
                add("${KeyResource.spaces}.ttf")
            }) {
                read
            }
        }
        PackGenerator.addTask(ArrayList(font).apply {
            add(KeyResource.space)
            add("${KeyResource.space}.json")
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
            add(KeyResource.legacySpace)
            add("${KeyResource.legacySpace}.json")
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