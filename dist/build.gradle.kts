plugins {
    alias(libs.plugins.standardConvention)
}

val api = rootProject.project("api:standard-api")

dependencies {
    compileOnly(api)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.bundles.library)

    testImplementation(api)
    testImplementation(libs.bundles.library)

    compileOnly("me.lucko:jar-relocator:1.7")
}

tasks.jar {
    dependsOn(api.tasks.jar)
}