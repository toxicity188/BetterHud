plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

dependencies {
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    implementation(project(":api"))
    implementation(project(":scheduler:standard"))
    implementation(project(":scheduler:folia"))
    implementation(project(":bedrock:geyser"))
    implementation(project(":bedrock:floodgate"))
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
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOCore-API:1.12.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.SkriptLang:Skript:2.8.3")
}