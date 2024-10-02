package kr.toxicity.hud.bootstrap.fabric

import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.api.scheduler.HudTask
import kr.toxicity.hud.bootstrap.fabric.FabricScheduler.TaskType.*
import kr.toxicity.hud.util.removeIfSync
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class FabricScheduler: HudScheduler {

    private val serverTasks = ConcurrentLinkedQueue<SyncFabricTask>()
    private val worldTasks = ConcurrentHashMap<UUID, MutableCollection<SyncFabricTask>>()

    private val executors = Executors.newScheduledThreadPool(256)

    init {
        ServerTickEvents.START_SERVER_TICK.register {
            serverTasks.removeIfSync {
                it.run()
                it.isCancelled
            }
        }
        ServerTickEvents.START_WORLD_TICK.register {
            worldTasks.values.removeIfSync { list ->
                list.removeIfSync {
                    it.run()
                    it.isCancelled
                }
                list.isEmpty()
            }
        }
    }

    private class SyncFabricTask(
        private val type: TaskType,
        delay: Long,
        private val period: Long,
        private val run: Runnable
    ) : HudTask {

        private var i = -delay

        @Volatile
        private var cancel = false

        @Synchronized
        override fun cancel() {
            cancel = true
        }

        fun run() {
            run.run()
            if (++i >= period) {
                when (type) {
                    REMOVE -> synchronized(this) {
                        cancel = true
                    }
                    LOOP -> i = 0
                }
            }
        }

        @Synchronized
        override fun isCancelled(): Boolean = cancel
    }

    private enum class TaskType {
        REMOVE,
        LOOP
    }

    fun stopAll() {
        serverTasks.clear()
        worldTasks.clear()
        executors.close()
    }

    private class AsyncFabricTask(
        private val delegate: ScheduledFuture<*>
    ) : HudTask {
        override fun cancel() {
            delegate.cancel(true)
        }
        override fun isCancelled(): Boolean = delegate.isCancelled
    }


    @Synchronized
    override fun task(runnable: Runnable): HudTask {
        val task = SyncFabricTask(
            REMOVE,
            0,
            1,
            runnable
        )
        synchronized(serverTasks) {
            serverTasks.add(task)
        }
        return task
    }

    override fun task(location: LocationWrapper, runnable: Runnable): HudTask {
        val task = SyncFabricTask(
            REMOVE,
            0,
            1,
            runnable
        )
        val list = synchronized(worldTasks) {
            worldTasks.computeIfAbsent(location.world.uuid) {
                ConcurrentLinkedQueue()
            }
        }
        synchronized(list) {
            list.add(task)
        }
        return task
    }

    override fun taskLater(delay: Long, runnable: Runnable): HudTask {
        val task = SyncFabricTask(
            REMOVE,
            delay,
            1,
            runnable
        )
        synchronized(serverTasks) {
            serverTasks.add(task)
        }
        return task
    }

    override fun asyncTask(runnable: Runnable): HudTask {
        return AsyncFabricTask(
            executors.schedule(runnable, 1, TimeUnit.MILLISECONDS)
        )
    }

    override fun asyncTaskTimer(delay: Long, period: Long, runnable: Runnable): HudTask {
        if (delay < 0) throw RuntimeException("delay < 0")
        if (period < 1) throw RuntimeException("period < 1")
        return AsyncFabricTask(
            executors.scheduleAtFixedRate(runnable, delay * 50, period * 50, TimeUnit.MILLISECONDS)
        )
    }
}