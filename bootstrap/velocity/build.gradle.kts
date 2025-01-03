plugins {
    id("xyz.jpenilla.resource-factory-velocity-convention") version "1.2.0"
}

velocityPluginJson {
    main = "kr.toxicity.hud.bootstrap.velocity.VelocityBootstrapImpl"
    version = rootProject.version.toString()
    id = "betterhud"
    name = "BetterHud"
    authors = listOf("toxicity")
    description = "Make a hud in minecraft!"
    url = "https://hangar.papermc.io/toxicity188/BetterHud"
}

