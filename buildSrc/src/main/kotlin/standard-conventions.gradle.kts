plugins {
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

group = "kr.toxicity.hud"
version = property("version").toString() + (BUILD_NUMBER?.let { "-SNAPSHOT-$it" } ?: "")

val targetJavaVersion = 21

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") //Fabric
    maven("https://repo.papermc.io/repository/maven-public/") //Paper
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") //Spigot
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.opencollab.dev/main/")
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
    moduleName = project.name
    dokkaSourceSets.configureEach {
        displayName = project.name
    }
}