import xyz.jpenilla.resourcefactory.bukkit.Permission

plugins {
    alias(libs.plugins.bootstrapConvention)
    alias(libs.plugins.resourceFactoryBukkit)
}

repositories {
    maven("https://maven.enginehub.org/repo/") //WorldEdit, WorldGuard
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/") //MMOItems, MMOCore, MythicLib
    maven("https://repo.skriptlang.org/releases") //Skript
    maven("https://repo.alessiodp.com/releases/") //Parties
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") //PlaceholderAPI
    maven("https://mvn.lumine.io/repository/maven/") //MythicMobs
    maven("https://repo.momirealms.net/releases/") //CraftEngine
    maven("https://repo.nexomc.com/releases/") //Nexo
    maven("https://jitpack.io") //Vault
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:${property("minecraft_version")}-R0.1-SNAPSHOT")
    compileOnly(libs.bundles.adventure)

    compileOnly(shade(project(":api:standard-api"))!!)
    compileOnly(shade(project(":api:bukkit-api"))!!)
    testImplementation(project(":api:bukkit-api"))
    rootProject.project("bedrock").subprojects.forEach {
        compileOnly(shade(it)!!)
    }
    rootProject.project("scheduler").subprojects.forEach {
        compileOnly(shade(it)!!)
    }
    rootProject.project("nms").subprojects.forEach {
        compileOnly(shade(project(":nms:${it.name}", configuration = "reobf"))!!)
        //compileOnly(shade(it)!!)
    }
    compileOnly(libs.bstatsBukkit)
    shade(libs.bstatsBukkit)
    compileOnly(libs.adventurePlatformBukkit)
    compileOnly(shade(rootProject.fileTree("shaded"))!!)

    compileOnly("io.lumine:Mythic-Dist:5.10.1")
    compileOnly("io.lumine:MythicLib-dist:1.7.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOCore-API:1.13.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.7")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.17") {
        exclude("com.google.guava")
        exclude("com.google.code.gson")
        exclude("it.unimi.dsi")
    }
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.14") {
        exclude("com.google.guava")
        exclude("com.google.code.gson")
        exclude("it.unimi.dsi")
    }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.github.SkriptLang:Skript:2.13.1")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.9.0")
    compileOnly("com.alessiodp.parties:parties-bukkit:3.2.16")
    compileOnly("net.momirealms:craft-engine-core:0.0.64")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.64")
    compileOnly("com.nexomc:nexo:1.15.0")
}

bukkitPluginYaml {
    main = "$group.bootstrap.bukkit.BukkitBootstrapImpl"
    version = project.version.toString()
    name = rootProject.name
    apiVersion = "1.19"
    author = "toxicity"
    description = "A multiplatform server-side implementation of HUD in Minecraft."
    foliaSupported = true
    website = "https://www.spigotmc.org/resources/115559"
    softDepend = listOf(
        "MythicLib",
        "MythicMobs",
        "MMOCore",
        "MMOItems",
        "PlaceholderAPI",
        "WorldGuard",
        "Vault",
        "floodgate",
        "Geyser-Spigot",
        "Skript",
        "SkBee",
        "skript-placeholders",
        "skript-reflect",
        "SkinsRestorer",
        "Parties",
        "GPS",
        "BetterModel",
        "CraftEngine",
        "Nexo"
    )
    permissions {
        create("betterhud.help") {
            description = "Accesses to help command."
            default = Permission.Default.OP
        }
        create("betterhud.reload") {
            description = "Accesses to reload command."
            default = Permission.Default.OP
        }
        create("betterhud.parse") {
            description = "Accesses to parse command."
            default = Permission.Default.OP
        }
        create("betterhud.hud") {
            description = "Accesses to hud command."
            default = Permission.Default.OP
            children = mapOf(
                "betterhud.hud.add" to true,
                "betterhud.hud.remove" to true
            )
        }
        create("betterhud.compass") {
            description = "Accesses to compass command."
            default = Permission.Default.OP
            children = mapOf(
                "betterhud.compass.add" to true,
                "betterhud.compass.remove" to true
            )
        }
        create("betterhud.turn") {
            description = "Accesses to turn command."
            default = Permission.Default.OP
            children = mapOf(
                "betterhud.turn.on" to true,
                "betterhud.turn.off" to true,
                "betterhud.turn.on.admin" to true,
                "betterhud.turn.off.admin" to true
            )
        }
        create("betterhud.pointer") {
            description = "Accesses to pointer command."
            default = Permission.Default.OP
            children = mapOf(
                "betterhud.pointer.set" to true,
                "betterhud.pointer.clear" to true,
                "betterhud.pointer.remove" to true
            )
        }
        create("betterhud.popup") {
            description = "Accesses to popup command."
            default = Permission.Default.OP
            children = mapOf(
                "betterhud.popup.add" to true,
                "betterhud.popup.remove" to true,
                "betterhud.popup.show" to true,
                "betterhud.popup.hide" to true
            )
        }
    }
}

tasks.jar {
    archiveBaseName = "${rootProject.name}-bukkit"
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot"
    }
}


modrinth {
    uploadFile.set(tasks.jar)
    versionName = "BetterHud ${project.version} for Bukkit"
    gameVersions = SUPPORTED_MINECRAFT_VERSION
    loaders = listOf("bukkit", "spigot", "paper", "folia", "purpur")
}