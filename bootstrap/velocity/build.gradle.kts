plugins {
    alias(libs.plugins.bootstrapConvention)
    alias(libs.plugins.resourceFactoryVelocity)
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

dependencies {
    compileOnly(shade(project(":api:standard-api"))!!)
    compileOnly(shade(project(":api:velocity-api"))!!)
    compileOnly(libs.bstatsVelocity)
    shade(libs.bstatsVelocity)
    compileOnly("io.netty:netty-all:4.1.115.Final")
    annotationProcessor("com.velocitypowered:velocity-api:${property("velocity_version")}-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:${property("velocity_version")}-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:${property("velocity_version")}-SNAPSHOT")
}

tasks.jar {
    archiveBaseName = "${rootProject.name}-velocity"
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
    setManifest()
    doLast {
        relocateAll()
    }
}

beforeEvaluate {
    modrinth {
        uploadFile.set(tasks.jar)
        versionName = "BetterHud ${project.version} for velocity"
        gameVersions = SUPPORTED_MINECRAFT_VERSION
        loaders = listOf("bukkit", "spigot", "paper", "folia", "purpur")
    }
}