plugins {
    alias(libs.plugins.conventions.paperweight)
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
}

// Minecraft 26.1's API is compiled for Java 25, so a JDK 25 toolchain is required to
// compile against it. The emitted bytecode, however, stays at Java 21 (inherited from
// standard-conventions): this module only *calls* the 26.1 server API, it does not need
// Java 22+ language features. Keeping every class at major 65 lets Paper's plugin
// remapper (ASM) load the single jar on both Java 21 (1.21.x) and Java 25 (26.1) servers.
java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}
