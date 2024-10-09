package kr.toxicity.hud.pack

import com.google.gson.JsonPrimitive
import com.sun.net.httpserver.HttpServer
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.util.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.util.*

object PackUploader {

    interface PackServer {
        fun stop()
        val uuid: UUID
        val url: String
        val digest: ByteArray
        val digestString: String
    }
    @Volatile
    var server: PackServer? = null
        private set

    fun stop(): Boolean {
        val result = server?.stop() != null
        server = null
        return result
    }

    fun upload(message: MessageDigest, byteArray: ByteArray) {
        val hash = StringBuilder(40)
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
        fun openServer(body: String) {
            info("opening server...");
            val host = ConfigManagerImpl.selfHostPort
            val url = "http://$body:$host/$string.zip"
            runWithExceptionHandling(CONSOLE, "Unable to open server.") {

                info("ip: " + InetAddress.getLocalHost() + "port: " + host);

                server?.stop()
                val http = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), host), 0).apply {
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

                    override val uuid: UUID = uuid
                    override val digest: ByteArray = digest
                    override val digestString: String = string
                    override val url: String = url
                }
                BOOTSTRAP.sendResourcePack()
                info("Resource pack server opened at $url")
            }
        }
        when (val host = ConfigManagerImpl.selfHostIp) {
            "*" -> HttpClient.newHttpClient()
                .sendAsync(HttpRequest.newBuilder()
                    .uri(URI.create("http://checkip.amazonaws.com/"))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString()).thenAccept {
                        val body = it.body()
                        openServer(body.substring(0, body.length - 1))
                }
            else -> openServer(host)
        }

    }
}