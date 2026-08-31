plugins {
    alias(libs.plugins.conventions.standard)
}

dependencies {
    compileOnly(project(":api:bukkit-api"))
    compileOnly("org.geysermc.geyser:api:2.9.2-SNAPSHOT")
}