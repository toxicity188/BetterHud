pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "BetterHud"

gradle.startParameter.isParallelProjectExecutionEnabled = true

include(
    "api:standard-api",
    "api:bukkit-api",
    "api:velocity-api",
    "api:fabric-api",

    "dist",
    "nms:v1_19_R1",
    "nms:v1_19_R2",
    "nms:v1_19_R3",
    "nms:v1_20_R1",
    "nms:v1_20_R2",
    "nms:v1_20_R3",
    "nms:v1_20_R4",
    "nms:v1_21_R1",
    "nms:v1_21_R2",
    "nms:v1_21_R3",
    "nms:v1_21_R4",

    "scheduler:standard",
    "scheduler:paper",

    "bedrock:geyser",
    "bedrock:floodgate",

    "bootstrap:bukkit",
    "bootstrap:velocity",
    "bootstrap:fabric"
)