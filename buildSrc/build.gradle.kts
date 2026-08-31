plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.build.kotlin.jvm)
    implementation(libs.build.minotaur)
    implementation(libs.build.resourcefactory)
    implementation(libs.build.shadow)
    implementation(libs.build.dokka)
    implementation(libs.build.publish)
    implementation(libs.build.paperweight)
}