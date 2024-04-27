dependencies {
    paperweight.paperDevBundle("1.20.5-R0.1-SNAPSHOT")
}

val targetJavaVersion = 21

java {
    toolchain.vendor = JvmVendorSpec.ADOPTIUM
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}

kotlin {
    jvmToolchain(targetJavaVersion)
}