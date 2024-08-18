plugins {
    id("xyz.jpenilla.resource-factory-velocity-convention") version("1.1.2")
}

velocityPluginJson {
    main = "kr.toxicity.hud.bootstrap.velocity.VelocityBootstrapImpl"
    version = rootProject.version.toString()
    id = "betterhud"
    name = "BetterHud"
    authors = listOf("toxicity")
    description = "Make a hud in minecraft!"
    url = "https://www.spigotmc.org/resources/115559"
}

