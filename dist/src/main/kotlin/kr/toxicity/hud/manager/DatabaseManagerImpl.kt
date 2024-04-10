package kr.toxicity.hud.manager

import kr.toxicity.hud.api.database.HudDatabase
import kr.toxicity.hud.api.database.HudDatabaseConnector
import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.manager.DatabaseManager
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.sql.DriverManager

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
                return HudPlayerImpl(
                    player,
                    yaml.getStringList("huds").mapNotNull {
                        HudManagerImpl.getHud(it)
                    }.filter {
                        !it.isDefault
                    }.toMutableSet(),
                    yaml.getStringList("popups").mapNotNull {
                        PopupManagerImpl.getPopup(it)
                    }.filter {
                        !it.isDefault
                    }.toMutableSet()
                )
            }

            override fun save(player: HudPlayer): Boolean {
                YamlConfiguration().run {
                    set("popups", player.popups.filter {
                        !it.isDefault
                    }.map {
                        it.name
                    }.toTypedArray())
                    set("huds", player.huds.filter {
                        !it.isDefault
                    }.map {
                        it.name
                    }.toTypedArray())
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
                    val huds = HashSet<Hud>()
                    val popups = HashSet<Popup>()
                    mysql.prepareStatement("SELECT type, name FROM enabled_hud WHERE uuid = '$uuid';").use { s ->
                        val result = s.executeQuery()
                        while (result.next()) {
                            when (result.getString("type")) {
                                "hud" -> HudManagerImpl.getHud(result.getString("name"))?.let { h ->
                                    if (!h.isDefault) huds.add(h)
                                }
                                "popup" -> PopupManagerImpl.getPopup(result.getString("popup"))?.let { p ->
                                    if (!p.isDefault) popups.add(p)
                                }
                            }
                        }
                    }
                    return HudPlayerImpl(player, huds, popups)
                }

                override fun save(player: HudPlayer): Boolean {
                    val uuid = player.bukkitPlayer.uniqueId.toString()
                    mysql.run {
                        prepareStatement("DELETE FROM enabled_hud WHERE uuid = '$uuid';").use { s ->
                            s.executeUpdate()
                        }
                        player.huds.filter { h ->
                            !h.isDefault
                        }.forEach { h ->
                            prepareStatement("INSERT INTO enabled_hud(uuid, type, name) VALUES(?, ?, ?);").use { s ->
                                s.setString(1, uuid)
                                s.setString(2, "hud")
                                s.setString(3, h.name)
                                s.executeUpdate()
                            }
                        }
                        player.popups.filter { p ->
                            !p.isDefault
                        }.forEach { p ->
                            prepareStatement("INSERT INTO enabled_hud(uuid, type, name) VALUES(?, ?, ?);").use { s ->
                                s.setString(1, uuid)
                                s.setString(2, "popup")
                                s.setString(3, p.name)
                                s.executeUpdate()
                            }
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

    override fun reload(resource: GlobalResource) {
        runCatching {
            current.close()
            val db = File(DATA_FOLDER, "database.yml").apply {
                if (!exists()) PLUGIN.saveResource("database.yml", false)
            }.toYaml()
            val type = db.getString("type").ifNull("type value not set.")
            val info = db.getConfigurationSection("info").ifNull("info configuration not set.")
            current = connectionMap[type].ifNull("this database doesn't exist: $type").connect(info)
        }.onFailure { e ->
            current = defaultConnector.connect(MemoryConfiguration())
            warn("Unable to connect the database.")
            warn("Reason: ${e.message}")
        }

    }

    override fun end() {
    }
}