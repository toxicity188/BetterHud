import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
    id("xyz.jpenilla.resource-factory-fabric-convention") version "1.2.0"
}

repositories {
    //placeholderapi
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    //kyori snapshot
//    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
//        name = "sonatype-oss-snapshots1"
//        mavenContent { snapshotsOnly() }
//    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:2024.07.28")
    })
    //Other mod dependency
    modCompileOnly("eu.pb4:polymer-resource-pack:0.9.18+1.21.1")
    modCompileOnly("eu.pb4:placeholder-api:2.4.1+1.21")
    modCompileOnly("net.luckperms:api:5.4")

    //Kyori
    modCompileOnly("net.fabricmc:fabric-loader:${project.properties["loader_version"]}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"]}")
    modImplementation(include("net.kyori:adventure-platform-fabric:5.14.2")!!)
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("win", "0")
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
    )
    depends = mapOf(
        "fabricloader" to listOf("*"),
        "minecraft" to listOf("~${project.properties["minecraft_version"]}"),
        "java" to listOf(">=21"),
        "fabric-api" to listOf("*"),
        "adventure-platform-fabric" to listOf("*")
    )
    suggests = mapOf(
        "luckperms" to listOf("*"),
        "placeholder-api" to listOf("*"),
        "polymer-resource-pack" to listOf("*")
    )
}

tasks {
    remapJar {
        archiveClassifier = "remapped"
        from(configurations.modImplementation.get().filter {
            !it.name.startsWith("fabric")
        }) {
            into("META-INF/jars")
        }
    }
    runServer {
        enabled = false
    }
}