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

configurations.create("merge")

dependencies {
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:2024.07.28")
    })
    //Other mod dependency
    modCompileOnly("net.kyori:adventure-api:4.17.0")
    modCompileOnly(include("eu.pb4:polymer-resource-pack:0.10.2+1.21.3")!!)
    modCompileOnly(include("eu.pb4:placeholder-api:2.5.0+1.21.2")!!)
    modCompileOnly("net.luckperms:api:5.4")

    //Kyori
    modCompileOnly("net.fabricmc:fabric-loader:${project.properties["loader_version"]}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"]}")
    modImplementation("net.kyori:adventure-platform-fabric:5.14.2") {
        exclude("net.kyori", "adventure-api")
        exclude("net.fabricmc")
    }

    //Shadow
    "merge"("net.kyori:adventure-text-serializer-legacy:4.17.0")
    "merge"("net.kyori:adventure-api:4.17.0")
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
        mainEntrypoint("net.kyori.adventure.platform.fabric.impl.AdventureCommon"),
        mainEntrypoint("net.kyori.adventure.platform.fabric.impl.compat.permissions.PermissionsApiIntegration"),
        entrypoint("adventure-internal:sidedproxy/server", "net.kyori.adventure.platform.fabric.impl.server.DedicatedServerProxy"),
        serverEntrypoint("kr.toxicity.hud.bootstrap.fabric.FabricBootstrapImpl"),
    )
    depends = mapOf(
        "fabricloader" to listOf(">=${project.properties["loader_version"]}"),
        "minecraft" to listOf("~${project.properties["minecraft_version"]}"),
        "java" to listOf(">=21"),
        "fabric-api" to listOf("*")
    )
    mixins = listOf(
        mixin("adventure-platform-fabric.accessor.mixins.json"),
        mixin("adventure-platform-fabric.mixins.json")
    )
    suggests = mapOf(
        "placeholder-api" to listOf("*"),
        "luckperms" to listOf("*"),
        "polymer-resource-pack" to listOf("*")
    )
}

val addedJar by tasks.creating(Jar::class.java) {
    from(sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations.modImplementation.map {
        it.resolve()
    }.get().forEach {
        from(zipTree(it)) {
            exclude("META-INF/*.SF")
            exclude("META-INF/*.DSA")
            exclude("META-INF/*.RSA")
        }
    }
}

tasks {
    remapJar {
        inputFile = addedJar.archiveFile
        archiveClassifier = "remapped"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations.getByName("merge").forEach {
            from(zipTree(it)) {
                exclude("META-INF/**")
            }
        }
    }
    runServer {
        enabled = false
    }
}