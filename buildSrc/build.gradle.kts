plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.kotlinJvm)
    implementation(libs.minotaur)
    implementation("me.lucko:jar-relocator:1.7") {
        exclude("org.ow2.asm")
    }
    implementation(libs.bundles.asm)
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.0.0")
    implementation("com.vanniktech.maven.publish:com.vanniktech.maven.publish.gradle.plugin:0.32.0")
}