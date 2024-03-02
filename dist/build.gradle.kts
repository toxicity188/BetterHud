plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

dependencies {
    implementation("net.objecthunter:exp4j:0.4.8")

    implementation(project(":api"))
    implementation(project(":nms:v1_17_R1", configuration = "reobf"))
    implementation(project(":nms:v1_18_R1", configuration = "reobf"))
    implementation(project(":nms:v1_18_R2", configuration = "reobf"))
    implementation(project(":nms:v1_19_R1", configuration = "reobf"))
    implementation(project(":nms:v1_19_R2", configuration = "reobf"))
    implementation(project(":nms:v1_19_R3", configuration = "reobf"))
    implementation(project(":nms:v1_20_R1", configuration = "reobf"))
    implementation(project(":nms:v1_20_R2", configuration = "reobf"))
    implementation(project(":nms:v1_20_R3", configuration = "reobf"))

    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
}