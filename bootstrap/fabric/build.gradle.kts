import net.fabricmc.loom.task.RunGameTask
import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
    id("xyz.jpenilla.resource-factory-fabric-convention") version("1.2.0")
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