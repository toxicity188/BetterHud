plugins {
    `maven-publish`
}

subprojects {
    apply(plugin = "maven-publish")

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")

        testCompileOnly("org.projectlombok:lombok:1.18.34")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
    }

    val sourcesJar by tasks.creating(Jar::class.java) {
        from(sourceSets.main.get().allJava)
        archiveClassifier = "sources"
    }
    val javadocJar by tasks.creating(Jar::class.java) {
        dependsOn(tasks.javadoc)
        archiveClassifier = "javadoc"
        from(tasks.javadoc.get().destinationDir)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                afterEvaluate {
                    artifact(javadocJar)
                    artifact(sourcesJar)
                }
            }
        }
    }
}