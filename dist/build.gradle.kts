plugins {
    alias(libs.plugins.conventions.standard)
}

dependencies {
    implementation(project(":api"))
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.bundles.library)

    testImplementation(libs.bundles.library)

    compileOnly("me.lucko:jar-relocator:1.7")
}