import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
    alias(libs.plugins.bootstrapConvention)
    alias(libs.plugins.resourceFactoryFabric)
    id("net.fabricmc.fabric-loom")
}

val minecraft = property("minecraft_version")
val supportedVersion = property("supported_version")

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    //Other mod dependency
    compileOnly("eu.pb4:polymer-resource-pack:0.16.2+26.1.1")
    compileOnly("eu.pb4:polymer-autohost:0.16.2+26.1.1")
    compileOnly("eu.pb4:placeholder-api:3.0.0+26.1")
    compileOnly("net.luckperms:api:5.5")
    compileOnly("org.checkerframework:checker-qual:3.55.1")

    //Kyori
    compileOnly("net.fabricmc:fabric-loader:${property("loader_version")}")
    compileOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    implementation("net.kyori:adventure-platform-fabric:${property("kyori_mod_implementation")}")
    compileOnly(project(":api:standard-api"))
    implementation(include(project(":api:fabric-api"))!!)
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
        serverEntrypoint("$group.bootstrap.fabric.FabricBootstrapImpl")
    )
    depends = mapOf(
        "fabricloader" to listOf("*"),
        "minecraft" to listOf("~$supportedVersion"),
        "java" to listOf(">=21"),
        "fabric-api" to listOf("*"),
        "betterhud-fabric-api" to listOf("*")
    )
    suggests = mapOf(
        "luckperms" to listOf("*"),
        "polymer-resource-pack" to listOf("*"),
        "polymer-autohost" to listOf("*"),
        "placeholder-api" to listOf("*")
    )
}

tasks {
    jar {
        archiveBaseName = "${rootProject.name}-fabric+$minecraft"
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
        archiveClassifier = ""
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
    required.project("fabric-api")
    optional.project("polymer", "placeholder-api", "luckperms")
}