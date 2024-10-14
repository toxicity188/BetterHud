package kr.toxicity.hud.bootstrap.velocity

import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import com.velocitypowered.proxy.protocol.packet.BossBarPacket
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler
import kr.toxicity.hud.manager.ConfigManagerImpl
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Overlay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.TextDecoration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class VelocityVolatileCodeHandler : VolatileCodeHandler {
    companion object {
        private const val INJECT_NAME = BetterHud.DEFAULT_NAMESPACE
        private val bossBarMap = ConcurrentHashMap<UUID, PlayerBossBar>()
    }

    override fun inject(player: HudPlayer, color: BossBar.Color) {
        val h = player.handle() as ConnectedPlayer
        bossBarMap.computeIfAbsent(h.uniqueId) {
            PlayerBossBar(h, h.connection, color)
        }
    }
    override fun showBossBar(player: HudPlayer, color: BossBar.Color, component: Component) {
        bossBarMap[player.uuid()]?.update(color, component)
    }

    override fun removeBossBar(player: HudPlayer) {
        bossBarMap.remove(player.uuid())?.remove()
    }

    override fun reloadBossBar(player: HudPlayer, color: BossBar.Color) {
        bossBarMap[player.uuid()]?.resetDummy(color)
    }

    override fun getTextureValue(player: HudPlayer): String {
        return (player.handle() as ConnectedPlayer).gameProfile.properties.first {
            it.name == "textures"
        }.value
    }


    private class PlayerBossBar(val player: Player, val listener: MinecraftConnection, var originalColor: BossBar.Color): ChannelDuplexHandler() {
        private inner class PlayerDummyBossBar(color: BossBar.Color) {
            val line = ConfigManagerImpl.bossbarLine - 1
            val dummyBars = (0..<line).map {
                HudBossBar(UUID.randomUUID(), color, listener.protocolVersion).apply {
                    listener.write(createAddPacket())
                }
            }
            val dummyBarsUUID = dummyBars.map {
                it.uuid
            }
        }
        private var dummy = PlayerDummyBossBar(originalColor)
        private val dummyBarHandleMap = Collections.synchronizedMap(LinkedHashMap<UUID, HudBossBar>())
        private val otherBarCache = ConcurrentLinkedQueue<HudBossBar>()
        private val uuid = UUID.randomUUID()
        private val uuidHud = HudBossBar(uuid, originalColor, listener.protocolVersion).apply {
            listener.write(createAddPacket())
        }

        private var last: HudBossBar = uuidHud
        private var onUse = uuidHud

        init {
            val pipeLine = listener.channel.pipeline()
            pipeLine.toMap().forEach {
                if (it.value is MinecraftConnection) pipeLine.addBefore(it.key, INJECT_NAME, this)
            }
        }

        fun update(color: BossBar.Color, component: Component) {
            val bossBar = HudBossBar(uuid, color, listener.protocolVersion)
            last = bossBar
            listener.write(bossBar.createUpdateNamePacket(component))
        }
        
        fun resetDummy(color: BossBar.Color) {
            listener.write(uuidHud.createRemovePacket())
            dummy.dummyBars.forEach {
                listener.write(it.createRemovePacket())
            }
            dummy = PlayerDummyBossBar(color)
            dummy.dummyBars.forEach { 
                listener.write(it.createAddPacket())
            }
            listener.write(last.createAddPacket())
            originalColor = color
        }

        fun remove() {
            val channel = listener.channel
            channel.eventLoop().submit {
                channel.pipeline().remove(INJECT_NAME)
            }
            listener.write(uuidHud.createRemovePacket())
            dummy.dummyBars.forEach {
                listener.write(it.createRemovePacket())
            }
        }

        private fun writeBossBar(ctx: ChannelHandlerContext?, buf: BossBarPacket, promise: ChannelPromise?) {
            val originalUUID = buf.uuid
            if (originalUUID == uuid || dummy.dummyBarsUUID.contains(originalUUID)) {
                super.write(ctx, buf, promise)
                return
            }
            if (BetterHudAPI.inst().isOnReload) return
            val enum = buf.action

            val bossBar = HudBossBar(originalUUID, BossBar.Color.entries[buf.color], listener.protocolVersion)
            bossBar.apply(buf)

            fun getBuf(targetUUID: UUID = uuid) = HudBossBar(targetUUID, originalColor, listener.protocolVersion)

            fun sendProgress(getBuf: HudBossBar = getBuf(), targetBuf: HudBossBar = bossBar) = listener.write(getBuf.createProgressPacket(targetBuf.bossBar.progress()))
            fun sendName(getBuf: HudBossBar = getBuf(), targetBuf: HudBossBar = bossBar) = listener.write(getBuf.createUpdateNamePacket(targetBuf.bossBar.name()))
            fun sendStyle(getBuf: HudBossBar = getBuf(), targetBuf: HudBossBar = bossBar) = listener.write(getBuf.createUpdateStylePacket(targetBuf.bossBar.color(), targetBuf.bossBar.overlay()))
            fun sendProperties(getBuf: HudBossBar = getBuf(), targetBuf: HudBossBar = bossBar) = listener.write(getBuf.createUpdatePropertiesPacket(targetBuf.bossBar.flags()))
            fun changeName(targetBuf: HudBossBar = bossBar) {
                runCatching {
                    val hud = BetterHudAPI.inst().getPlayerManager().getHudPlayer(player.uniqueId) ?: return
                    val comp = targetBuf.bossBar.name()
                    val key = BetterHudAPI.inst().defaultKey
                    fun applyFont(component: Component): Component {
                        return component.font(key).children(component.children().map {
                            applyFont(it)
                        })
                    }
                    fun hasDecoration(parent: Boolean, state: TextDecoration.State) = when (state) {
                        TextDecoration.State.TRUE -> true
                        TextDecoration.State.NOT_SET -> parent
                        TextDecoration.State.FALSE -> false
                    }
                    fun getWidth(component: Component, bold: Boolean, italic: Boolean): Int {
                        var i = 0
                        if (bold) i++
                        if (italic) i++
                        return component.children().sumOf {
                            getWidth(
                                it,
                                hasDecoration(bold, it.decoration(TextDecoration.BOLD)),
                                hasDecoration(italic, it.decoration(TextDecoration.ITALIC))
                            )
                        } + (when (component) {
                            is TextComponent -> component.content()
                            is TranslatableComponent -> BetterHudAPI.inst().translate((player.effectiveLocale ?: Locale.US).toString(), component.key())
                            else -> null
                        }?.codePoints()?.map {
                            (if (it == ' '.code) 4 else BetterHudAPI.inst().getWidth(it) + 1) + i
                        }?.sum() ?: 0)
                    }
                    hud.additionalComponent = WidthComponent(Component.text().append(applyFont(comp)), getWidth(
                        comp,
                        hasDecoration(false, comp.decoration(TextDecoration.BOLD)),
                        hasDecoration(false, comp.decoration(TextDecoration.ITALIC))
                    ))
                }
            }
            fun removeBossbar(changeCache: Boolean = false): Boolean {
                if (onUse.uuid == uuid) return false
                var result = false
                if (changeCache) {
                    val cacheSize = dummyBarHandleMap.size
                    if (cacheSize < dummy.line) {
                        dummyBarHandleMap[onUse.uuid] = onUse
                        sendName(getBuf = getBuf(onUse.uuid), targetBuf = onUse)
                        sendProgress(getBuf = getBuf(onUse.uuid), targetBuf = onUse)
                        sendStyle(getBuf = getBuf(onUse.uuid), targetBuf = onUse)
                        sendProperties(getBuf = getBuf(onUse.uuid), targetBuf = onUse)
                        result = true
                    }
                }
                otherBarCache.poll()?.let { target ->
                    listener.write(target.createRemovePacket())
                    changeName(targetBuf = target)
                    sendProgress(targetBuf = target)
                    sendStyle(targetBuf = target)
                    sendProperties(targetBuf = target)
                    onUse = target
                } ?: run {
                    onUse = uuidHud
                    BetterHudAPI.inst().getPlayerManager().getHudPlayer(player.uniqueId)?.additionalComponent = null
                    listener.write(last.createUpdateNamePacket())
                    listener.write(last.createProgressPacket())
                    listener.write(last.createUpdateStylePacket())
                    listener.write(last.createUpdatePropertiesPacket())
                }
                return result
            }

            runCatching {
                val cacheSize = dummyBarHandleMap.size
                if (cacheSize < dummy.line && enum == 0) {
                    val hud = dummyBarHandleMap.computeIfAbsent(originalUUID) {
                        bossBar
                    }
                    sendName(getBuf = getBuf(hud.uuid))
                    sendProgress(getBuf = getBuf(hud.uuid))
                    sendStyle(getBuf = getBuf(hud.uuid))
                    sendProperties(getBuf = getBuf(hud.uuid))
                    return
                } else {
                    dummyBarHandleMap[originalUUID]?.let {
                        when (enum) {
                            0 -> {
                                sendName(getBuf = getBuf(it.uuid))
                                sendProgress(getBuf = getBuf(it.uuid))
                                sendStyle(getBuf = getBuf(it.uuid))
                                sendProperties(getBuf = getBuf(it.uuid))
                            }
                            1 -> {
                                dummyBarHandleMap.remove(originalUUID)
                                val swap = removeBossbar(changeCache = true)
                                val list = dummyBarHandleMap.entries.toList()
                                val last = if (list.isNotEmpty()) list.last().value else it
                                list.forEach { target ->
                                    val after = target.value
                                    target.setValue(bossBar)
                                    sendName(getBuf = getBuf(bossBar.uuid), targetBuf = after)
                                    sendProgress(getBuf = getBuf(bossBar.uuid), targetBuf = after)
                                    sendStyle(getBuf = getBuf(bossBar.uuid), targetBuf = after)
                                    sendProperties(getBuf = getBuf(bossBar.uuid), targetBuf = after)
                                }
                                if (!swap) {
                                    listener.write(last.createUpdateNamePacket())
                                    listener.write(last.createProgressPacket())
                                    listener.write(last.createUpdateStylePacket())
                                    listener.write(last.createUpdatePropertiesPacket())
                                }
                            }
                            2 -> sendProgress(getBuf = getBuf(it.uuid))
                            3 -> sendName(getBuf = getBuf(it.uuid))
                            4 -> sendStyle(getBuf = getBuf(it.uuid))
                            5 -> sendProperties(getBuf = getBuf(it.uuid))
                            else -> {}
                        }
                        return
                    }
                }
                if (otherBarCache.isEmpty() && enum == 0 && onUse.uuid == uuid) {
                    onUse = bossBar
                    changeName()
                    sendProgress()
                    sendStyle()
                    sendProperties()
                    return
                }
                if (originalUUID == onUse.uuid) {
                    when (enum) {
                        0 -> {
                            changeName()
                            sendProgress()
                            sendStyle()
                            sendProperties()
                        }
                        1 -> removeBossbar()
                        2 -> sendProgress()
                        3 -> changeName()
                        4 -> sendStyle()
                        5 -> sendProperties()
                        else -> {}
                    }
                } else {
                    when (enum) {
                        0 -> {
                            otherBarCache.removeIf {
                                it.uuid == originalUUID
                            }
                            otherBarCache.add(bossBar)
                        }
                        1 -> otherBarCache.removeIf {
                            it.uuid == originalUUID
                        }
                    }
                    super.write(ctx, buf, promise)
                }
            }.onFailure {
                it.printStackTrace()
            }
        }

        override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
            if (msg is BossBarPacket) {
                if (BetterHudAPI.inst().isMergeBossBar) {
                    writeBossBar(ctx, msg, promise)
                } else super.write(ctx, msg, promise)
            } else {
                super.write(ctx, msg, promise)
            }
        }
    }

    private class HudBossBar(val uuid: UUID, color: BossBar.Color, private val version: ProtocolVersion) {
        var bossBar = BossBar.bossBar(
            Component.empty(),
            0F,
            color,
            Overlay.PROGRESS
        )
            private set

        fun apply(packet: BossBarPacket) {
            val f = packet.flags.toInt()
            val set = EnumSet.noneOf(BossBar.Flag::class.java)
            if (f and 1 == 1) set.add(BossBar.Flag.DARKEN_SCREEN)
            if ((f shr 1) and 1 == 1) set.add(BossBar.Flag.PLAY_BOSS_MUSIC)
            if ((f shr 2) and 1 == 1) set.add(BossBar.Flag.CREATE_WORLD_FOG)
            bossBar = bossBar
                .name(packet.name?.component ?: Component.empty())
                .color(BossBar.Color.entries[packet.color])
                .progress(packet.percent)
                .overlay(BossBar.Overlay.entries[packet.overlay])
                .flags(set)
        }
        fun createUpdateNamePacket(): BossBarPacket {
            return BossBarPacket.createUpdateNamePacket(uuid, bossBar, ComponentHolder(version, bossBar.name()))
        }
        fun createProgressPacket(): BossBarPacket {
            return BossBarPacket.createUpdateProgressPacket(uuid, bossBar)
        }
        fun createUpdateStylePacket(): BossBarPacket {
            return BossBarPacket.createUpdateStylePacket(uuid, bossBar)
        }
        fun createUpdatePropertiesPacket(): BossBarPacket {
            return BossBarPacket.createUpdatePropertiesPacket(uuid, bossBar)
        }

        fun createAddPacket(): BossBarPacket = BossBarPacket.createAddPacket(uuid, bossBar, ComponentHolder(version, bossBar.name()))
        fun createRemovePacket(): BossBarPacket = BossBarPacket.createRemovePacket(uuid, bossBar)
        fun createUpdateNamePacket(component: Component): BossBarPacket {
            bossBar = bossBar.name(component)
            return BossBarPacket.createUpdateNamePacket(uuid, bossBar, ComponentHolder(version, bossBar.name()))
        }
        fun createProgressPacket(progress: Float): BossBarPacket {
            bossBar = bossBar.progress(progress)
            return BossBarPacket.createUpdateProgressPacket(uuid, bossBar)
        }
        fun createUpdateStylePacket(color: BossBar.Color, overlay: Overlay): BossBarPacket {
            bossBar = bossBar
                .color(color)
                .overlay(overlay)
            return BossBarPacket.createUpdateStylePacket(uuid, bossBar)
        }
        fun createUpdatePropertiesPacket(set: Set<BossBar.Flag>): BossBarPacket {
            bossBar = bossBar.flags(set)
            return BossBarPacket.createUpdatePropertiesPacket(uuid, bossBar)
        }
    }
}