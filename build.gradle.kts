plugins {
    `java-library`
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

val adventure = "4.16.0"
val platform = "4.3.2"

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    group = "kr.toxicity.hud"
    version = "beta-4"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://mvn.lumine.io/repository/maven/")
    }

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test")

        compileOnly("net.kyori:adventure-api:$adventure")
        compileOnly("net.kyori:adventure-text-minimessage:$adventure")
        compileOnly("net.kyori:adventure-platform-bukkit:$platform")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
        compileJava {
            options.encoding = Charsets.UTF_8.name()
        }
        processResources {
            filteringCharset = Charsets.UTF_8.name()
            val props = mapOf(
                "version" to project.version,
                "adventure" to adventure,
                "platform" to platform
            )
            inputs.properties(props)
            filesMatching("plugin.yml") {
                expand(props)
            }
        }
    }
}

dependencies {
    implementation(project(":dist", configuration = "shadow"))
}

tasks {
    jar {
        finalizedBy(shadowJar)
    }
    shadowJar {
        archiveClassifier = ""
        fun prefix(pattern: String) {
            relocate(pattern, "${project.group}.shaded.$pattern")
        }
        prefix("kotlin")
        prefix("net.objecthunter.exp4j")
        prefix("org.bstats")
    }
}

val targetJavaVersion = 17

java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}

kotlin {
    jvmToolchain(targetJavaVersion)
}