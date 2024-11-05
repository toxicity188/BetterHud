pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "BetterHud"

gradle.startParameter.isParallelProjectExecutionEnabled = true

include(
    "api:standard-api",
    "api:bukkit-api",
    "api:velocity-api",
    "api:fabric-api",

    "dist",
    //"nms:v1_17_R1",
    //"nms:v1_18_R1",
    "nms:v1_18_R2",
    "nms:v1_19_R1",
    "nms:v1_19_R2",
    "nms:v1_19_R3",
    "nms:v1_20_R1",
    "nms:v1_20_R2",
    "nms:v1_20_R3",
    "nms:v1_20_R4",
    "nms:v1_21_R1",
    "nms:v1_21_R2",

    "scheduler:standard",
    "scheduler:folia",

    "oraxen:1.0",
    "oraxen:2.0",

    "bedrock:geyser",
    "bedrock:floodgate",

    "bootstrap:bukkit",
    "bootstrap:velocity",
    "bootstrap:fabric"
)