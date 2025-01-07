plugins {
    id("standard-conventions")
}

dependencies {
    compileOnly(project(":api:standard-api"))
    compileOnly(project(":api:bukkit-api"))
}