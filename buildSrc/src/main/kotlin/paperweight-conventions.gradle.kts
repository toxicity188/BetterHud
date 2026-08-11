plugins {
    id("standard-conventions")
    id("io.papermc.paperweight.userdev")
}

// NMS modules build on a JDK 21 toolchain by default. The shared standard-conventions
// uses JDK 25 (needed to compile against Minecraft 26.1), but paperweight's remap step
// runs codebook/ASM 9.6, which cannot read JDK 25 platform classes (class major 69) and
// fails for the older mojang-mapped versions (1.21.4/1.21.5/1.21.6). Those Minecraft
// versions are Java 21 anyway, so remapping and compiling them on JDK 21 is correct.
// Only nms:v26_R1 overrides this back to JDK 25 for Minecraft 26.1.
java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":api:bukkit-api"))
}