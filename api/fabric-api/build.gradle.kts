plugins {
    alias(libs.plugins.apiConvention)
    alias(libs.plugins.resourceFactoryFabric)
    alias(libs.plugins.loom)
}

repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") { //Kyori snapshot
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:${property("parchment")}")
    })
    //Kyori
    modCompileOnly("net.fabricmc:fabric-loader:${property("loader_version")}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modCompileOnly("net.kyori:adventure-platform-mod-shared-fabric-repack:${property("kyori_mod_implementation")}")
    modImplementation(include("net.kyori:adventure-platform-fabric:${property("kyori_mod_implementation")}")!!)
    implementation(include(project(":api:standard-api"))!!)
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("win", "0")
    }
}

fabricModJson {
    id = "betterhud-fabric-api"
    name = "${rootProject.name}-Fabric-API"
    version = project.version.toString()
    description = "A fabric api of BetterHud"
    authors.set(listOf(person("toxicity") {
        contact.sources = "https://github.com/toxicity188/BetterHud"
    }))
    license = listOf("MIT")
    entrypoints = listOf(
        mainEntrypoint("$group.api.fabric.Main")
    )
    depends = mapOf(
        "fabricloader" to listOf("*"),
        "minecraft" to listOf("~${property("supported_version")}"),
        "java" to listOf(">=21"),
        "fabric-api" to listOf("*"),
        "adventure-platform-fabric" to listOf("*")
    )
    mixins = listOf(
        mixin("betterhud.mixins.json")
    )
}

tasks {
    remapJar {
        archiveBaseName = "betterhud-${project.name}"
        archiveClassifier = ""
    }
    runServer {
        enabled = false
    }
}