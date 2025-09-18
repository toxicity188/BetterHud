import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    id("standard-conventions")
    id("com.vanniktech.maven.publish")
    signing
}

val publishName = "${rootProject.name}-${project.name}"

dependencies {
    compileOnly(libs.bundles.adventure)

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

java {
    withSourcesJar()
    withJavadocJar()
}

signing {
    useGpgCmd()
}

val archiveName = "betterhud-${project.name}"

tasks.jar {
    archiveBaseName = archiveName
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.toxicity188", publishName, project.version as String)
    configure(JavaLibrary(
        javadocJar = JavadocJar.None(),
        sourcesJar = true,
    ))
    pom {
        name = publishName
        description = "A multi-platform server-side implementation of HUD in Minecraft, supporting Bukkit(with Folia), Velocity, and Fabric."
        inceptionYear = "2024"
        url = "https://github.com/toxicity188/BetterHud/"
        licenses {
            license {
                name = "MIT License"
                url = "https://mit-license.org/"
            }
        }
        developers {
            developer {
                id = "toxicity188"
                name = "toxicity188"
                url = "https://github.com/toxicity188/"
            }
        }
        scm {
            url = "https://github.com/toxicity188/BetterHud/"
            connection = "scm:git:git://github.com/toxicity188/BetterHud.git"
            developerConnection = "scm:git:ssh://git@github.com/toxicity188/BetterHud.git"
        }
    }
}