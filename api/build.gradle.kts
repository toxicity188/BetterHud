import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish") version "0.30.0"
    signing
}

subprojects {

    val publishName = "${rootProject.name}-${project.name}"

    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "signing")

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.projectlombok:lombok:1.18.36")

        testCompileOnly("org.projectlombok:lombok:1.18.36")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    signing {
        useGpgCmd()
    }

    tasks.jar {
        archiveBaseName = "betterhud-${project.name}"
    }

    mavenPublishing {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
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
}