plugins {
    alias(libs.plugins.standardConvention)
}

dependencies {
    compileOnly(project(":api:standard-api"))
    compileOnly("org.spigotmc:spigot-api:${property("minecraft_version")}-R0.1-SNAPSHOT")
}