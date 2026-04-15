plugins {
    id("standard-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:${property("minecraft_version")}-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.3.10")
    api(libs.bundles.adventure)
}
