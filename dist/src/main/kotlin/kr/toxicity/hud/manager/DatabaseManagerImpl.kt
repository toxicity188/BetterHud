package kr.toxicity.hud.manager

import kr.toxicity.hud.api.configuration.HudObject
import kr.toxicity.hud.api.database.HudDatabase
import kr.toxicity.hud.api.database.HudDatabaseConnector
import kr.toxicity.hud.api.manager.DatabaseManager
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.sql.DriverManager
import java.util.concurrent.CompletableFuture

object DatabaseManagerImpl: BetterHudManager, DatabaseManager {


    private val defaultConnector = HudDatabaseConnector {
        object : HudDatabase {

            private fun getFile(player: Player): File {
                return DATA_FOLDER
                    .subFolder(".users")
                    .subFile("${player.uniqueId}.yml")
            }

            override fun close() {
            }

            override fun load(player: Player): HudPlayer {
                val yaml = getFile(player).toYaml()
                val set = HashSet<HudObject>()
                fun add(name: String, mapper: (String) -> HudObject?) {
                    yaml.getStringList(name).mapNotNull(mapper).forEach {
                        if (!it.isDefault) set.add(it)
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
                return HudPlayerImpl(
                    player,
                    set
                )
            }

            override fun save(player: HudPlayer): Boolean {
                YamlConfiguration().run {
                    fun save(name: String, supplier: () -> Set<HudObject>) {
                        set(name, supplier().filter {
                            !it.isDefault
                        }.map {
                            it.name
                        }.toTypedArray())
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
                    save(getFile(player.bukkitPlayer))
                }
                return true
            }
        }
    }

    private val connectionMap = mapOf(
        "yml" to defaultConnector,
        "mysql" to HudDatabaseConnector {
            val host = it.getString("host").ifNull("unable to find the host value.")
            val database = it.getString("database").ifNull("unable to find the database value.")
            val name = it.getString("name").ifNull("unable to find the name value.")
            val password = it.getString("password").ifNull("unable to find the password value.")

            val mysql = DriverManager.getConnection("jdbc:mysql://$host/$database?autoReconnect=true&useSSL=false&cmaxReconnets=5&initialTimeout=1", name, password).apply {
                createStatement().use { s ->
                    s.execute("CREATE TABLE IF NOT EXISTS enabled_hud(uuid CHAR(36) NOT NULL, type VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL);")
                }
            }
            object: HudDatabase {
                override fun close() {
                    mysql.close()
                }

                override fun load(player: Player): HudPlayer {
                    val uuid = player.uniqueId.toString()
                    val set = HashSet<HudObject>()
                    mysql.prepareStatement("SELECT type, name FROM enabled_hud WHERE uuid = '$uuid';").use { s ->
                        val result = s.executeQuery()
                        while (result.next()) {
                            when (result.getString("type")) {
                                "hud" -> HudManagerImpl.getHud(result.getString("name"))?.let { h ->
                                    if (!h.isDefault) set.add(h)
                                }
                                "popup" -> PopupManagerImpl.getPopup(result.getString("popup"))?.let { p ->
                                    if (!p.isDefault) set.add(p)
                                }
                                "compass" -> CompassManagerImpl.getCompass(result.getString("compass"))?.let { p ->
                                    if (!p.isDefault) set.add(p)
                                }
                            }
                        }
                    }
                    return HudPlayerImpl(player, set)
                }

                override fun save(player: HudPlayer): Boolean {
                    val uuid = player.bukkitPlayer.uniqueId.toString()
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

    private var current = defaultConnector.connect(MemoryConfiguration())
    override fun start() {

    }

    override fun getCurrentDatabase(): HudDatabase = current

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        CompletableFuture.runAsync {
            synchronized(this) {
                runCatching {
                    current.close()
                    val db = PluginConfiguration.DATABASE.create()
                    val type = db.getString("type").ifNull("type value not set.")
                    val info = db.getConfigurationSection("info").ifNull("info configuration not set.")
                    current = connectionMap[type].ifNull("this database doesn't exist: $type").connect(info)
                }.onFailure { e ->
                    current = defaultConnector.connect(MemoryConfiguration())
                    warn(
                        "Unable to connect the database.",
                        "Reason: ${e.message}"
                    )
                }
                callback()
            }
        }.handle { _, e ->
            e.printStackTrace()
            callback()
        }
    }

    override fun end() {
    }
}