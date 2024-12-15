package kr.toxicity.hud.bootstrap.fabric.module

import kr.toxicity.hud.bootstrap.fabric.module.fabric.FabricEntityModule
import kr.toxicity.hud.bootstrap.fabric.module.fabric.FabricStandardModule

val MODULE_FABRIC = mapOf(
    "standard" to {
        FabricStandardModule()
    },
    "entity" to {
        FabricEntityModule()
    }
)