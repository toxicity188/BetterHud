plugins {
    alias(libs.plugins.apiConvention)
}

dependencies {
    implementation(project(":api:standard-api"))
    annotationProcessor("com.velocitypowered:velocity-api:${property("velocity_version")}-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:${property("velocity_version")}-SNAPSHOT")
}