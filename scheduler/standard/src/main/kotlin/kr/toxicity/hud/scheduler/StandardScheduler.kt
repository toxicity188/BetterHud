package kr.toxicity.hud.scheduler

import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.api.scheduler.HudTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin

class StandardScheduler: HudScheduler {
    override fun task(plugin: Plugin, runnable: Runnable): HudTask {
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

    override fun task(plugin: Plugin, location: Location, runnable: Runnable): HudTask = task(plugin, runnable)

    override fun taskLater(plugin: Plugin, delay: Long, runnable: Runnable): HudTask {
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

    override fun asyncTask(plugin: Plugin, runnable: Runnable): HudTask {
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

    override fun asyncTaskTimer(plugin: Plugin, delay: Long, period: Long, runnable: Runnable): HudTask {
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