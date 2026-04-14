plugins {
    alias(libs.plugins.apiConvention)
}

dependencies {
    api(project(":api:standard-api"))
    annotationProcessor("com.velocitypowered:velocity-api:${property("velocity_version")}-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:${property("velocity_version")}-SNAPSHOT")
}