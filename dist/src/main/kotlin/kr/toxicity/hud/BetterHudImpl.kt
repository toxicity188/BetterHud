package kr.toxicity.hud

import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.manager.ListenerManager
import kr.toxicity.hud.api.manager.PlaceholderManager
import kr.toxicity.hud.api.manager.PopupManager
import kr.toxicity.hud.api.manager.TriggerManager
import kr.toxicity.hud.api.nms.NMS
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.plugin.ReloadResult
import kr.toxicity.hud.api.plugin.ReloadState
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.info
import kr.toxicity.hud.util.task
import kr.toxicity.hud.util.warn
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.util.jar.JarFile

class BetterHudImpl: BetterHud() {
    private val managers = listOf(
        ConfigManager,
        CommandManager,
        PlayerManager,

        ListenerManagerImpl,
        PlaceholderManagerImpl,
        TriggerManagerImpl,

        ImageManager,
        TextManager,
        LayoutManager,
        HudManager,
        PopupManagerImpl,

        CompatibilityManager,

        ShaderManager
    )

    private lateinit var nms: NMS
    private lateinit var audience: BukkitAudiences

    override fun onEnable() {
        nms = when (val version = Bukkit.getServer().javaClass.`package`.name.split('.')[3]) {
            "v1_17_R1" -> kr.toxicity.hud.nms.v1_17_R1.NMSImpl()
            "v1_18_R1" -> kr.toxicity.hud.nms.v1_18_R1.NMSImpl()
            "v1_18_R2" -> kr.toxicity.hud.nms.v1_18_R2.NMSImpl()
            "v1_19_R1" -> kr.toxicity.hud.nms.v1_19_R1.NMSImpl()
            "v1_19_R2" -> kr.toxicity.hud.nms.v1_19_R2.NMSImpl()
            "v1_19_R3" -> kr.toxicity.hud.nms.v1_19_R3.NMSImpl()
            "v1_20_R1" -> kr.toxicity.hud.nms.v1_20_R1.NMSImpl()
            "v1_20_R2" -> kr.toxicity.hud.nms.v1_20_R2.NMSImpl()
            "v1_20_R3" -> kr.toxicity.hud.nms.v1_20_R3.NMSImpl()
            else -> {
                warn("Unsupported bukkit version: $version")
                Bukkit.getPluginManager().disablePlugin(this)
                return
            }
        }
        audience = BukkitAudiences.create(this)

        managers.forEach {
            it.start()
        }
        task {
            reload()
            info("Plugin enabled.")
        }
    }

    private var onReload = false

    override fun reload(): ReloadResult {
        if (onReload) return ReloadResult(ReloadState.STILL_ON_RELOAD, 0)
        onReload = true
        val time = System.currentTimeMillis()
        val resource = GlobalResource()
        managers.forEach {
            it.reload(resource)
        }
        onReload = false
        return ReloadResult(ReloadState.SUCCESS, System.currentTimeMillis() - time)
    }


    override fun onDisable() {
        managers.forEach {
            it.end()
        }
        info("Plugin disabled.")
    }

    override fun getNMS(): NMS = nms
    override fun getWidth(target: Char): Int = TextManager.getWidth(target)
    override fun getAudiences(): BukkitAudiences = audience
    override fun getHudPlayer(player: Player): HudPlayer = PlayerManager.getHudPlayer(player)

    override fun loadAssets(prefix: String, dir: File) {
        JarFile(file).use {
            it.entries().asSequence().sortedBy { dir ->
                dir.name.length
            }.forEach { entry ->
                if (!entry.name.startsWith(prefix)) return@forEach
                if (entry.name.length <= prefix.length + 1) return@forEach
                val name = entry.name.substring(prefix.length + 1)
                val file = File(dir, name)
                if (entry.isDirectory) {
                    if (!file.exists()) file.mkdir()
                } else {
                    getResource(entry.name)?.buffered()?.use { stream ->
                        if (!file.exists()) {
                            file.createNewFile()
                            file.outputStream().buffered().use { fos ->
                                stream.copyTo(fos)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getPlaceholderManager(): PlaceholderManager = PlaceholderManagerImpl
    override fun getListenerManager(): ListenerManager = ListenerManagerImpl
    override fun getPopupManager(): PopupManager = PopupManagerImpl
    override fun getTriggerManager(): TriggerManager = TriggerManagerImpl
    override fun isOnReload(): Boolean = onReload
}