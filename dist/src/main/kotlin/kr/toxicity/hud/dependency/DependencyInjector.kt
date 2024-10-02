package kr.toxicity.hud.dependency

import kr.toxicity.hud.api.BetterHudLogger
import kr.toxicity.hud.util.subFile
import kr.toxicity.hud.util.subFolder
import kr.toxicity.hud.util.toYaml
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLClassLoader


class DependencyInjector(version: String, dataFolder: File, private val logger: BetterHudLogger, classLoader: URLClassLoader) {
    companion object {

        private const val CENTERAL = "https://repo1.maven.org/maven2"
    }

    private val addUrl: (URL) -> Unit = runCatching {
        val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java).apply {
            isAccessible = true
        }
        object : (URL) -> Unit {
            override fun invoke(p1: URL) {
                method(classLoader, p1)
            }
        }
    }.getOrElse {
        val unsafe = UnsafeURLClassLoader(classLoader)
        object : (URL) -> Unit {
            override fun invoke(p1: URL) {
                unsafe.addURL(p1)
            }
        }
    }

    private val dir = dataFolder.subFolder(".libraries")

    init {
        if (dataFolder.subFile("version.yml").toYaml().get("plugin-version")?.asString() != version) dir.deleteRecursively()
    }

    fun load(dependency: Dependency) {
        val file = File(dir, dependency.toPath().replace('/', File.separatorChar)).apply {
            parentFile.mkdirs()
        }
        if (!file.exists() || file.length() == 0L) {
            logger.info("Downloading ${dependency.name}-${dependency.version}...")
            val connection = URI.create("$CENTERAL/${dependency.toPath()}").toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.inputStream.buffered().use { input ->
                file.outputStream().buffered().use { output ->
                    input.copyTo(output)
                }
            }
            connection.disconnect()
        }
        addUrl(file.toURI().toURL())
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    private class UnsafeURLClassLoader(classLoader: URLClassLoader) {
        private val unopenedURLs: MutableCollection<URL>
        private val pathURLs: MutableCollection<URL>

        init {
            val unsafe = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe").run {
                isAccessible = true
                get(null) as sun.misc.Unsafe
            }
            fun fetchField(clazz: Class<*>, any: Any, name: String): Any {
                return unsafe.getObject(any, unsafe.objectFieldOffset(clazz.getDeclaredField(name)))
            }
            try {
                val ucp: Any = fetchField(URLClassLoader::class.java, classLoader, "ucp")
                unopenedURLs = fetchField(ucp.javaClass, ucp, "unopenedUrls") as MutableCollection<URL>
                pathURLs = fetchField(ucp.javaClass, ucp, "path") as MutableCollection<URL>
            } catch (e: Throwable) {
                throw RuntimeException("Unsupported jdk.")
            }
        }

        fun addURL(url: URL) {
            unopenedURLs.add(url)
            pathURLs.add(url)
        }
    }


    private fun Dependency.toPath(): String = "${group.replace("{}", "/")}/$name/$version/$name-$version.jar"

}