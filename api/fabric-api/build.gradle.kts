plugins {
    alias(libs.plugins.conventions.api)
    id("net.neoforged.moddev")
}

dependencies {
    api(project(":api:standard-api"))
}

neoForge {
    enable {
        neoFormVersion = libs.versions.neoform.get()
    }
}