package kr.toxicity.hud.util

import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.manager.ConfigManager
import kr.toxicity.hud.api.manager.ConfigManager.DebugLevel
import kr.toxicity.hud.manager.ConfigManagerImpl
import net.kyori.adventure.text.Component
import java.text.DecimalFormat

private val COMMA_FORMAT = DecimalFormat("#,###")
fun Number.withDecimal() = Component.text(COMMA_FORMAT.format(this))

val PLUGIN
    get() = BetterHudAPI.inst()

val BOOTSTRAP
    get() = PLUGIN.bootstrap()

const val NAME_SPACE = BetterHud.DEFAULT_NAMESPACE
val NAME_SPACE_ENCODED
    get() = ConfigManagerImpl.key.namespace

val DATA_FOLDER
    get() = BOOTSTRAP.dataFolder().apply {
        if (!exists()) mkdir()
    }

val VOLATILE_CODE = BOOTSTRAP.volatileCode()

fun info(vararg message: String) = BOOTSTRAP.logger().info(*message)
fun warn(vararg message: String) = BOOTSTRAP.logger().warn(*message)
fun debug(level: DebugLevel, vararg message: String) {
    if (ConfigManager.checkAvailable(level)) BOOTSTRAP.logger().info(*message)
}
fun task(block: () -> Unit) = BOOTSTRAP.scheduler().task(block)
fun task(location: LocationWrapper, block: () -> Unit) = BOOTSTRAP.scheduler().task(location, block)
fun taskLater(delay: Long, block: () -> Unit) = BOOTSTRAP.scheduler().taskLater(delay, block)
fun asyncTask(block: () -> Unit) = BOOTSTRAP.scheduler().asyncTask(block)
fun asyncTaskLater(delay: Long, block: () -> Unit) = BOOTSTRAP.scheduler().asyncTaskLater(delay, block)
fun asyncTaskTimer(delay: Long, period: Long, block: () -> Unit) = BOOTSTRAP.scheduler().asyncTaskTimer(delay, period, block)