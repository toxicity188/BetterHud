package kr.toxicity.hud.module

import kr.toxicity.hud.module.bukkit.BukkitEntityModule
import kr.toxicity.hud.module.bukkit.BukkitItemModule

val MODULE_BUKKIT = mapOf(
    "entity" to {
        BukkitEntityModule()
    },
    "item" to {
        BukkitItemModule()
    }
)