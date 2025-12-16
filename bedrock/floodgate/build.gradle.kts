plugins {
    alias(libs.plugins.standardConvention)
}

dependencies {
    compileOnly(project(":api:bukkit-api"))
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
}