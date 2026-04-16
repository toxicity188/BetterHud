pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("net.fabricmc.fabric-loom-repositories") version "1.16-SNAPSHOT"
    id("net.neoforged.moddev.repositories") version "2.0.141"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()

        // Standard
        maven("https://maven.fabricmc.net/") //Fabric
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") //Spigot
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.opencollab.dev/main/")
        maven("https://jitpack.io")

        // Bukkit
        maven("https://maven.enginehub.org/repo/") //WorldEdit, WorldGuard
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/") //MMOItems, MMOCore, MythicLib
        maven("https://repo.skriptlang.org/releases") //Skript
        maven("https://repo.alessiodp.com/releases/") //Parties
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") //PlaceholderAPI
        maven("https://mvn.lumine.io/repository/maven/") //MythicMobs
        maven("https://repo.momirealms.net/releases/") //CraftEngine
        maven("https://repo.nexomc.com/releases/") //Nexo

        // Mod
        maven("https://maven.nucleoid.xyz/") { //placeholderapi, polymer
            name = "Nucleoid"
        }
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") { //Kyori snapshot
            name = "sonatype-oss-snapshots1"
            mavenContent { snapshotsOnly() }
        }
    }
}

rootProject.name = "BetterHud"

gradle.startParameter.isParallelProjectExecutionEnabled = true

include(
    "api:bukkit-api",
    "api:velocity-api",
    "api:mod-api",

    "dist",
    "nms:v1_21_R2",
    "nms:v1_21_R3",
    "nms:v1_21_R4",
    "nms:v1_21_R5",
    "nms:v1_21_R6",
    "nms:v1_21_R7",
    "nms:v26_R1",

    "scheduler:standard",
    "scheduler:paper",

    "bedrock:geyser",
    "bedrock:floodgate",

    "bootstrap:bukkit",
    "bootstrap:velocity",
    "bootstrap:fabric"
)