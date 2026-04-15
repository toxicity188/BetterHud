plugins {
    id("standard-conventions")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":api:bukkit-api"))
}