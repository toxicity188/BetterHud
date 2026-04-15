plugins {
    alias(libs.plugins.conventions.bootstrap)
    alias(libs.plugins.conventions.velocity)
    alias(libs.plugins.resourcefactory.velocity)
    alias(libs.plugins.shadow)
}

velocityPluginJson {
    main = "$group.bootstrap.velocity.VelocityBootstrapImpl"
    version = rootProject.version.toString()
    id = "betterhud"
    name = "BetterHud"
    authors = listOf("toxicity")
    description = "Make a hud in minecraft!"
    url = "https://hangar.papermc.io/toxicity188/BetterHud"
}

dependencies {
    shade(project(":api:velocity-api")) { isTransitive = false }
    shade(libs.bstats.velocity)
    shade(libs.kotlinStdlib)
}

val shade = configurations.getByName("shade")
val targetAttribute = manifestAttribute
val groupString = group.toString()

tasks {
    jar {
        finalizedBy(shadowJar)
    }
    shadowJar {
        configurations = listOf(shade)
        archiveBaseName = "${rootProject.name}-velocity"
        archiveClassifier = ""
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
        manifest {
            attributes(targetAttribute)
        }
        dependencies {
            exclude(dependency("org.jetbrains:annotations:26.1.0"))
        }
        fun prefix(pattern: String) {
            relocate(pattern, "$groupString.shaded.$pattern")
        }
        prefix("kotlin")
        prefix("kr.toxicity.command.impl")
        prefix("org.bstats")
        prefix("me.lucko.jarrelocator")
    }
}


modrinth {
    uploadFile.set(tasks.shadowJar)
    versionName = "BetterHud ${project.version} for Velocity"
    gameVersions = SUPPORTED_MINECRAFT_VERSION
    loaders = listOf("velocity")
}