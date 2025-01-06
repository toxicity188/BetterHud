plugins {
    alias(libs.plugins.standardConvention)
}

dependencies {
    compileOnly(project(":api:standard-api"))
    compileOnly("org.spigotmc:spigot-api:${properties["minecraft_version"]}-R0.1-SNAPSHOT")
}