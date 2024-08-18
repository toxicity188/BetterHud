package kr.toxicity.hud.bootstrap.bukkit.module

import kr.toxicity.hud.bootstrap.bukkit.module.bukkit.BukkitEntityModule
import kr.toxicity.hud.bootstrap.bukkit.module.bukkit.BukkitItemModule
import kr.toxicity.hud.bootstrap.bukkit.module.bukkit.BukkitStandardModule

val MODULE_BUKKIT = mapOf(
    "standard" to {
        BukkitStandardModule()
    },
    "entity" to {
        BukkitEntityModule()
    },
    "item" to {
        BukkitItemModule()
    }
)