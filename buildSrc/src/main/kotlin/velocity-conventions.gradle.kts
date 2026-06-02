plugins {
    id("standard-conventions")
}

dependencies {
    compileOnly("io.netty:netty-all:4.2.15.Final")
    annotationProcessor("com.velocitypowered:velocity-api:${property("velocity_version")}-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:${property("velocity_version")}-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:${property("velocity_version")}-SNAPSHOT")
}
