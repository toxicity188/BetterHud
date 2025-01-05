package kr.toxicity.hud.scheduler

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
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

    private fun ScheduledTask.wrap() = object : HudTask {
        override fun isCancelled(): Boolean {
            return this@wrap.isCancelled
        }
        override fun cancel() {
            this@wrap.cancel()
        }
    }

    override fun task(runnable: Runnable): HudTask = Bukkit.getGlobalRegionScheduler().run(plugin) {
        runnable.run()
    }.wrap()
    override fun task(location: LocationWrapper, runnable: Runnable): HudTask = Bukkit.getRegionScheduler().run(plugin, Location(
        Bukkit.getWorld(location.world.uuid),
        location.x,
        location.y,
        location.z,
        location.pitch,
        location.yaw
    )) {
        runnable.run()
    }.wrap()

    override fun taskLater(delay: Long, runnable: Runnable): HudTask = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, {
        runnable.run()
    }, delay).wrap()

    override fun asyncTask(runnable: Runnable): HudTask = Bukkit.getAsyncScheduler().runNow(plugin) {
        runnable.run()
    }.wrap()

    override fun asyncTaskLater(delay: Long, runnable: Runnable): HudTask = Bukkit.getAsyncScheduler().runDelayed(plugin, {
        runnable.run()
    }, delay * 50, TimeUnit.MILLISECONDS).wrap()

    override fun asyncTaskTimer(delay: Long, period: Long, runnable: Runnable): HudTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
        runnable.run()
    }, delay * 50, period * 50, TimeUnit.MILLISECONDS).wrap()
}