import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
    alias(libs.plugins.bootstrapConvention)
    alias(libs.plugins.resourceFactoryFabric)
    alias(libs.plugins.loom)
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

val minecraft = property("minecraft_version")
val supportedVersion = property("supported_version")

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:${properties["parchment"]}")
    })
    //Other mod dependency
    modCompileOnly("eu.pb4:polymer-resource-pack:0.11.3+1.21.4")
    modCompileOnly("eu.pb4:polymer-autohost:0.11.3+1.21.4")
    modCompileOnly("eu.pb4:placeholder-api:2.5.1+1.21.3")
    modCompileOnly("net.luckperms:api:5.4")

    //Kyori
    modCompileOnly("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    modCompileOnly("net.kyori:adventure-platform-mod-shared-fabric-repack:${properties["kyori_mod_implementation"]}")
    modImplementation("net.kyori:adventure-platform-fabric:${properties["kyori_mod_implementation"]}")
    compileOnly(project(":api:standard-api"))
    implementation(include(project(":api:fabric-api"))!!)
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
    description = "A multi-platform server-side implementation of HUD in Minecraft."
    authors.set(listOf(person("toxicity") {
        contact.sources = "https://github.com/toxicity188/BetterHud"
    }))
    license = listOf("MIT")
    environment = Environment.SERVER
    entrypoints = listOf(
        serverEntrypoint("kr.toxicity.hud.bootstrap.fabric.FabricBootstrapImpl")
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
        archiveClassifier = "dev"
        doLast {
            relocateAll()
        }
    }
    remapJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveBaseName = "${rootProject.name}-fabric+$minecraft"
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
        archiveClassifier = ""
        setManifest()
    }
    runServer {
        enabled = false
    }
}

beforeEvaluate {
    modrinth {
        uploadFile.set(tasks.remapJar)
        versionName = "BetterHud ${project.version} for fabric"
        gameVersions = SUPPORTED_MINECRAFT_VERSION.subList(
            SUPPORTED_MINECRAFT_VERSION.indexOf(supportedVersion),
            SUPPORTED_MINECRAFT_VERSION.size
        )
        loaders = listOf("fabric", "quilt")
        required.project("fabric-api")
        optional.project("polymer", "placeholder-api", "luckperms")
    }
}