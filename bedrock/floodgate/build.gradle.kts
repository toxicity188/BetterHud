plugins {
    alias(libs.plugins.conventions.standard)
}

dependencies {
    compileOnly(project(":api:bukkit-api"))
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
}