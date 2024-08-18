package kr.toxicity.hud.manager

import kr.toxicity.hud.api.configuration.HudObject
import kr.toxicity.hud.api.database.HudDatabase
import kr.toxicity.hud.api.database.HudDatabaseConnector
import kr.toxicity.hud.api.manager.DatabaseManager
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import kr.toxicity.hud.yaml.YamlObjectImpl
import net.kyori.adventure.audience.Audience
import java.io.File
import java.sql.DriverManager
import java.util.concurrent.CompletableFuture

object DatabaseManagerImpl: BetterHudManager, DatabaseManager {


    private val defaultConnector = HudDatabaseConnector {
        object : HudDatabase {

            private var closed = false

            private fun getFile(player: HudPlayer): File {
                return DATA_FOLDER
                    .subFolder(".users")
                    .subFile("${player.uuid()}.yml")
            }

            override fun isClosed(): Boolean = closed

            override fun close() {
                closed = true
            }

            override fun load(player: HudPlayer) {
                asyncTask {
                    val yaml = getFile(player).toYaml()
                    fun add(name: String, mapper: (String) -> HudObject?) {
                        yaml.get(name)?.asArray()?.mapNotNull {
                            mapper(it.asString())
                        }?.forEach {
                            if (!it.isDefault) player.hudObjects.add(it)
                        }
                    }
                    add("huds") {
                        HudManagerImpl.getHud(it)
                    }
                    add("popups") {
                        PopupManagerImpl.getPopup(it)
                    }
                    add("compasses") {
                        CompassManagerImpl.getCompass(it)
                    }
                }
            }

            override fun save(player: HudPlayer): Boolean {
                LinkedHashMap<String, Any>().apply {
                    fun save(name: String, supplier: () -> Set<HudObject>) {
                        put(name, supplier().filter {
                            !it.isDefault
                        }.map {
                            it.name
                        })
                    }
                    save("popups") {
                        player.popups
                    }
                    save("huds") {
                        player.huds
                    }
                    save("compasses") {
                        player.compasses
                    }
                }.saveToYaml(getFile(player))
                return true
            }
        }
    }

    private val connectionMap = mutableMapOf(
        "yml" to defaultConnector,
        "mysql" to HudDatabaseConnector {
            val host = it.get("host")?.asString().ifNull("unable to find the host value.")
            val database = it.get("database")?.asString().ifNull("unable to find the database value.")
            val name = it.get("name")?.asString().ifNull("unable to find the name value.")
            val password = it.get("password")?.asString().ifNull("unable to find the password value.")

            val mysql = DriverManager.getConnection("jdbc:mysql://$host/$database?autoReconnect=true&useSSL=false&cmaxReconnets=5&initialTimeout=1", name, password).apply {
                createStatement().use { s ->
                    s.execute("CREATE TABLE IF NOT EXISTS enabled_hud(uuid CHAR(36) NOT NULL, type VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL);")
                }
            }
            object: HudDatabase {

                override fun close() {
                    mysql.close()
                }

                override fun isClosed(): Boolean = mysql.isClosed

                override fun load(hudPlayer: HudPlayer) {
                    asyncTask {
                        val uuid = hudPlayer.uuid().toString()
                        mysql.prepareStatement("SELECT type, name FROM enabled_hud WHERE uuid = '$uuid';").use { s ->
                            val result = s.executeQuery()
                            while (result.next()) {
                                when (result.getString("type")) {
                                    "hud" -> HudManagerImpl.getHud(result.getString("name"))?.let { h ->
                                        if (!h.isDefault) hudPlayer.hudObjects.add(h)
                                    }
                                    "popup" -> PopupManagerImpl.getPopup(result.getString("popup"))?.let { p ->
                                        if (!p.isDefault) hudPlayer.hudObjects.add(p)
                                    }
                                    "compass" -> CompassManagerImpl.getCompass(result.getString("compass"))?.let { p ->
                                        if (!p.isDefault) hudPlayer.hudObjects.add(p)
                                    }
                                }
                            }
                        }
                    }
                }

                override fun save(player: HudPlayer): Boolean {
                    val uuid = player.uuid().toString()
                    mysql.run {
                        prepareStatement("DELETE FROM enabled_hud WHERE uuid = '$uuid';").use { s ->
                            s.executeUpdate()
                        }
                        fun save(name: String, supplier: () -> Set<HudObject>) {
                            supplier().filter { h ->
                                !h.isDefault
                            }.forEach { h ->
                                prepareStatement("INSERT INTO enabled_hud(uuid, type, name) VALUES(?, ?, ?);").use { s ->
                                    s.setString(1, uuid)
                                    s.setString(2, name)
                                    s.setString(3, h.name)
                                    s.executeUpdate()
                                }
                            }
                        }
                        save("hud") {
                            player.huds
                        }
                        save("popup") {
                            player.popups
                        }
                        save("compass") {
                            player.compasses
                        }
                    }
                    return true
                }


            }
        }
    )

    private var current = defaultConnector.connect(YamlObjectImpl("", mutableMapOf<String, Any>()))
    override fun start() {

    }

    override fun getCurrentDatabase(): HudDatabase = current
    override fun addDatabase(name: String, connector: HudDatabaseConnector): Boolean {
        return connectionMap.putIfAbsent(name, connector) == null
    }

    override fun reload(sender: Audience, resource: GlobalResource) {
        CompletableFuture.runAsync {
            synchronized(this) {
                runCatching {
                    current.close()
                    val db = PluginConfiguration.DATABASE.create()
                    val type = db.get("type")?.asString().ifNull("type value not set.")
                    val info = db.get("info")?.asObject().ifNull("info configuration not set.")
                    current = connectionMap[type].ifNull("this database doesn't exist: $type").connect(info)
                }.onFailure { e ->
                    current = defaultConnector.connect(YamlObjectImpl("", mutableMapOf<String, Any>()))
                    warn(
                        "Unable to connect the database.",
                        "Reason: ${e.message}"
                    )
                    if (ConfigManagerImpl.debug) e.printStackTrace()
                }
            }
        }.handle { _, e ->
            e?.printStackTrace()
        }.join()
    }

    override fun end() {
    }
}