package kr.toxicity.hud.pack

import com.google.gson.JsonPrimitive
import com.sun.net.httpserver.HttpServer
import kr.toxicity.hud.api.nms.NMSVersion
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.util.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.util.*


object PackUploader {

    private interface PackServer {
        fun stop()
        fun apply(player: Player)
    }
    @Volatile
    private var server: PackServer? = null

    fun apply(player: Player): Boolean {
        return server?.apply(player) != null
    }
    fun stop(): Boolean {
        val result = server?.stop() != null
        server = null
        return result
    }

    fun upload(message: MessageDigest, byteArray: ByteArray) {
        val hash = StringBuilder(40)
        val useUrl = VERSION >= NMSVersion.V1_20_R3
        val digest = message.digest()
        for (element in digest) {
            val byte = element.toInt()
            hash.append(Character.forDigit((byte shr 4) and 15, 16))
                .append(Character.forDigit(byte and 15, 16))
        }
        val string = hash.toString()
        var t = 0
        val uuid = UUID.nameUUIDFromBytes(ByteArray(20) {
            ((Character.digit(hash.codePointAt(t++), 16) shl 4) or Character.digit(hash.codePointAt(t++), 16)).toByte()
        })
        HttpClient.newHttpClient()
            .sendAsync(HttpRequest.newBuilder()
                .uri(URI.create("http://checkip.amazonaws.com/"))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString()).thenAccept {
                    val host = ConfigManagerImpl.selfHostPort
                    val body = it.body()
                    val url = "http://${body.substring(0, body.length - 1)}:$host/$string.zip"
                    runCatching {
                        server?.stop()
                        val http = HttpServer.create(InetSocketAddress(InetAddress.getLocalHost(), host), 0).apply {
                            createContext("/") { exec ->
                                exec.use { exchange ->
                                    if (exchange.requestURI.path != "/$string.zip") {
                                        exchange.responseHeaders.set("Content-Type", "application/json")
                                        val byte = JsonPrimitive("Invalid file name.").toByteArray()
                                        exchange.sendResponseHeaders(200, byte.size.toLong())
                                        exchange.responseBody.write(byte)
                                    } else {
                                        exchange.responseHeaders.set("Content-Type", "application/zip")
                                        exchange.sendResponseHeaders(200, byteArray.size.toLong())
                                        exchange.responseBody.write(byteArray)
                                    }
                                }
                            }
                            start()
                        }
                        server = object : PackServer {
                            override fun stop() {
                                http.stop(0)
                            }
                            override fun apply(player: Player) {
                                if (useUrl) {
                                    player.setResourcePack(uuid, url, digest, null, false)
                                } else {
                                    player.setResourcePack(url, digest, null, false)
                                }
                            }
                        }
                        Bukkit.getOnlinePlayers().forEach { player ->
                            if (useUrl) {
                                player.setResourcePack(uuid, url, digest, null, false)
                            } else {
                                player.setResourcePack(url, digest, null, false)
                            }
                        }
                        info("Resource pack server opened at $url")
                    }.onFailure { e ->
                        e.printStackTrace()
                        warn("Unable to open server.")
                    }
            }
    }
}