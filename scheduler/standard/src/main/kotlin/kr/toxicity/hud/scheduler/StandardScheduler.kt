package kr.toxicity.hud.scheduler

import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.api.scheduler.HudTask
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class StandardScheduler(
    private val plugin: Plugin
): HudScheduler {
    override fun task(runnable: Runnable): HudTask {
        val task = Bukkit.getScheduler().runTask(plugin, runnable)
        return object : HudTask {
            override fun isCancelled(): Boolean {
                return task.isCancelled
            }
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun task(location: LocationWrapper, runnable: Runnable): HudTask = task(runnable)

    override fun taskLater(delay: Long, runnable: Runnable): HudTask {
        val task = Bukkit.getScheduler().runTaskLater(plugin, runnable, delay)
        return object : HudTask {
            override fun isCancelled(): Boolean {
                return task.isCancelled
            }
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun asyncTask(runnable: Runnable): HudTask {
        val task = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)
        return object : HudTask {
            override fun isCancelled(): Boolean {
                return task.isCancelled
            }
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun asyncTaskTimer(delay: Long, period: Long, runnable: Runnable): HudTask {
        val task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period)
        return object : HudTask {
            override fun isCancelled(): Boolean {
                return task.isCancelled
            }
            override fun cancel() {
                task.cancel()
            }
        }
    }
}