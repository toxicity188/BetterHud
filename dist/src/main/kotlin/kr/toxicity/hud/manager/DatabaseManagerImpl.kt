package kr.toxicity.hud.manager

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kr.toxicity.hud.api.configuration.HudObject
import kr.toxicity.hud.api.database.HudDatabase
import kr.toxicity.hud.api.database.HudDatabaseConnector
import kr.toxicity.hud.api.manager.DatabaseManager
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.PointedLocation
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import kr.toxicity.hud.yaml.YamlObjectImpl
import java.io.File
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

object DatabaseManagerImpl : BetterHudManager, DatabaseManager {

    override val managerName: String = "Database"
    override val supportExternalPacks: Boolean = false

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
                            if (!it.isDefault) it.add(player)
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
                    yaml["locations"]?.asArray()?.forEach {
                        runCatching {
                            player.pointers().add(PointedLocation.deserialize(it.asObject()))
                        }.onFailure { e ->
                            e.handle("unable to load ${player.name()}'s location.")
                        }
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
                    put("locations", player.pointers().map {
                        it.serialize()
                    })
                }.saveToYaml(getFile(player))
                return true
            }
        }
    }
    private var currentConnector = defaultConnector

    private val connectionMap = mutableMapOf(
        "yml" to defaultConnector,
        "mysql" to HudDatabaseConnector {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val host = it.get("host")?.asString().ifNull { "unable to find the host value." }
            val database = it.get("database")?.asString().ifNull { "unable to find the database value." }
            val mysql = HikariDataSource(HikariConfig().apply {
                jdbcUrl = "jdbc:mysql://$host/$database"
                username = it.get("name")?.asString().ifNull { "unable to find the name value." }
                password = it.get("password")?.asString().ifNull { "unable to find the password value." }
                maximumPoolSize = 10
                minimumIdle = 2
                idleTimeout = 30000
                maxLifetime = 1800000
                validationTimeout = 5000
                connectionTestQuery = "SELECT 1"
            }).apply {
                connection.use {
                    it.createStatement().use { s ->
                        s.execute("CREATE TABLE IF NOT EXISTS enabled_hud(uuid CHAR(36) NOT NULL, type VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL);")
                        s.execute("CREATE TABLE IF NOT EXISTS enabled_pointed_location(uuid CHAR(36), name VARCHAR(255), value TEXT NOT NULL, PRIMARY KEY(uuid, name));")
                    }
                }
            }
            object: HudDatabase {

                override fun close() {
                    mysql.close()
                }

                fun transaction(block: Connection.() -> Unit): Boolean {
                    return runCatching{
                        mysql.connection.use {
                            block(it)
                        }
                        true
                    }.onFailure { e ->
                        e.handle("Unable to connect mysql")
                    }.getOrDefault(false)
                }

                fun Connection.query(sql: String, query: PreparedStatement.() -> Unit = {}, block: ResultSet.() -> Unit) = prepareStatement(sql).use {
                    it.query()
                    it.executeQuery().query(block)
                }

                fun Connection.update(sql: String, block: PreparedStatement.() -> Unit = {}) = prepareStatement(sql).use {
                    it.block()
                    it.executeUpdate()
                }

                fun ResultSet.query(block: ResultSet.() -> Unit) = use {
                    while (it.next()) block(this)
                }

                override fun isClosed(): Boolean = mysql.isClosed

                override fun load(player: HudPlayer) {
                    asyncTask {
                        val uuid = player.uuid().toString()
                        transaction {
                            query("SELECT type, name FROM enabled_hud WHERE uuid = ?;", {
                                setString(1, uuid)
                            }) {
                                when (getString("type")) {
                                    "hud" -> HudManagerImpl.getHud(getString("name"))?.let { h ->
                                        if (!h.isDefault) h.add(player)
                                    }
                                    "popup" -> PopupManagerImpl.getPopup(getString("popup"))?.let { p ->
                                        if (!p.isDefault) p.add(player)
                                    }
                                    "compass" -> CompassManagerImpl.getCompass(getString("compass"))?.let { c ->
                                        if (!c.isDefault) c.add(player)
                                    }
                                }
                            }
                            query("SELECT type, name FROM enabled_hud WHERE uuid = ?;", {
                                setString(1, uuid)
                            }) {
                                when (getString("type")) {
                                    "hud" -> HudManagerImpl.getHud(getString("name"))?.let { h ->
                                        if (!h.isDefault) h.add(player)
                                    }
                                    "popup" -> PopupManagerImpl.getPopup(getString("popup"))?.let { p ->
                                        if (!p.isDefault) p.add(player)
                                    }
                                    "compass" -> CompassManagerImpl.getCompass(getString("compass"))?.let { c ->
                                        if (!c.isDefault) c.add(player)
                                    }
                                }
                            }
                            query("SELECT value FROM enabled_pointed_location WHERE uuid = ?;", {
                                setString(1, uuid)
                            }) {
                                runCatching {
                                    player.pointers().add(PointedLocation.deserialize(getString("value")
                                        .toBase64Json()
                                        .asJsonObject))
                                }.onFailure { e ->
                                    e.handle("unable to load ${player.name()}'s location.")
                                }
                            }
                        }
                    }
                }

                override fun save(player: HudPlayer): Boolean {
                    val uuid = player.uuid().toString()
                    return transaction {
                        update("DELETE FROM enabled_hud WHERE uuid = ?;") {
                            setString(1, uuid)
                        }
                        update("DELETE FROM enabled_pointed_location WHERE uuid = ?;") {
                            setString(1, uuid)
                        }
                        fun save(name: String, supplier: () -> Set<HudObject>) {
                            val values = supplier().filter { !it.isDefault }.joinToString(", ") { h ->
                                "('$uuid', '$name', '${h.name}')"
                            }
                            if (values.isNotEmpty()) {
                                update("INSERT INTO enabled_hud(uuid, type, name) VALUES $values;")
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
                        val pointerValues = player.pointers().joinToString(", ") { p ->
                            "('$uuid', '${p.name}', '${p.serialize().toJsonElement().toBase64String()}')"
                        }
                        if (pointerValues.isNotEmpty()) {
                            update("INSERT INTO enabled_pointed_location(uuid, name, value) VALUES $pointerValues;")
                        }
                    }
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

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
        runCatching {
            val db = PluginConfiguration.DATABASE.create()
            val type = db.get("type")?.asString().ifNull { "type value not set." }
            val dbInfo = db.get("info")?.asObject().ifNull { "info configuration not set." }
            val connector = connectionMap[type].ifNull { "this database doesn't exist: $type" }
            if (currentConnector === connector) return
            current.close()
            current = connector.connect(dbInfo)
            currentConnector = connector
        }.onFailure { e ->
            current = defaultConnector.connect(YamlObjectImpl("", mutableMapOf<String, Any>()))
            e.handle("Unable to connect the database.")
        }
    }

    override fun end() {
    }
}