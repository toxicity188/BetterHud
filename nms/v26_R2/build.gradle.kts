plugins {
    alias(libs.plugins.conventions.paperweight)
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
}

// Same as nms:v26_R1: Minecraft 26.2's API is compiled for Java 25, so a JDK 25
// toolchain is required to compile against it, while the emitted bytecode stays at
// Java 21 (inherited from standard-conventions) so one jar loads on both Java 21
// (1.21.x) and Java 25 (26.x) servers.
java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}
