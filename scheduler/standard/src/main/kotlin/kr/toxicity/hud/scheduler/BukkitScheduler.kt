package kr.toxicity.hud.scheduler

import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.api.scheduler.HudTask
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

class BukkitScheduler(
    private val plugin: Plugin
) : HudScheduler {

    private fun BukkitTask.wrap() = object : HudTask {
        override fun isCancelled(): Boolean {
            return this@wrap.isCancelled
        }
        override fun cancel() {
            this@wrap.cancel()
        }
    }

    override fun task(runnable: Runnable): HudTask = Bukkit.getScheduler().runTask(plugin, runnable).wrap()
    override fun task(location: LocationWrapper, runnable: Runnable): HudTask = task(runnable)
    override fun taskLater(delay: Long, runnable: Runnable): HudTask = Bukkit.getScheduler().runTaskLater(plugin, runnable, delay).wrap()
    override fun asyncTask(runnable: Runnable): HudTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable).wrap()
    override fun asyncTaskLater(delay: Long, runnable: Runnable): HudTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay).wrap()
    override fun asyncTaskTimer(delay: Long, period: Long, runnable: Runnable): HudTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period).wrap()
}