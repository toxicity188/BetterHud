plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "BetterHud"

include(
    "api",
    "dist",
    "nms:v1_20_R2",
    "nms:v1_20_R3",
    "nms:v1_20_R4",

    "scheduler:standard",
    "scheduler:folia",

    "bedrock:geyser",
    "bedrock:floodgate"
)