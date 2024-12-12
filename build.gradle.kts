import io.papermc.hangarpublishplugin.model.Platforms
import io.papermc.paperweight.tasks.RemapJar
import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("me.lucko:jar-relocator:1.7")
    }
}

plugins {
    `java-library`
    kotlin("jvm") version "2.1.0"
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.6" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("org.jetbrains.dokka") version "2.0.0-Beta"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("fabric-loom") version "1.9-SNAPSHOT" apply false
    id("com.modrinth.minotaur") version "2.+"
    id("com.github.ben-manes.versions") version "0.51.0"
}

val minecraft = project.properties["minecraft_version"]!!.toString()
val adventure = "4.17.0"
val platform = "4.3.4"
val targetJavaVersion = 21
val velocity = "3.4.0"
val bStats = "3.1.0"
val betterCommand = "1.4"

val supportedMinecraftVersions = listOf(
    //1.17
    //"1.17",
    //"1.17.1",
    //1.18
    //"1.18",
    //"1.18.1",
    "1.18.2",
    //1.19
    "1.19",
    "1.19.1",
    "1.19.2",
    "1.19.3",
    "1.19.4",
    //1.20
    "1.20",
    "1.20.1",
    "1.20.2",
    "1.20.3",
    "1.20.4",
    "1.20.5",
    "1.20.6",
    //1.21
    "1.21",
    "1.21.1",
    "1.21.2",
    "1.21.3",
    "1.21.4"
)
val supportedVelocityVersions = listOf(
    "3.3",
    "3.4"
)

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.dokka")

    group = "kr.toxicity.hud"
    version = "1.10.2" + (System.getenv("BUILD_NUMBER")?.let { ".DEV-$it" } ?: "")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") //Spigot
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
        maven("https://repo.opencollab.dev/main/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://maven.fabricmc.net/") //Fabric
    }

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        implementation("io.github.toxicity188:BetterCommand:$betterCommand")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
        compileJava {
            options.compilerArgs.addAll(listOf("-source", "17", "-target", "17"))
            options.encoding = Charsets.UTF_8.name()
        }
        compileKotlin {
            compilerOptions {
                freeCompilerArgs.addAll(listOf("-jvm-target", "17", "-java-parameters"))
            }
        }
    }
    java {
        toolchain.vendor = JvmVendorSpec.ADOPTIUM
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

dokka {
    moduleName = "BetterHud docs"
    dokkaSourceSets.configureEach {
        displayName = project.name
    }
}

subprojects {
    dokka {
        val list = mutableListOf(project.name)
        var parent: Project? = project.parent
        do {
            parent?.let {
                list.add(it.name)
            }
            parent = parent?.parent
        } while (parent != null)
        moduleName = list.reversed().joinToString("/")
        dokkaSourceSets.configureEach {
            displayName = project.name
        }
    }
}

val legacyNmsVersion = listOf(
    //"v1_17_R1",
    //"v1_18_R1",
    "v1_18_R2",
    "v1_19_R1",
    "v1_19_R2",
    "v1_19_R3",
    "v1_20_R1",
    "v1_20_R2",
    "v1_20_R3",
).map {
    project("nms:$it")
}.onEach {
    it.legacy()
}

val currentNmsVersion = listOf(
    "v1_20_R4",
    "v1_21_R1",
    "v1_21_R2",
    "v1_21_R3",
).map {
    project("nms:$it")
}

val allNmsVersion = legacyNmsVersion + currentNmsVersion

fun Project.dependency(any: Any) = also {
    if (any is Collection<*>) {
        any.forEach { element ->
            val get = element ?: return@forEach
            it.dependencies {
                compileOnly(get)
                testImplementation(get)
            }
        }
    } else {
        it.dependencies {
            compileOnly(any)
            testImplementation(any)
        }
    }
}
fun Project.bukkit() = dependency("org.spigotmc:spigot-api:$minecraft-R0.1-SNAPSHOT")
    .dependency("org.bstats:bstats-bukkit:$bStats")
    .dependency(rootProject.fileTree("shaded"))
fun Project.velocity() = also {
    it.dependency("com.velocitypowered:velocity-api:$velocity-SNAPSHOT")
        .dependency("io.netty:netty-all:5.0.0.Alpha2")
        .dependency("org.bstats:bstats-velocity:$bStats")
    it.dependencies.compileOnly("com.velocitypowered:velocity-proxy:$velocity-SNAPSHOT")
    it.dependencies.annotationProcessor("com.velocitypowered:velocity-api:$velocity-SNAPSHOT")
}
fun Project.folia() = dependency("io.papermc.paper:paper-api:$minecraft-R0.1-SNAPSHOT")
fun Project.adventure() = dependency("net.kyori:adventure-api:$adventure")
    .dependency("net.kyori:adventure-text-minimessage:$adventure")
    .dependency("net.kyori:adventure-text-serializer-legacy:$adventure")
    .dependency("net.kyori:adventure-text-serializer-gson:$adventure")
fun Project.library() = also {
    it.dependencies {
        compileOnly("org.yaml:snakeyaml:2.3")
        compileOnly("com.google.code.gson:gson:2.11.0")
        compileOnly("net.objecthunter:exp4j:0.4.8")
        implementation("me.lucko:jar-relocator:1.7") {
            exclude("org.ow2.asm")
        }
        implementation(rootProject.fileTree("shaded"))
    }
}
fun Project.bukkitAudience() = dependency("net.kyori:adventure-platform-bukkit:$platform")

fun Project.legacy() = also {
    it.java {
        toolchain.languageVersion = JavaLanguageVersion.of(17)
    }
}
fun Project.modrinthPublish(depend: Jar, additionalJar: List<Jar>, loadersList: List<String>, versionList: List<String>, requiredDependency: List<String>, softDependency: List<String>) {
    apply(plugin = "com.modrinth.minotaur")
    modrinth {
        val log = System.getenv("COMMIT_MESSAGE")
        if (log != null) {
            versionType = "alpha"
            changelog = log
        } else {
            versionType = "release"
            changelog = rootProject.file("changelog/${project.version}.md").readText()
        }
        token = System.getenv("MODRINTH_API_TOKEN")
        projectId = "betterhud2"
        versionName = "BetterHud ${project.version} for ${depend.archiveClassifier.get()}"
        versionNumber = project.version as String
        uploadFile.set(depend.archiveFile)
        additionalFiles = additionalJar.map {
            it.archiveFile
        }
        gameVersions = versionList
        loaders = loadersList
        requiredDependency.forEach {
            required.project(it)
        }
        softDependency.forEach {
            optional.project(it)
        }
    }
}

val apiShare = project("api:standard-api").adventure()
val apiBukkit = project("api:bukkit-api").adventure().bukkit().dependency(apiShare)
val apiVelocity = project("api:velocity-api").velocity().dependency(apiShare)
val apiFabric = project("api:fabric-api").adventure().dependency(apiShare)

val api = listOf(
    apiShare,
    apiBukkit,
    apiVelocity,
    apiFabric
).onEach {
    project -> project.legacy()
}

fun Project.api() = dependency(api)

val dist = project("dist").adventure().library().api()
val scheduler = project("scheduler")
val bedrock = project("bedrock")

allNmsVersion.forEach {
    it.dependency(apiShare)
        .dependency(apiBukkit)
}

scheduler.project("standard").adventure().bukkit().api()
scheduler.project("folia").folia().api()

dist.dependency(allNmsVersion)

val bukkitBootstrap = project("bootstrap:bukkit")
    .adventure()
    .bukkit()
    .api()
    .dependency(dist)
    .bukkitAudience()
    .dependency(scheduler.subprojects)
    .dependency(bedrock.subprojects)
    .dependency(allNmsVersion)

val velocityBootstrap = project("bootstrap:velocity").velocity().api().dependency(dist)
val fabricBootstrap = project("bootstrap:fabric").api().dependency(dist).adventure().also {
    it.apply(plugin = "fabric-loom")
}

val bootstrap = listOf(
    bukkitBootstrap,
    velocityBootstrap,
    fabricBootstrap
)

allNmsVersion.forEach {
    it.apply(plugin = "io.papermc.paperweight.userdev")
}

dependencies {
    api.forEach {
        implementation(it)
    }
    implementation(dist)
    implementation("org.bstats:bstats-bukkit:$bStats")
    implementation("org.bstats:bstats-velocity:$bStats")
}

val sourcesJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.classes)
    fun getProjectSource(project: Project): Array<File> {
        return if (project.subprojects.isEmpty()) project.sourceSets.main.get().allSource.srcDirs.toTypedArray() else ArrayList<File>().apply {
            project.subprojects.forEach {
                addAll(getProjectSource(it))
            }
        }.toTypedArray()
    }
    archiveClassifier = "sources"
    from(*getProjectSource(project))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
val javadocJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.dokkaGenerate)
    archiveClassifier = "javadoc"
    from(layout.buildDirectory.dir("dokka/html").orNull?.asFile)
}
val fabricJar by tasks.creating(Jar::class.java) {
    archiveClassifier = "fabric+$minecraft"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(zipTree(fabricBootstrap.tasks.named("remapJar").map {
        (it as org.gradle.jvm.tasks.Jar).archiveFile
    }))
    from(zipTree(tasks.shadowJar.map {
        it.archiveFile
    }))
    doLast {
        relocateAll()
    }
}
val pluginJar by tasks.creating(Jar::class.java) {
    archiveClassifier = "bukkit"
    from(zipTree(bukkitBootstrap.tasks.jar.map {
        it.archiveFile
    }))
    scheduler.subprojects.forEach {
        from(zipTree(it.tasks.jar.map { t ->
            t.archiveFile
        }))
    }
    bedrock.subprojects.forEach {
        from(zipTree(it.tasks.jar.map { t ->
            t.archiveFile
        }))
    }
    allNmsVersion.forEach {
        from(zipTree(it.tasks.named("reobfJar").map { t ->
            (t as RemapJar).outputJar
        }))
    }
    from(zipTree(tasks.shadowJar.map {
        it.archiveFile
    }))
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot"
    }
    doLast {
        relocateAll()
    }
}
val velocityJar by tasks.creating(Jar::class.java) {
    archiveClassifier = "velocity"
    from(zipTree(velocityBootstrap.tasks.jar.map {
        it.archiveFile
    }))
    from(zipTree(tasks.shadowJar.map {
        it.archiveFile
    }))
    doLast {
        relocateAll()
    }
}

