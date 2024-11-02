<div align="center">  

![-0001-export](https://github.com/toxicity188/BetterHud/assets/114675706/ccbf4bd3-9133-44ee-b277-985eae4349ae)

Welcome to BetterHud!

[SpigotMC](https://www.spigotmc.org/resources/115559/) | [Hangar](https://hangar.papermc.io/toxicity188/BetterHud) | [Modrinth](https://modrinth.com/plugin/betterhud2) | [Github](https://github.com/toxicity188/BetterHud)

[![GitHub Release](https://img.shields.io/github/v/release/toxicity188/BetterHud?display_name=release&style=for-the-badge&logo=kotlin)](https://github.com/toxicity188/BetterHud/releases/latest)
[![Discord](https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/rePyFESDbk)
[![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/toxicity188/BetterHud?style=for-the-badge&logo=github)](https://github.com/toxicity188/BetterHud/issues)
[![Static Badge](https://img.shields.io/badge/WIKI-blue?style=for-the-badge)](https://github.com/toxicity188/BetterHud/wiki)
![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/toxicity188/BetterHud?style=for-the-badge)


</div>

### Multi-platform server-side HUD implementation of Minecraft.
This project implements a server-side HUD.

- Supports auto-generating resource pack.
- Supports display image(include png sequence), display, head.
- Supports animation.

### Platform
- Bukkit(including Folia) 1.17-1.21.3
- Velocity 3.3-3.4
- Fabric server 1.21.3

### Library
- [kotlin stdlib](https://github.com/JetBrains/kotlin): Implements better functional programming.
- [adventure](https://github.com/KyoriPowered/adventure): Implements multi-platform component.
- [bstats](https://bstats.org/getting-started/include-metrics): Implements metrics.
- [exp4j](https://github.com/fasseg/exp4j): Implements equation.
- [negative space font](https://github.com/AmberWat/NegativeSpaceFont): Implements space font in minecraft legacy version.
- [snakeyaml](https://github.com/snakeyaml): Implements yaml parser.
- [gson](https://github.com/google/gson): Implements json parser/writer.
- [better command](https://github.com/toxicity188/BetterCommand): Implements multi-platform supporting command.


## Dependency
- Bukkit: No
- Velocity: No
- Fabric server: [Fabric API](https://modrinth.com/mod/fabric-api)

### Build
Requires Java 17, 21 Eclipse Adoptium.

- Build all available jar: ./gradlew build  
- Build Bukkit plugin: ./gradlew pluginJar
- Build Velocity plugin: ./gradlew velocityJar
- Build Fabric server side mod: ./gradlew fabricJar  
- Build source code jar: ./gradlew sourceJar  
- Build dokka-based docs jar: ./gradlew dokkaJar

### API
[![](https://jitpack.io/v/toxicity188/BetterHud.svg)](https://jitpack.io/#toxicity188/BetterHud)
``` kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("net.kyori:adventure-api:VERSION") //Adventure api.
    compileOnly("com.github.toxicity188:BetterHud:VERSION") //BetterHud.
    compileOnly("com.github.toxicity188:BetterCommand:VERSION") //BetterCommand library.
}
```

### Use BetterHud with Skript
[Go to download Skript](https://github.com/SkriptLang/Skript/releases)
```
command /pointadd:
    trigger:
        #compass marker add
        point add location at 0, 0, 0 in world "world" named "test1" to player
        point add location at 10, 0, 0 in world "world" named "test2" with icon "other" to player

command /pointremove:
    trigger:
        #compass marker remove
        point remove "test1" to player
        point remove "test2" to player

command /popup:
    trigger:
        #show popup with custom event
        set {_o::rand} to random integer between 1 to 100
        show popup "test_popup" to player with variable of {_o::*}
```