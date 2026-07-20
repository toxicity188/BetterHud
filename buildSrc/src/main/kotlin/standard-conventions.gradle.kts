import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

group = "kr.toxicity.hud"
version = property("version").toString() + (BUILD_NUMBER?.let { "-SNAPSHOT-$it" } ?: "")

// JDK 25 toolchain is required to compile against Minecraft 26.1 (nms:v26_R1),
// but the common/shared modules emit Java 21 bytecode so the final plugin stays
// loadable on Java 21 servers (1.21.x). The Java 25 code lives only in nms:v26_R1,
// which is loaded reflectively just on 26.1 servers.
val toolchainJavaVersion = 25
val targetBytecodeVersion = 21

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    api(libs.betterCommand)
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = targetBytecodeVersion
    }
    compileKotlin {
        compilerOptions.jvmTarget = JvmTarget.fromTarget(targetBytecodeVersion.toString())
    }
}

java {
    disableAutoTargetJvm()
    toolchain.vendor = JvmVendorSpec.ADOPTIUM
    toolchain.languageVersion = JavaLanguageVersion.of(toolchainJavaVersion)
}

kotlin {
    jvmToolchain(toolchainJavaVersion)
}

dokka {
    moduleName = project.name
    dokkaSourceSets.configureEach {
        displayName = project.name
    }
}