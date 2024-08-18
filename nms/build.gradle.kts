val api = rootProject.project("api")

subprojects {
    dependencies {
        api.subprojects.forEach {
            compileOnly(it)
        }
    }
}