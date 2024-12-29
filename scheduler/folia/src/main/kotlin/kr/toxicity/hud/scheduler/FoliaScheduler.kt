package kr.toxicity.hud.scheduler

import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.api.scheduler.HudTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

class FoliaScheduler(
    private val plugin: Plugin
): HudScheduler {
    override fun task(runnable: Runnable): HudTask {
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
    override fun task(location: LocationWrapper, runnable: Runnable): HudTask {
        val task = Bukkit.getRegionScheduler().run(plugin, Location(
            Bukkit.getWorld(location.world.uuid),
            location.x,
            location.y,
            location.z,
            location.pitch,
            location.yaw
        )) {
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

    override fun taskLater(delay: Long, runnable: Runnable): HudTask {
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

    override fun asyncTask(runnable: Runnable): HudTask {
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

    override fun asyncTaskLater(delay: Long, runnable: Runnable): HudTask {
        val task = Bukkit.getAsyncScheduler().runDelayed(plugin, {
            runnable.run()
        }, delay * 50, TimeUnit.MILLISECONDS)
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