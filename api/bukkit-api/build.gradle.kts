plugins {
    alias(libs.plugins.conventions.api)
    alias(libs.plugins.conventions.bukkit)
}

dependencies {
    api(project(":api:standard-api"))
}