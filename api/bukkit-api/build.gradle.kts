plugins {
    alias(libs.plugins.apiConvention)
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:${property("minecraft_version")}-R0.1-SNAPSHOT")
    implementation(project(":api:standard-api"))
}