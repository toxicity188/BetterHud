import xyz.jpenilla.resourcefactory.bukkit.Permission

plugins {
    alias(libs.plugins.conventions.bootstrap)
    alias(libs.plugins.conventions.bukkit)
    alias(libs.plugins.resourcefactory.bukkit)
    alias(libs.plugins.shadow)
}

dependencies {
    shade(project(":api:bukkit-api")) { isTransitive = false }

    shade(project(":bedrock:geyser")) { isTransitive = false }
    shade(project(":bedrock:floodgate")) { isTransitive = false }

    shade(project(":scheduler:standard")) { isTransitive = false }
    shade(project(":scheduler:paper")) { isTransitive = false }

    shade(project(":nms:v1_21_R2", configuration = "reobf")) { isTransitive = false }
    shade(project(":nms:v1_21_R3", configuration = "reobf")) { isTransitive = false }
    shade(project(":nms:v1_21_R4", configuration = "reobf")) { isTransitive = false }
    shade(project(":nms:v1_21_R5", configuration = "reobf")) { isTransitive = false }
    shade(project(":nms:v1_21_R6", configuration = "reobf")) { isTransitive = false }
    shade(project(":nms:v1_21_R7", configuration = "reobf")) { isTransitive = false }
    shade(project(":nms:v26_R1")) { isTransitive = false }

    shade(libs.bstats.bukkit)
    shade(libs.kotlinStdlib)

    compileOnly(libs.adventure.platform.bukkit)
    compileOnly(shade(rootProject.fileTree("shaded"))!!)

    compileOnly("io.lumine:Mythic-Dist:5.11.2")
    compileOnly("io.lumine:MythicLib-dist:1.7.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOCore-API:1.13.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.2") {
        exclude("com.google.guava")
        exclude("com.google.code.gson")
        exclude("it.unimi.dsi")
    }
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.16") {
        exclude("com.google.guava")
        exclude("com.google.code.gson")
        exclude("it.unimi.dsi")
    }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.github.SkriptLang:Skript:2.14.1")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.12.0")
    compileOnly("com.alessiodp.parties:parties-bukkit:3.2.16")
    compileOnly("net.momirealms:craft-engine-core:0.0.67")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.67")
    compileOnly("com.nexomc:nexo:1.21.0")
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

val shade = configurations.getByName("shade")
val targetAttribute = manifestAttribute + mapOf("paperweight-mappings-namespace" to "spigot")
val groupString = group.toString()

tasks {
    jar {
        finalizedBy(shadowJar)
    }
    shadowJar {
        configurations = listOf(shade)
        archiveBaseName = "${rootProject.name}-bukkit"
        archiveClassifier = ""
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
        manifest {
            attributes(targetAttribute)
        }
        dependencies {
            exclude(dependency("org.jetbrains:annotations:26.1.0"))
        }
        fun prefix(pattern: String) {
            relocate(pattern, "$groupString.shaded.$pattern")
        }
        prefix("kotlin")
        prefix("kr.toxicity.command.impl")
        prefix("org.bstats")
        prefix("me.lucko.jarrelocator")
    }
}


modrinth {
    uploadFile.set(tasks.shadowJar)
    versionName = "BetterHud ${project.version} for Bukkit"
    gameVersions = SUPPORTED_MINECRAFT_VERSION
    loaders = listOf("bukkit", "spigot", "paper", "folia", "purpur")
}