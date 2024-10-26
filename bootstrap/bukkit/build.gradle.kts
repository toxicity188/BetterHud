repositories {
    maven("https://maven.enginehub.org/repo/") //WorldEdit, WorldGuard
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/") //MMOItems, MMOCore, MythicLib
    maven("https://repo.skriptlang.org/releases") //Skript
    maven("https://repo.alessiodp.com/releases/") //Parties
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") //PlaceholderAPI
    maven("https://mvn.lumine.io/repository/maven/") //MythicMobs
}

dependencies {
    compileOnly("io.lumine:Mythic-Dist:5.7.2")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOCore-API:1.12.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.0-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.github.SkriptLang:Skript:2.9.3")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.4.3")
    compileOnly("com.alessiodp.parties:parties-bukkit:3.2.15")
}

tasks.processResources {
    filteringCharset = Charsets.UTF_8.name()
    val props = mapOf(
        "version" to project.version
    )
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}