package kr.toxicity.hud.util

import kr.toxicity.hud.api.BetterHud
import org.bukkit.Bukkit

val PLUGIN
    get() = BetterHud.getInstance()

const val NAME_SPACE = BetterHud.DEFAULT_NAMESPACE
val DATA_FOLDER
    get() = PLUGIN.dataFolder.apply {
        if (!exists()) mkdir()
    }

val VERSION = PLUGIN.nms.version

fun info(message: String) = PLUGIN.logger.info(message)
fun warn(message: String) = PLUGIN.logger.warning(message)

fun task(block: () -> Unit) = PLUGIN.scheduler.task(PLUGIN, block)
fun asyncTask(block: () -> Unit) = PLUGIN.scheduler.asyncTask(PLUGIN, block)
fun asyncTaskTimer(delay: Long, period: Long, block: () -> Unit) = PLUGIN.scheduler.asyncTaskTimer(PLUGIN, delay, period, block)