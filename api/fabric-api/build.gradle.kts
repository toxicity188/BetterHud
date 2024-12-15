dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:${properties["parchment"]}")
    })
    //Kyori
    modCompileOnly("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    modCompileOnly("net.kyori:adventure-platform-mod-shared-fabric-repack:${properties["kyori_mod_implementation"]}")
    modCompileOnly("net.kyori:adventure-platform-fabric:${properties["kyori_mod_implementation"]}")
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("win", "0")
    }
}

tasks.runServer {
    enabled = false
}