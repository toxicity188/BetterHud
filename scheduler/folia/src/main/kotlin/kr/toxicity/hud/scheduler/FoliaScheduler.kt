package kr.toxicity.hud.scheduler

import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.api.scheduler.HudTask
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

class FoliaScheduler: HudScheduler {
    override fun task(plugin: Plugin, runnable: Runnable): HudTask {
        val task = Bukkit.getGlobalRegionScheduler().run(plugin) {
            runnable.run()
        }
        return object : HudTask {
            override fun isCancelled(): Boolean {
                return task.isCancelled
            }
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun taskLater(plugin: Plugin, delay: Long, runnable: Runnable): HudTask {
        val task = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, {
            runnable.run()
        }, delay)
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
        val task = Bukkit.getAsyncScheduler().runNow(plugin) {
            runnable.run()
        }
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
        val task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
            runnable.run()
        }, delay * 50, period * 50, TimeUnit.MILLISECONDS)
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