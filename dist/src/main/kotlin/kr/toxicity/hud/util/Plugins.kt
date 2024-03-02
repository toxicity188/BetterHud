package kr.toxicity.hud.util

import kr.toxicity.hud.api.MythicHud
import org.bukkit.Bukkit

val PLUGIN
    get() = MythicHud.getInstance()

val DATA_FOLDER
    get() = PLUGIN.dataFolder.apply {
        if (!exists()) mkdir()
    }

val VERSION = PLUGIN.nms.version

fun info(message: String) = PLUGIN.logger.info(message)
fun warn(message: String) = PLUGIN.logger.warning(message)

fun task(block: () -> Unit) = Bukkit.getScheduler().runTask(PLUGIN, block)
fun asyncTask(block: () -> Unit) = Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, block)
fun taskLater(delay: Long, block: () -> Unit) = Bukkit.getScheduler().runTaskLater(PLUGIN, block, delay)
fun asyncTaskLater(delay: Long, block: () -> Unit) = Bukkit.getScheduler().runTaskLaterAsynchronously(PLUGIN, block, delay)
fun taskTimer(delay: Long, period: Long, block: () -> Unit) = Bukkit.getScheduler().runTaskTimer(PLUGIN, block, delay, period)
fun asyncTaskTimer(delay: Long, period: Long, block: () -> Unit) = Bukkit.getScheduler().runTaskTimerAsynchronously(PLUGIN, block, delay, period)