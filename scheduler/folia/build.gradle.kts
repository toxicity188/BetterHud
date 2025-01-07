plugins {
    alias(libs.plugins.standardConvention)
}

dependencies {
    compileOnly(project(":api:standard-api"))
    compileOnly("io.papermc.paper:paper-api:${properties["minecraft_version"]}-R0.1-SNAPSHOT")
}