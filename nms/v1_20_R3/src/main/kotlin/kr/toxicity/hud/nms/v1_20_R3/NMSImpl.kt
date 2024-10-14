package kr.toxicity.hud.nms.v1_20_R3

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.bukkit.nms.NMS
import kr.toxicity.hud.api.bukkit.nms.NMSVersion
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.pointer.Pointers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.BossEvent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.WorldBorder
import org.bukkit.craftbukkit.v1_20_R3.CraftServer
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataContainer
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory
import org.bukkit.permissions.Permission
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class NMSImpl : NMS {
    companion object {
        private const val INJECT_NAME = BetterHud.DEFAULT_NAMESPACE
        private val bossBarMap = ConcurrentHashMap<UUID, HudPlayerBossBar>()

        @Suppress("UNCHECKED_CAST")
        private val operation = ClientboundBossEventPacket::class.java.declaredClasses.first {
            it.isEnum
        } as Class<out Enum<*>>

        private val operationEnum = operation.enumConstants
        private val getConnection: (ServerCommonPacketListenerImpl) -> Connection = if (BetterHudAPI.inst().bootstrap().isPaper) {
            {
               it.connection
            }
        } else {
            ServerCommonPacketListenerImpl::class.java.declaredFields.first { f ->
                f.type == Connection::class.java
            }.apply {
                isAccessible = true
            }.let { get ->
                {
                    get[it] as Connection
                }
            }
        }

        private fun toAdventure(component: net.minecraft.network.chat.Component) = GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(component))
        private fun fromAdventure(component: Component) = CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component))
        private fun getColor(color: BossBar.Color) =  when (color) {
            BossBar.Color.PINK -> BossEvent.BossBarColor.PINK
            BossBar.Color.BLUE -> BossEvent.BossBarColor.BLUE
            BossBar.Color.RED -> BossEvent.BossBarColor.RED
            BossBar.Color.GREEN -> BossEvent.BossBarColor.GREEN
            BossBar.Color.YELLOW -> BossEvent.BossBarColor.YELLOW
            BossBar.Color.PURPLE -> BossEvent.BossBarColor.PURPLE
            BossBar.Color.WHITE -> BossEvent.BossBarColor.WHITE
        }
    }

    override fun inject(hudPlayer: HudPlayer, color: BossBar.Color) {
        val h = hudPlayer.handle() as CraftPlayer
        bossBarMap.computeIfAbsent(h.uniqueId) {
            HudPlayerBossBar(h, h.handle.connection, color, Component.empty())
        }
    }
    override fun showBossBar(hudPlayer: HudPlayer, color: BossBar.Color, component: Component) {
        bossBarMap[hudPlayer.uuid()]?.update(color, component)
    }

    override fun removeBossBar(hudPlayer: HudPlayer) {
        bossBarMap.remove(hudPlayer.uuid())?.remove()
    }

    override fun reloadBossBar(hudPlayer: HudPlayer, color: BossBar.Color) {
        bossBarMap[hudPlayer.uuid()]?.resetDummy(color)
    }

    override fun getVersion(): NMSVersion {
        return NMSVersion.V1_20_R3
    }

    override fun getTextureValue(player: HudPlayer): String {
        return (player.handle() as CraftPlayer).handle.gameProfile.properties.get("textures").first().value
    }

    override fun getFoliaAdaptedPlayer(hudPlayer: Player): Player {
        val handle = (hudPlayer as CraftPlayer).handle
        return object : CraftPlayer(Bukkit.getServer() as CraftServer, handle) {
            override fun getPersistentDataContainer(): CraftPersistentDataContainer {
                return hudPlayer.persistentDataContainer
            }
            override fun getHandle(): ServerPlayer {
                return handle
            }
            override fun getHealth(): Double {
                return hudPlayer.health
            }
            override fun getScaledHealth(): Float {
                return hudPlayer.scaledHealth
            }
            override fun getFirstPlayed(): Long {
                return hudPlayer.firstPlayed
            }
            override fun getInventory(): PlayerInventory {
                return hudPlayer.inventory
            }
            override fun getEnderChest(): Inventory {
                return hudPlayer.enderChest
            }
            override fun isOp(): Boolean {
                return hudPlayer.isOp
            }
            override fun getGameMode(): GameMode {
                return hudPlayer.gameMode
            }
            override fun getEquipment(): EntityEquipment {
                return hudPlayer.equipment
            }
            override fun hasPermission(name: String): Boolean {
                return hudPlayer.hasPermission(name)
            }
            override fun hasPermission(perm: Permission): Boolean {
                return hudPlayer.hasPermission(perm)
            }
            override fun isPermissionSet(name: String): Boolean {
                return hudPlayer.isPermissionSet(name)
            }
            override fun isPermissionSet(perm: Permission): Boolean {
                return hudPlayer.isPermissionSet(perm)
            }
            override fun hasPlayedBefore(): Boolean {
                return hudPlayer.hasPlayedBefore()
            }
            override fun getWorldBorder(): WorldBorder? {
                return hudPlayer.getWorldBorder()
            }
            override fun showBossBar(bar: BossBar) {
                hudPlayer.showBossBar(bar)
            }
            override fun hideBossBar(bar: BossBar) {
                hudPlayer.hideBossBar(bar)
            }
            override fun sendMessage(message: String) {
                hudPlayer.sendMessage(message)
            }
            override fun getLastDamageCause(): EntityDamageEvent? {
                return hudPlayer.lastDamageCause
            }
            override fun pointers(): Pointers {
                return hudPlayer.pointers()
            }
            override fun spigot(): Player.Spigot {
                return hudPlayer.spigot()
            }
        }
    }


    private class CachedHudBossbar(val hud: HudBossBar, val cacheUUID: UUID, val buf: FriendlyByteBuf)
    private class HudPlayerBossBar(val player: Player, val listener: ServerGamePacketListenerImpl, color: BossBar.Color, component: Component): ChannelDuplexHandler() {
        private inner class HudPlayerDummyBossBar(color: BossBar.Color) {
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
        private var dummy = HudPlayerDummyBossBar(color)
        private val dummyBarHandleMap = Collections.synchronizedMap(LinkedHashMap<UUID, CachedHudBossbar>())
        private val otherBarCache = ConcurrentLinkedQueue<Pair<UUID, HudByteBuf>>()
        private val uuid = UUID.randomUUID().apply {
            listener.send(ClientboundBossEventPacket.createAddPacket(HudBossBar(this, component, color)))
        }

        private var last: HudBossBar = HudBossBar(uuid, Component.empty(), color)
        private var onUse = uuid to HudByteBuf(Unpooled.buffer())

        init {
            val pipeLine = getConnection(listener).channel.pipeline()
            pipeLine.toMap().forEach {
                if (it.value is Connection) pipeLine.addBefore(it.key, INJECT_NAME, this)
            }
        }

        fun update(color: BossBar.Color, component: Component) {
            val bossBar = HudBossBar(uuid, component, color)
            last = bossBar
            listener.send(ClientboundBossEventPacket.createUpdateNamePacket(bossBar))
        }
        
        fun resetDummy(color: BossBar.Color) {
            listener.send(ClientboundBossEventPacket.createRemovePacket(uuid))
            dummy.dummyBarsUUID.forEach {
                listener.send(ClientboundBossEventPacket.createRemovePacket(it))
            }
            dummy = HudPlayerDummyBossBar(color)
            dummy.dummyBars.forEach { 
                listener.send(ClientboundBossEventPacket.createAddPacket(it))
            }
            listener.send(ClientboundBossEventPacket.createAddPacket(last))
        }

        fun remove() {
            val channel = getConnection(listener).channel
            channel.eventLoop().submit {
                channel.pipeline().remove(INJECT_NAME)
            }
            listener.send(ClientboundBossEventPacket.createRemovePacket(uuid))
            dummy.dummyBarsUUID.forEach {
                listener.send(ClientboundBossEventPacket.createRemovePacket(it))
            }
        }

        private fun writeBossBar(buf: FriendlyByteBuf, ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
            val originalUUID = buf.readUUID()
            if (originalUUID == uuid || dummy.dummyBarsUUID.contains(originalUUID)) {
                super.write(ctx, msg, promise)
                return
            }
            val enum = buf.readEnum(operation)

            fun getBuf(targetUUID: UUID = uuid) = HudByteBuf(Unpooled.buffer(1 shl 4))
                .writeUUID(targetUUID)

            fun sendProgress(getBuf: FriendlyByteBuf = getBuf(), targetBuf: FriendlyByteBuf = buf) = listener.send(ClientboundBossEventPacket(getBuf
                .writeEnum(operationEnum[2])
                .writeFloat(targetBuf.readFloat())
            ))
            fun sendName(getBuf: FriendlyByteBuf = getBuf(), targetBuf: FriendlyByteBuf = buf) = listener.send(ClientboundBossEventPacket(getBuf
                .writeEnum(operationEnum[3])
                .writeComponent(targetBuf.readComponentTrusted())
            ))
            fun sendStyle(getBuf: FriendlyByteBuf = getBuf(), targetBuf: FriendlyByteBuf = buf) = listener.send(ClientboundBossEventPacket(getBuf
                .writeEnum(operationEnum[4])
                .writeEnum(targetBuf.readEnum(BossEvent.BossBarColor::class.java))
                .writeEnum(targetBuf.readEnum(BossEvent.BossBarOverlay::class.java)))
            )
            fun sendProperties(getBuf: FriendlyByteBuf = getBuf(), targetBuf: FriendlyByteBuf = buf) = listener.send(ClientboundBossEventPacket(getBuf
                .writeEnum(operationEnum[5])
                .writeByte(targetBuf.readUnsignedByte().toInt())
            ))
            fun changeName(targetBuf: FriendlyByteBuf = buf) {
                runCatching {
                    val hud = BetterHudAPI.inst().playerManager.getHudPlayer(player.uniqueId) ?: return
                    val comp = toAdventure(targetBuf.readComponentTrusted())
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
                    @Suppress("DEPRECATION")
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
                            is TranslatableComponent -> BetterHudAPI.inst().translate(player.locale, component.key())
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
                    BetterHudAPI.inst().playerManager.getHudPlayer(player.uniqueId)?.additionalComponent = null
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
                        msg.write(this)
                    }
                    writeBossBar(buf, ctx, msg, promise)
                } else super.write(ctx, msg, promise)
            } else {
                super.write(ctx, msg, promise)
            }
        }
    }
    private class HudByteBuf(private val source: ByteBuf): FriendlyByteBuf(source) {
        override fun unwrap(): ByteBuf {
            return Unpooled.copiedBuffer(source)
        }
    }

    private class HudBossBar(val uuid: UUID, component: net.minecraft.network.chat.Component, color: BossBarColor): BossEvent(uuid, component, color, BossBarOverlay.PROGRESS) {
        constructor(uuid: UUID, component: Component, color: BossBar.Color): this(
            uuid,
            fromAdventure(component),
            getColor(color)
        )
        override fun getProgress(): Float {
            return 0F
        }
    }
}