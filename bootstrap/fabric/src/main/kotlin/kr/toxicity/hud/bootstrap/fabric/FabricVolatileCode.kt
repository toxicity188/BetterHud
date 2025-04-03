package kr.toxicity.hud.bootstrap.fabric

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.fabric.player.FabricCommonPacketListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler
import kr.toxicity.hud.bootstrap.fabric.util.toAdventure
import kr.toxicity.hud.bootstrap.fabric.util.toMinecraft
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtOps
import net.minecraft.network.Connection
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.BossEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class FabricVolatileCode : VolatileCodeHandler {
    companion object {
        private const val INJECT_NAME = BetterHud.DEFAULT_NAMESPACE
        private val bossBarMap = ConcurrentHashMap<UUID, PlayerBossBar>()

        @Suppress("UNCHECKED_CAST")
        private val operation = ClientboundBossEventPacket::class.java.declaredClasses.first {
            it.isEnum
        } as Class<out Enum<*>>

        private val operationEnum = operation.enumConstants

        fun createBossBar(byteBuf: RegistryFriendlyByteBuf): ClientboundBossEventPacket = ClientboundBossEventPacket.STREAM_CODEC.decode(byteBuf)

        private fun getColor(color: BossBar.Color) = when (color) {
            BossBar.Color.PINK -> BossEvent.BossBarColor.PINK
            BossBar.Color.BLUE -> BossEvent.BossBarColor.BLUE
            BossBar.Color.RED -> BossEvent.BossBarColor.RED
            BossBar.Color.GREEN -> BossEvent.BossBarColor.GREEN
            BossBar.Color.YELLOW -> BossEvent.BossBarColor.YELLOW
            BossBar.Color.PURPLE -> BossEvent.BossBarColor.PURPLE
            BossBar.Color.WHITE -> BossEvent.BossBarColor.WHITE
        }
    }

    override fun inject(player: HudPlayer, color: BossBar.Color) {
        val h = player.handle() as ServerPlayer
        bossBarMap.computeIfAbsent(h.uuid) {
            PlayerBossBar(h, h.connection, color, Component.empty())
        }
    }
    override fun showBossBar(player: HudPlayer, color: BossBar.Color, component: Component) {
        bossBarMap[player.uuid()]?.update(color, component)
    }

    override fun removeBossBar(player: HudPlayer) {
        bossBarMap.remove(player.uuid())
    }

    override fun getTextureValue(player: HudPlayer): String {
        val value = (player.handle() as ServerPlayer)
            .gameProfile
            .properties["textures"]
        return if (value.isNotEmpty()) value.first().value else ""
    }

    private class CachedHudBossbar(val hud: HudBossBar, val cacheUUID: UUID, val buf: HudByteBuf)
    private inner class PlayerBossBar(val player: ServerPlayer, val listener: ServerGamePacketListenerImpl, color: BossBar.Color, component: Component): ChannelDuplexHandler() {
        private inner class PlayerDummyBossBar(color: BossBar.Color) {
            val line = BetterHudAPI.inst().configManager.bossbarLine - 1
            val dummyBars = (0..<line).map {
                HudBossBar(UUID.randomUUID(), Component.empty(), color).apply {
                    listener.send(ClientboundBossEventPacket.createAddPacket(this))
                }
            }
            val dummyBarsUUID = dummyBars.map {
                it.uuid
            }
        }
        private var dummy = PlayerDummyBossBar(color)
        private val dummyBarHandleMap = Collections.synchronizedMap(LinkedHashMap<UUID, CachedHudBossbar>())
        private val otherBarCache = ConcurrentLinkedQueue<Pair<UUID, HudByteBuf>>()
        private val uuid = UUID.randomUUID().apply {
            listener.send(ClientboundBossEventPacket.createAddPacket(HudBossBar(this, component, color)))
        }

        private var last: HudBossBar = HudBossBar(uuid, Component.empty(), color)
        private var onUse = uuid to HudByteBuf(Unpooled.buffer())

        init {
            val pipeLine = (listener as FabricCommonPacketListener).`betterHud$channel`().pipeline()
            pipeLine.toMap().forEach {
                if (it.value is Connection) pipeLine.addBefore(it.key, INJECT_NAME, this)
            }
        }

        fun update(color: BossBar.Color, component: Component) {
            val bossBar = HudBossBar(uuid, component, color)
            last = bossBar
            listener.send(ClientboundBossEventPacket.createUpdateNamePacket(bossBar))
        }

        private fun writeBossBar(buf: HudByteBuf, ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
            val originalUUID = buf.readUUID()
            if (originalUUID == uuid || dummy.dummyBarsUUID.contains(originalUUID)) {
                super.write(ctx, msg, promise)
                return
            }
            if (BetterHudAPI.inst().isOnReload) return
            val enum = buf.readEnum(operation)

            fun getBuf(targetUUID: UUID = uuid) = HudByteBuf(Unpooled.buffer(1 shl 4))
                .writeUUID(targetUUID)

            fun sendProgress(getBuf: HudByteBuf = getBuf(), targetBuf: HudByteBuf = buf) = listener.send(createBossBar(getBuf
                .writeEnum(operationEnum[2])
                .writeFloat(targetBuf.readFloat())
            ))
            fun sendName(getBuf: HudByteBuf = getBuf(), targetBuf: HudByteBuf = buf) = listener.send(createBossBar(getBuf
                .writeEnum(operationEnum[3])
                .writeComponent(targetBuf.readComponentTrusted())
            ))
            fun sendStyle(getBuf: HudByteBuf = getBuf(), targetBuf: HudByteBuf = buf) = listener.send(createBossBar(getBuf
                .writeEnum(operationEnum[4])
                .writeEnum(targetBuf.readEnum(BossEvent.BossBarColor::class.java))
                .writeEnum(targetBuf.readEnum(BossEvent.BossBarOverlay::class.java)))
            )
            fun sendProperties(getBuf: HudByteBuf = getBuf(), targetBuf: HudByteBuf = buf) = listener.send(createBossBar(getBuf
                .writeEnum(operationEnum[5])
                .writeByte(targetBuf.readUnsignedByte().toInt())
            ))
            fun changeName(targetBuf: HudByteBuf = buf) {
                runCatching {
                    val hud = BetterHudAPI.inst().playerManager.getHudPlayer(player.uuid) ?: return
                    val comp = targetBuf.readComponentTrusted().toAdventure()
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
                            is TranslatableComponent -> BetterHudAPI.inst().translate(player.clientInformation().language, component.key())
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
                if (onUse.first == uuid) return false
                var result = false
                if (changeCache) {
                    val cacheSize = dummyBarHandleMap.size
                    if (cacheSize < dummy.line) {
                        val cache = CachedHudBossbar(dummy.dummyBars[cacheSize], onUse.first, HudByteBuf(onUse.second.unwrap()))
                        dummyBarHandleMap[onUse.first] = cache
                        sendName(getBuf = getBuf(cache.hud.uuid), targetBuf = onUse.second)
                        sendProgress(getBuf = getBuf(cache.hud.uuid), targetBuf = onUse.second)
                        sendStyle(getBuf = getBuf(cache.hud.uuid), targetBuf = onUse.second)
                        sendProperties(getBuf = getBuf(cache.hud.uuid), targetBuf = onUse.second)
                        result = true
                    }
                }
                otherBarCache.poll()?.let { target ->
                    val targetBuf = HudByteBuf(Unpooled.copiedBuffer(target.second.unwrap()))
                    listener.send(ClientboundBossEventPacket.createRemovePacket(target.first))
                    changeName(targetBuf = targetBuf)
                    sendProgress(targetBuf = targetBuf)
                    sendStyle(targetBuf = targetBuf)
                    sendProperties(targetBuf = targetBuf)
                    onUse = target
                } ?: run {
                    onUse = uuid to HudByteBuf(buf.unwrap())
                    BetterHudAPI.inst().playerManager.getHudPlayer(player.uuid)?.additionalComponent = null
                    listener.send(ClientboundBossEventPacket.createUpdateNamePacket(last))
                    listener.send(ClientboundBossEventPacket.createUpdateProgressPacket(last))
                    listener.send(ClientboundBossEventPacket.createUpdateStylePacket(last))
                    listener.send(ClientboundBossEventPacket.createUpdatePropertiesPacket(last))
                }
                return result
            }

            runCatching {
                val cacheSize = dummyBarHandleMap.size
                if (cacheSize < dummy.line && enum.ordinal == 0) {
                    val hud = dummyBarHandleMap.computeIfAbsent(originalUUID) {
                        CachedHudBossbar(dummy.dummyBars[cacheSize], originalUUID, HudByteBuf(buf.unwrap()))
                    }
                    sendName(getBuf = getBuf(hud.hud.uuid))
                    sendProgress(getBuf = getBuf(hud.hud.uuid))
                    sendStyle(getBuf = getBuf(hud.hud.uuid))
                    sendProperties(getBuf = getBuf(hud.hud.uuid))
                    return
                } else {
                    dummyBarHandleMap[originalUUID]?.let {
                        when (enum.ordinal) {
                            0 -> {
                                sendName(getBuf = getBuf(it.hud.uuid))
                                sendProgress(getBuf = getBuf(it.hud.uuid))
                                sendStyle(getBuf = getBuf(it.hud.uuid))
                                sendProperties(getBuf = getBuf(it.hud.uuid))
                            }
                            1 -> {
                                dummyBarHandleMap.remove(originalUUID)
                                val swap = removeBossbar(changeCache = true)
                                val list = dummyBarHandleMap.entries.toList()
                                val last = if (list.isNotEmpty()) list.last().value else it
                                list.forEachIndexed { index, target ->
                                    val after = target.value
                                    val targetBuf = after.buf
                                    val newCache = CachedHudBossbar(dummy.dummyBars[index], after.cacheUUID, HudByteBuf(targetBuf.unwrap()))
                                    target.setValue(newCache)
                                    sendName(getBuf = getBuf(newCache.hud.uuid), targetBuf = targetBuf)
                                    sendProgress(getBuf = getBuf(newCache.hud.uuid), targetBuf = targetBuf)
                                    sendStyle(getBuf = getBuf(newCache.hud.uuid), targetBuf = targetBuf)
                                    sendProperties(getBuf = getBuf(newCache.hud.uuid), targetBuf = targetBuf)
                                }
                                if (!swap) {
                                    listener.send(ClientboundBossEventPacket.createUpdateNamePacket(last.hud))
                                    listener.send(ClientboundBossEventPacket.createUpdateProgressPacket(last.hud))
                                    listener.send(ClientboundBossEventPacket.createUpdateStylePacket(last.hud))
                                    listener.send(ClientboundBossEventPacket.createUpdatePropertiesPacket(last.hud))
                                }
                            }
                            2 -> sendProgress(getBuf = getBuf(it.hud.uuid))
                            3 -> sendName(getBuf = getBuf(it.hud.uuid))
                            4 -> sendStyle(getBuf = getBuf(it.hud.uuid))
                            5 -> sendProperties(getBuf = getBuf(it.hud.uuid))
                            else -> {}
                        }
                        return
                    }
                }
                if (otherBarCache.isEmpty() && enum.ordinal == 0 && onUse.first == uuid) {
                    onUse = originalUUID to HudByteBuf(buf.unwrap())
                    changeName()
                    sendProgress()
                    sendStyle()
                    sendProperties()
                    return
                }
                if (originalUUID == onUse.first) {
                    when (enum.ordinal) {
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
                    when (enum.ordinal) {
                        0 -> {
                            otherBarCache.removeIf {
                                it.first == originalUUID
                            }
                            otherBarCache.add(originalUUID to HudByteBuf(buf.unwrap()))
                        }
                        1 -> otherBarCache.removeIf {
                            it.first == originalUUID
                        }
                    }
                    super.write(ctx, msg, promise)
                }
            }.onFailure {
                it.printStackTrace()
            }
        }

        override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
            if (msg is ClientboundBossEventPacket) {

                if (BetterHudAPI.inst().isMergeBossBar) {
                    val buf = HudByteBuf(Unpooled.buffer(1 shl 4)).apply {
                        ClientboundBossEventPacket.STREAM_CODEC.encode(this, msg)
                    }
                    writeBossBar(buf, ctx, msg, promise)
                } else super.write(ctx, msg, promise)
            } else {
                super.write(ctx, msg, promise)
            }
        }
    }
    private class HudByteBuf(private val source: ByteBuf) : RegistryFriendlyByteBuf(source, RegistryAccess.EMPTY) {
        override fun unwrap(): ByteBuf {
            return Unpooled.copiedBuffer(source)
        }
        override fun writeEnum(instance: Enum<*>): HudByteBuf {
            super.writeEnum(instance)
            return this
        }
        override fun writeUUID(uuid: UUID): HudByteBuf {
            super.writeUUID(uuid)
            return this
        }
        override fun writeFloat(f: Float): HudByteBuf {
            super.writeFloat(f)
            return this
        }
        override fun writeByte(i: Int): HudByteBuf {
            super.writeByte(i)
            return this
        }
        fun readComponentTrusted(): net.minecraft.network.chat.Component {
            return ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, readNbt(NbtAccounter.unlimitedHeap())).orThrow
        }
        fun writeComponent(component: net.minecraft.network.chat.Component): HudByteBuf {
            writeNbt(ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, component).orThrow)
            return this
        }
    }

    private class HudBossBar(val uuid: UUID, component: net.minecraft.network.chat.Component, color: BossBarColor) : BossEvent(uuid, component, color, BossBarOverlay.PROGRESS) {
        constructor(uuid: UUID, component: Component, color: BossBar.Color): this(
            uuid,
            component.toMinecraft(),
            getColor(color)
        )
        override fun getProgress(): Float {
            return 0F
        }
    }
}