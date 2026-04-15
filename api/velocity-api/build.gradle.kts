plugins {
    alias(libs.plugins.conventions.api)
    alias(libs.plugins.conventions.velocity)
}

dependencies {
    api(project(":api"))
}