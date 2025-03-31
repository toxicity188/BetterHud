plugins {
    alias(libs.plugins.standardConvention)
}

dependencies {
    compileOnly(project(":api:bukkit-api"))
    compileOnly("org.geysermc.geyser:api:2.6.2-SNAPSHOT")
}