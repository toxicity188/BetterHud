plugins {
    id("io.papermc.paperweight.userdev") version("1.5.6")
}

dependencies {
    paperweight.paperDevBundle("1.18.2-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}