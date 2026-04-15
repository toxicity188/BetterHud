plugins {
    id("standard-conventions")
    id("com.modrinth.minotaur")
}

val shade = configurations.create("shade")

configurations.implementation {
    extendsFrom(shade)
}

sourceSets {
    main {
        resources {
            srcDirs(rootDir.resolve("common-resources"))
        }
    }
}

dependencies {
    compileOnly(libs.bundles.library)
    compileOnly(shade("me.lucko:jar-relocator:1.7") {
        exclude("org.ow2.asm")
    })

    shade(project(":dist")) { isTransitive = false }
    shade(project(":api:standard-api")) { isTransitive = false }
    shade(libs.betterCommand) { isTransitive = false }
}

modrinth {
    val log = System.getenv("COMMIT_MESSAGE")
    if (log != null) {
        versionType = "alpha"
        changelog = log
    } else {
        versionType = "release"
        changelog = rootProject.file("changelog/${project.version}.md").readText()
    }
    token = System.getenv("MODRINTH_API_TOKEN")
    projectId = "betterhud2"
    versionNumber = project.version as String
    additionalFiles = listOf(
        rootProject.layout.buildDirectory.file("libs/${rootProject.name}-${rootProject.version}-javadoc.jar")
    )
}