package kr.toxicity.hud.dependency

import kr.toxicity.hud.api.BetterHudDependency
import kr.toxicity.hud.api.BetterHudLogger
import kr.toxicity.hud.util.subFile
import kr.toxicity.hud.util.subFolder
import kr.toxicity.hud.util.toYaml
import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLClassLoader


class DependencyInjector(version: String, dataFolder: File, private val logger: BetterHudLogger, classLoader: URLClassLoader) {
    companion object {
        private const val CENTERAL = "https://repo1.maven.org/maven2"
    }

    private fun interface UrlProcessor : (URL) -> Unit

    private val addUrl: UrlProcessor = runCatching {
        val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java).apply {
            isAccessible = true
        }
        UrlProcessor { p1 -> method(classLoader, p1) }
    }.getOrElse {
        val unsafe = UnsafeURLClassLoader(classLoader)
        UrlProcessor { p1 -> unsafe.addURL(p1) }
    }

    private val dir = dataFolder.apply {
        mkdirs()
    }.subFolder(".libraries")

    init {
        if (dataFolder.subFile("version.txt").readText() != version) dir.deleteRecursively()
    }

    fun load(dependency: BetterHudDependency) {
        val file = File(dir, dependency.toPath().replace('/', File.separatorChar)).apply {
            parentFile.mkdirs()
        }
        if (!file.exists() || file.length() == 0L) {
            logger.info("Downloading ${dependency.name}-${dependency.version}...")
            val connection = URI.create("$CENTERAL/${dependency.toPath()}").toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.inputStream.buffered().use { input ->
                if (dependency.isRelocate) {
                    val tempFile = File.createTempFile(file.name, System.currentTimeMillis().toString()).apply {
                        outputStream().buffered().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val target = dependency.group.replace("{}", "/")
                    JarRelocator(
                        tempFile,
                        file,
                        listOf(Relocation(target, "kr.toxicity.hud.shaded.$target"))
                    ).run()
                    tempFile.delete()
                } else {
                    file.outputStream().buffered().use { output ->
                        input.copyTo(output)
                    }
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


    private fun BetterHudDependency.toPath(): String = "${group.replace("{}", "/")}/$name/$version/$name-$version.jar"

}