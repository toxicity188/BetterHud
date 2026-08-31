import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.conventions.paperweight)
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.release = 21
    }
    compileKotlin {
        compilerOptions.jvmTarget = JvmTarget.JVM_21
    }
}