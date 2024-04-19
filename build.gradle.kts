plugins {
    `java-library`
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("io.papermc.paperweight.userdev") version("1.5.12") apply(false)
    id("org.jetbrains.dokka") version "1.9.20"
}

val minecraft = "1.20.4" // TODO Bumps version.
val adventure = "4.16.0"
val platform = "4.3.2"

val nmsVersion = listOf(
    "v1_17_R1",
    "v1_18_R1",
    "v1_18_R2",
    "v1_19_R1",
    "v1_19_R2",
    "v1_19_R3",
    "v1_20_R1",
    "v1_20_R2",
    "v1_20_R3",
    //"v1_20_R4",
)

val api = project(":api")
val dist = project(":dist")
val scheduler = project(":scheduler")
val bedrock = project(":bedrock")

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    group = "kr.toxicity.hud"
    version = "beta-15-HOTFIX"

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
    }

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test")

        implementation("net.objecthunter:exp4j:0.4.8")
        implementation("org.bstats:bstats-bukkit:3.0.2")
        implementation("net.byteflux:libby-bukkit:1.3.0")
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
    compileOnly("dev.folia:folia-api:$minecraft-R0.1-SNAPSHOT")
}

dist.dependencies {
    compileOnly(api)
    scheduler.subprojects.forEach {
        compileOnly(it)
    }
    bedrock.subprojects.forEach {
        compileOnly(it)
    }
    nmsVersion.forEach {
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
    nmsVersion.forEach {
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
    from(layout.buildDirectory.dir("dokka\\htmlMultiModule").orNull?.asFile)
}

tasks {
    jar {
        dependsOn(clean)
        finalizedBy(shadowJar)
    }
    shadowJar {
        nmsVersion.forEach {
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

val targetJavaVersion = 17

java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}

kotlin {
    jvmToolchain(targetJavaVersion)
}