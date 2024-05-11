plugins {
    `java-library`
    kotlin("jvm") version("1.9.24")
    id("io.github.goooler.shadow") version("8.1.7")
    id("io.papermc.paperweight.userdev") version("1.7.0") apply(false)
    id("xyz.jpenilla.run-paper") version("2.3.0")
    id("org.jetbrains.dokka") version("1.9.20")
}

val minecraft = "1.20.6"
val folia = "1.20.4" // TODO Bumps version.
val adventure = "4.17.0"
val platform = "4.3.2"
val targetJavaVersion = 21

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
    "v1_20_R4"
)

val allNmsVersion = ArrayList<String>().apply {
    addAll(legacyNmsVersion)
    addAll(currentNmsVersion)
}

val api = project(":api")
val dist = project(":dist")
val scheduler = project(":scheduler")
val bedrock = project(":bedrock")

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    group = "kr.toxicity.hud"
    version = "beta-23"

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

        implementation("net.objecthunter:exp4j:0.4.8")
        implementation("org.bstats:bstats-bukkit:3.0.2")
        implementation("net.byteflux:libby-bukkit:1.3.0")

        implementation(rootProject.fileTree("shaded"))
    }

    tasks {
        test {
            useJUnitPlatform()
        }
        compileJava {
            options.encoding = Charsets.UTF_8.name()
        }
    }
}

subprojects {
    val targetJavaVersion = 17

    java {
        toolchain.vendor = JvmVendorSpec.ADOPTIUM
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }

    kotlin {
        jvmToolchain(targetJavaVersion)
    }
    tasks {
        build {
            finalizedBy(clean)
        }
    }
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

listOf(
    api,
    dist,
    scheduler.project("standard")
).forEach {
    it.dependencies {
        compileOnly("net.kyori:adventure-api:$adventure")
        compileOnly("net.kyori:adventure-platform-bukkit:$platform")
        compileOnly("org.spigotmc:spigot-api:$minecraft-R0.1-SNAPSHOT")
    }
}

scheduler.project("folia").dependencies {
    compileOnly("dev.folia:folia-api:$folia-R0.1-SNAPSHOT")
}

dist.dependencies {
    compileOnly(api)
    scheduler.subprojects.forEach {
        compileOnly(it)
    }
    bedrock.subprojects.forEach {
        compileOnly(it)
    }
    allNmsVersion.forEach {
        compileOnly(project(":nms:$it"))
    }
}

project(":nms").subprojects.forEach {
    it.apply(plugin = "io.papermc.paperweight.userdev")
}

dependencies {
    implementation(api)
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
    }
    shadowJar {
        legacyNmsVersion.forEach {
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
        prefix("net.byteflux")
        relocate("net.kyori", "hud.net.kyori")
        finalizedBy(sourceJar)
        finalizedBy(dokkaJar)
    }
}

ArrayList<Project>().apply {
    add(project)
    addAll(currentNmsVersion.map {
        project("nms:$it")
    })
}.forEach {
    it.java {
        toolchain.vendor = JvmVendorSpec.ADOPTIUM
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
    it.kotlin {
        jvmToolchain(targetJavaVersion)
    }
}