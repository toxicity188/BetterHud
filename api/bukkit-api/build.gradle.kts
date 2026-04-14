plugins {
    alias(libs.plugins.apiConvention)
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:${property("minecraft_version")}-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.3.10")
    api(project(":api:standard-api"))
}