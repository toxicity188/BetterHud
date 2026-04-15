plugins {
    alias(libs.plugins.conventions.standard)
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("org.spigotmc:spigot-api:${property("minecraft_version")}-R0.1-SNAPSHOT")
}