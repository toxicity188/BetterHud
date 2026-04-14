plugins {
    alias(libs.plugins.paperweightConvention)
    alias(libs.plugins.paperweight)
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
}
