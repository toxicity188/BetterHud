import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
    id("xyz.jpenilla.resource-factory-fabric-convention") version("1.2.0")
}

repositories {
    // There might be other repos there too, just add it at the end
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:2024.07.28")
    })
    modCompileOnly("net.fabricmc:fabric-loader:${project.properties["loader_version"]}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"]}")
    modImplementation("net.kyori:adventure-platform-fabric:5.14.1") {
        exclude("net.fabricmc")
    }
    modImplementation("net.kyori:adventure-text-serializer-legacy:4.17.0")

    //Other mod dependency
    modCompileOnly(include("eu.pb4:placeholder-api:2.4.1+1.21")!!)
    modCompileOnly("net.luckperms:api:5.4")
}

loom {
    mixin {
        add(sourceSets.main.get(), "adventure-platform-fabric-refmap.json")
    }
}

fabricModJson {
    id = "betterhud"
    name = rootProject.name
    version = project.version.toString()
    description = "Make a hud in minecraft!"
    authors.set(listOf(person("toxicity") {
        contact.sources = "https://github.com/toxicity188/BetterHud"
    }))
    license = listOf("MIT")
    environment = Environment.SERVER
    entrypoints = listOf(
        serverEntrypoint("kr.toxicity.hud.bootstrap.fabric.FabricBootstrapImpl"),
        entrypoint("adventure-internal:sidedproxy/server", "net.kyori.adventure.platform.fabric.impl.server.DedicatedServerProxy")
    )
    depends = mapOf(
        "fabricloader" to listOf(">=${project.properties["loader_version"]}"),
        "minecraft" to listOf("~${project.properties["minecraft_version"]}"),
        "java" to listOf(">=21"),
        "fabric-api" to listOf("*")
    )
    mixins = listOf(
        mixin("betterhud.mixins.json") {
            environment = Environment.SERVER
        }
    )
    suggests = mapOf(
        "placeholder-api" to listOf("*"),
        "luckperms" to listOf("*")
    )
}

tasks {
    shadowJar {
        exclude("META-INF")
        configurations = listOf(project.configurations.modImplementation.get())
    }
    remapJar {
        inputFile = shadowJar.map {
            it.archiveFile
        }.get()
    }
    runServer {
        enabled = false
    }
}