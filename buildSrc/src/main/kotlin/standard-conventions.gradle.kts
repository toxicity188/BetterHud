plugins {
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

group = "kr.toxicity.hud"
version = "1.11.3" + (buildNumber?.let { ".$it" } ?: "")

val targetJavaVersion = 21

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") //Spigot
    maven("https://repo.papermc.io/repository/maven-public/") //Paper
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.opencollab.dev/main/")
    maven("https://maven.fabricmc.net/") //Fabric
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(libs.betterCommand)
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
}

java {
    toolchain.vendor = JvmVendorSpec.ADOPTIUM
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}

kotlin {
    jvmToolchain(targetJavaVersion)
}

dokka {
    val list = mutableListOf(project.name)
    var parent: Project? = project.parent
    do {
        parent?.let {
            list.add(it.name)
        }
        parent = parent?.parent
    } while (parent != null)
    moduleName = list.reversed().joinToString("/")
    dokkaSourceSets.configureEach {
        displayName = project.name
    }
}