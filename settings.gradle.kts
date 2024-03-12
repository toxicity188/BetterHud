plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "BetterHud"

include(
    "api",
    "dist",
    "nms:v1_17_R1",
    "nms:v1_18_R1",
    "nms:v1_18_R2",
    "nms:v1_19_R1",
    "nms:v1_19_R2",
    "nms:v1_19_R3",
    "nms:v1_20_R1",
    "nms:v1_20_R2",
    "nms:v1_20_R3",

    "scheduler:standard",
    "scheduler:folia"
)