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

    fun upload(packUUID: PackUUID, byteArray: ByteArray) {
        fun openServer(body: String) {
            val host = ConfigManagerImpl.selfHostPort
            val url = "http://$body:$host/${packUUID.hash}.zip"
            runCatching {
                server?.stop()
                packUUID.save()
                val http = HttpServer.create(InetSocketAddress(InetAddress.getLocalHost(), host), 0).apply {
                    createContext("/") { exec ->
                        exec.use { exchange ->
                            if (exchange.requestURI.path != "/${packUUID.hash}.zip") {
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

                    override val uuid: UUID = packUUID.uuid
                    override val digest: ByteArray = packUUID.digest
                    override val digestString: String = packUUID.hash
                    override val url: String = url
                }
                BOOTSTRAP.sendResourcePack()
                info("Resource pack server opened at $url")
            }.onFailure {
                it.handle("Unable to open server.")
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