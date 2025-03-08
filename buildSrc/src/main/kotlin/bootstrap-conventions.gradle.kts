import gradle.kotlin.dsl.accessors._71500ebfd02aecedbeba1ea751aee542.compileOnly

plugins {
    id("standard-conventions")
    id("com.modrinth.minotaur")
}

val shade = configurations.create("shade")
val dist = rootProject.project("dist")

dependencies {
    compileOnly(libs.bundles.library)
    compileOnly(shade("me.lucko:jar-relocator:1.7") {
        exclude("org.ow2.asm")
    })
    compileOnly(shade(dist)!!)
    testImplementation(project(":api:standard-api"))
    testImplementation(dist)
    shade(libs.kotlinStdlib)
}

val excludeDependencies = listOf(
    "annotations-13.0.jar"
)

val versionString = version.toString()
val versionGradle = gradle.gradleVersion
val groupString = group.toString()

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(shade.dependencies)
        from(shade
            .asSequence()
            .distinctBy {
                it.name
            }.filter {
                !excludeDependencies.contains(it.name)
            }.map { file ->
                zipTree(file)
            }.toList()
        ) {
            exclude("META-INF/MANIFEST.MF")
        }
        setManifest(versionString, versionGradle)
        doLast {
            relocateAll(groupString)
        }
    }
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
        rootProject.layout.buildDirectory.file("libs/${rootProject.name}-${rootProject.version}-sources.jar"),
        rootProject.layout.buildDirectory.file("libs/${rootProject.name}-${rootProject.version}-javadoc.jar")
    )
}