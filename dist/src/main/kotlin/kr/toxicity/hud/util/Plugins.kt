package kr.toxicity.hud.util

import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.manager.ConfigManagerImpl

val PLUGIN
    get() = BetterHud.getInstance()

const val NAME_SPACE = BetterHud.DEFAULT_NAMESPACE
val NAME_SPACE_ENCODED
    get() = ConfigManagerImpl.key.encodedNamespace

val DATA_FOLDER
    get() = PLUGIN.dataFolder.apply {
        if (!exists()) mkdir()
    }

val VERSION = PLUGIN.nms.version

fun info(vararg message: String) {
    val logger = PLUGIN.logger
    synchronized(logger) {
        message.forEach {
            logger.info(it)
        }
    }
}
fun warn(vararg message: String) {
    val logger = PLUGIN.logger
    synchronized(logger) {
        message.forEach {
            logger.warning(it)
        }
    }
}

fun task(block: () -> Unit) = PLUGIN.scheduler.task(PLUGIN, block)
fun taskLater(delay: Long, block: () -> Unit) = PLUGIN.scheduler.taskLater(PLUGIN, delay, block)
fun asyncTask(block: () -> Unit) = PLUGIN.scheduler.asyncTask(PLUGIN, block)
fun asyncTaskTimer(delay: Long, period: Long, block: () -> Unit) = PLUGIN.scheduler.asyncTaskTimer(PLUGIN, delay, period, block)