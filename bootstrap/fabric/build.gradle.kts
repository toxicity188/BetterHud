import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
    alias(libs.plugins.conventions.bootstrap)
    alias(libs.plugins.resourcefactory.fabric)
    id("net.fabricmc.fabric-loom")
}

val minecraft = property("minecraft_version")
val supportedVersion = property("supported_version")

configurations {
    include {
        extendsFrom(shade.get())
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    // Other mod dependency
    implementation("eu.pb4:polymer-resource-pack:0.16.2+26.1.1")
    implementation("eu.pb4:polymer-autohost:0.16.2+26.1.1")
    implementation("eu.pb4:placeholder-api:3.0.0+26.1")
    implementation("net.luckperms:api:5.5")
    implementation("org.checkerframework:checker-qual:4.0.0")

    // Fabric
    implementation(libs.bundles.fabric)

    // Include
    implementation(include("net.kyori:adventure-platform-fabric:${property("kyori_mod_implementation")}")!!)
    implementation(include(project(":api:mod-api"))!!)
}

fabricModJson {
    id = "betterhud"
    name = rootProject.name
    version = project.version.toString()
    description = "A multiplatform server-side implementation of HUD in Minecraft."
    authors.set(listOf(person("toxicity") {
        contact.sources = "https://github.com/toxicity188/BetterHud"
    }))
    license = listOf("MIT")
    environment = Environment.SERVER
    entrypoints = listOf(
        serverEntrypoint("$group.bootstrap.fabric.FabricBootstrapImpl") {
            adapter = "kotlin"
        }
    )
    mixins = listOf(
        mixin("betterhud.mixins.json")
    )
    depends = mapOf(
        "fabricloader" to listOf(">=${libs.versions.fabric.loader.get()}"),
        "fabric-language-kotlin" to listOf(">=${libs.versions.fabric.language.kotlin.get()}"),
        "fabric-api" to listOf("*"),
        "minecraft" to listOf("~$supportedVersion"),
        "java" to listOf(">=25")
    )
    suggests = mapOf(
        "luckperms" to listOf("*"),
        "polymer-resource-pack" to listOf("*"),
        "polymer-autohost" to listOf("*"),
        "placeholder-api" to listOf("*")
    )
}

val targetAttribute = manifestAttribute

tasks {
    jar {
        archiveBaseName = "${rootProject.name}-fabric+$minecraft"
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
        archiveClassifier = ""
        manifest {
            attributes(targetAttribute)
        }
    }
    runServer {
        enabled = false
    }
}


modrinth {
    uploadFile.set(tasks.jar)
    versionName = "BetterHud ${project.version} for Fabric"
    gameVersions = SUPPORTED_MINECRAFT_VERSION.subList(
        SUPPORTED_MINECRAFT_VERSION.indexOf(supportedVersion),
        SUPPORTED_MINECRAFT_VERSION.size
    )
    loaders = listOf("fabric", "quilt")
    required.version("fabric-api", libs.versions.fabric.api.get())
    required.version("fabric-language-kotlin", libs.versions.fabric.language.kotlin.get())
    optional.project("polymer", "placeholder-api", "luckperms")
}