fun Jar.relocateAll() {
    val file = archiveFile.get().asFile
    val tempFile = file.copyTo(File.createTempFile("jar-relocator", System.currentTimeMillis().toString()).apply {
        if (exists()) delete()
    })
    JarRelocator(
        tempFile,
        file,
        listOf(
            "kotlin",
            "net.objecthunter.exp4j",
            "org.bstats",
            "me.lucko.jarrelocator",
            "kr.toxicity.command.impl"
        ).map {
            Relocation(it, "${project.group}.shaded.$it")
        }
    ).run()
    tempFile.delete()
}

runPaper {
    disablePluginJarDetection()
}

dependencies {
    fun searchAll(target: Project) {
        val sub = target.subprojects
        if (sub.isNotEmpty()) sub.forEach {
            searchAll(it)
        } else dokka(target)
    }
    searchAll(rootProject)
}

tasks {
    runServer {
        version(minecraft)
        pluginJars(pluginJar.archiveFile)
        pluginJars(fileTree("plugins"))
    }
    build {
        finalizedBy(sourcesJar, javadocJar, pluginJar, velocityJar, fabricJar)
    }
    shadowJar {
        archiveClassifier = ""
        dependencies {
            exclude(dependency("org.jetbrains:annotations:13.0"))
        }
    }
}

bukkitBootstrap.modrinthPublish(
    pluginJar,
    listOf(sourcesJar, javadocJar),
    listOf("bukkit", "spigot", "paper", "purpur", "folia"),
    supportedMinecraftVersions,
    listOf(),
    listOf()
)

velocityBootstrap.modrinthPublish(
    velocityJar,
    listOf(sourcesJar, javadocJar),
    listOf("velocity"),
    supportedMinecraftVersions,
    listOf(),
    listOf()
)
fabricBootstrap.modrinthPublish(
    fabricJar,
    listOf(sourcesJar, javadocJar),
    listOf("fabric", "quilt"),
    listOf(minecraft),
    listOf("fabric-api"),
    listOf("luckperms", "placeholder-api", "polymer")
)

tasks.create("modrinthPublish") {
    dependsOn(*bootstrap.map {
        it.tasks.modrinth
    }.toTypedArray())
    dependsOn(tasks.modrinthSyncBody)
}

hangarPublish {
    publications.register("plugin") {
        version = project.version as String
        id = "BetterHud"
        apiKey = System.getenv("HANGAR_API_TOKEN")
        val log = System.getenv("COMMIT_MESSAGE")
        if (log != null) {
            changelog = log
            channel = "Snapshot"
        } else {
            changelog = rootProject.file("changelog/${project.version}.md").readText()
            channel = "Release"
        }
        platforms {
            register(Platforms.PAPER) {
                jar = file("build/libs/${project.name}-${project.version}-bukkit.jar")
                platformVersions = supportedMinecraftVersions
            }
            register(Platforms.VELOCITY) {
                jar = file("build/libs/${project.name}-${project.version}-velocity.jar")
                platformVersions = supportedVelocityVersions
            }
        }
    }
}

modrinth {
    token = System.getenv("MODRINTH_API_TOKEN")
    projectId = "betterhud2"
    syncBodyFrom = rootProject.file("BANNER.md").readText()
}