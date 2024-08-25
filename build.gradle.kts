plugins {
    `java-library`
    kotlin("jvm") version("2.0.10")
    id("io.github.goooler.shadow") version("8.1.8")
    id("io.papermc.paperweight.userdev") version("1.7.2") apply(false)
    id("xyz.jpenilla.run-paper") version("2.3.0")
    id("org.jetbrains.dokka") version("1.9.20")
}

val minecraft = "1.21.1"
val folia = "1.20.6" // TODO bumps version to 1.21.1
val adventure = "4.17.0"
val platform = "4.3.4"
val targetJavaVersion = 21
val velocity = "3.3.0"
val bStats = "3.0.2"

val legacyNmsVersion = listOf(
    "v1_17_R1",
    "v1_18_R1",
    "v1_18_R2",
    "v1_19_R1",
    "v1_19_R2",
    "v1_19_R3",
    "v1_20_R1",
    "v1_20_R2",
    "v1_20_R3",
)
val currentNmsVersion = listOf(
    "v1_20_R4",
    "v1_21_R1"
)

val allNmsVersion = ArrayList<String>().apply {
    addAll(legacyNmsVersion)
    addAll(currentNmsVersion)
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    group = "kr.toxicity.hud"
    version = "1.4"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.opencollab.dev/main/")
        maven("https://repo.skriptlang.org/releases")
        maven("https://repo.alessiodp.com/releases/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://mvn.lumine.io/repository/maven/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test")
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
                freeCompilerArgs.addAll(listOf("-jvm-target", "17"))
            }
        }
    }
    java {
        toolchain.vendor = JvmVendorSpec.ADOPTIUM
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}


fun Project.bukkit() = also {
    it.dependencies {
        compileOnly("org.spigotmc:spigot-api:$minecraft-R0.1-SNAPSHOT")
        compileOnly("org.bstats:bstats-bukkit:$bStats")
        compileOnly(rootProject.fileTree("shaded"))
    }
}
fun Project.velocity() = also {
    it.dependencies {
        compileOnly("com.velocitypowered:velocity-api:$velocity-SNAPSHOT")
        compileOnly("com.velocitypowered:velocity-proxy:$velocity-SNAPSHOT")
        annotationProcessor("com.velocitypowered:velocity-api:$velocity-SNAPSHOT")
        compileOnly("io.netty:netty-all:4.1.112.Final")
        compileOnly("org.bstats:bstats-velocity:$bStats")
    }
}
fun Project.folia() = also {
    it.dependencies {
        compileOnly("dev.folia:folia-api:$folia-R0.1-SNAPSHOT")
    }
}
fun Project.adventure() = also {
    it.dependencies {
        compileOnly("net.kyori:adventure-api:$adventure")
        compileOnly("net.kyori:adventure-text-minimessage:$adventure")
        compileOnly("net.kyori:adventure-text-serializer-legacy:$adventure")
        compileOnly("net.kyori:adventure-text-serializer-gson:$adventure")
    }
}
fun Project.library() = also {
    it.dependencies {
        implementation("org.yaml:snakeyaml:2.2")
        implementation("com.google.code.gson:gson:2.11.0")
        implementation("net.objecthunter:exp4j:0.4.8")
        implementation(rootProject.fileTree("shaded"))
    }
}
fun Project.bukkitAudience() = also {
    it.dependencies {
        compileOnly("net.kyori:adventure-platform-bukkit:$platform")
    }
}
fun Project.legacy() = also {
    it.java {
        toolchain.languageVersion = JavaLanguageVersion.of(17)
    }
}
fun Project.dependency(any: Any) = also {
    it.dependencies.compileOnly(any)
}

val apiShare = project("api:standard-api").adventure().legacy()

val api = listOf(
    apiShare,
    project("api:bukkit-api").adventure().bukkit().dependency(apiShare).legacy(),
    project("api:velocity-api").velocity().dependency(apiShare).legacy()
)

fun Project.api() = also {
    it.dependencies {
        api.forEach { p ->
            compileOnly(p)
        }
    }
}

val dist = project("dist").adventure().library().api()
val scheduler = project("scheduler")
val bedrock = project("bedrock")



legacyNmsVersion.map {
    project("nms:$it")
}.forEach {
    it.legacy()
}

fun branch(project: Project) {
    if (project.subprojects.isNotEmpty()) {
        project.subprojects.forEach {
            branch(it)
        }
    } else {
        project.apply(plugin = "org.jetbrains.dokka")
    }
}
branch(project)

scheduler.project("standard").adventure().bukkit().api()
scheduler.project("folia").folia().api()

dist.dependencies {
    allNmsVersion.forEach {
        compileOnly(project(":nms:$it"))
    }
}


val bootstrap = listOf(
    project("bootstrap:bukkit")
        .adventure()
        .bukkit()
        .api()
        .dependency(dist)
        .bukkitAudience()
        .also {
            it.dependencies {
                scheduler.subprojects.forEach { p ->
                    compileOnly(p)
                }
                bedrock.subprojects.forEach { p ->
                    compileOnly(p)
                }
                allNmsVersion.forEach { p ->
                    compileOnly(project(":nms:$p"))
                }
            }
    },
    project("bootstrap:velocity").velocity().api().dependency(dist)
)

project(":nms").subprojects.forEach {
    it.apply(plugin = "io.papermc.paperweight.userdev")
}

dependencies {
    api.forEach {
        implementation(it)
    }
    implementation(dist)
    scheduler.subprojects.forEach {
        implementation(it)
    }
    bedrock.subprojects.forEach {
        implementation(it)
    }
    allNmsVersion.forEach {
        implementation(project(":nms:$it", configuration = "reobf"))
    }
    bootstrap.forEach {
        implementation(it)
    }
    implementation("org.bstats:bstats-bukkit:$bStats")
    implementation("org.bstats:bstats-velocity:$bStats")
}

val sourceJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.classes)
    fun getProjectSource(project: Project): Array<File> {
        return if (project.subprojects.isEmpty()) project.sourceSets.main.get().allSource.srcDirs.toTypedArray() else ArrayList<File>().apply {
            project.subprojects.forEach {
                addAll(getProjectSource(it))
            }
        }.toTypedArray()
    }
    archiveClassifier = "source"
    from(*getProjectSource(project))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
val dokkaJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.dokkaHtmlMultiModule)
    archiveClassifier = "dokka"
    from(layout.buildDirectory.dir("dokka${File.separatorChar}htmlMultiModule").orNull?.asFile)
}

tasks {
    jar {
        finalizedBy(shadowJar)
    }
    runServer {
        version(minecraft)
        pluginJars(fileTree("plugins"))
    }
    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "spigot"
        }
        allNmsVersion.forEach {
            dependsOn(":nms:$it:reobfJar")
        }
        archiveClassifier = ""
        fun prefix(pattern: String) {
            relocate(pattern, "${project.group}.shaded.$pattern")
        }
        dependencies {
            exclude(dependency("org.jetbrains:annotations:13.0"))
        }
        prefix("kotlin")
        prefix("net.objecthunter.exp4j")
        prefix("org.bstats")
        prefix("org.objectweb.asm")
        prefix("me.lucko.jarrelocator")
        prefix("org.yaml.snakeyaml")
        prefix("com.google.gson")
        prefix("com.google.errorprone")
        finalizedBy(sourceJar)
        finalizedBy(dokkaJar)
    }
}
