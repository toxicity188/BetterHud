dependencies {
    compileOnly("io.lumine:Mythic-Dist:5.7.1")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOCore-API:1.12.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.0-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.SkriptLang:Skript:2.9.1")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.4.2")
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