plugins {
    alias(libs.plugins.apiConvention)
    alias(libs.plugins.resourceFactoryFabric)
    alias(libs.plugins.loom)
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:${properties["parchment"]}")
    })
    //Kyori
    modCompileOnly("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    modCompileOnly("net.kyori:adventure-platform-mod-shared-fabric-repack:${properties["kyori_mod_implementation"]}")
    modImplementation(include("net.kyori:adventure-platform-fabric:${properties["kyori_mod_implementation"]}")!!)
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
        mainEntrypoint("kr.toxicity.hud.api.fabric.Main")
    )
    depends = mapOf(
        "fabricloader" to listOf("*"),
        "minecraft" to listOf("~${properties["supported_version"]}"),
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