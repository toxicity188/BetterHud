package kr.toxicity.hud.nms.v1_17_R1

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.nms.NMS
import kr.toxicity.hud.api.nms.NMSVersion
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.BossEvent
import org.bukkit.boss.BarColor
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import java.util.*

class NMSImpl: NMS {
    companion object {
        private const val INJECT_NAME = BetterHud.DEFAULT_NAMESPACE
        private val bossBarMap = HashMap<UUID, PlayerBossBar>()

        @Suppress("UNCHECKED_CAST")
        private val operation = ClientboundBossEventPacket::class.java.declaredClasses.first {
            it.isEnum
        } as Class<out Enum<*>>

        private val operationEnum = operation.enumConstants

        private fun toAdventure(component: net.minecraft.network.chat.Component) = GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(component))
        private fun fromAdventure(component: Component) = CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component))
        private fun getColor(color: BarColor) =  when (color) {
            BarColor.PINK -> BossEvent.BossBarColor.PINK
            BarColor.BLUE -> BossEvent.BossBarColor.BLUE
            BarColor.RED -> BossEvent.BossBarColor.RED
            BarColor.GREEN -> BossEvent.BossBarColor.GREEN
            BarColor.YELLOW -> BossEvent.BossBarColor.YELLOW
            BarColor.PURPLE -> BossEvent.BossBarColor.PURPLE
            BarColor.WHITE -> BossEvent.BossBarColor.WHITE
        }
    }

    override fun showBossBar(player: Player, color: BarColor, component: Component) {
        player as CraftPlayer
        bossBarMap.getOrPut(player.uniqueId) {
            PlayerBossBar(player, player.handle.connection, color, component)
        }.update(color, component)
    }

    override fun removeBossBar(player: Player) {
        bossBarMap.remove(player.uniqueId)?.remove()
    }

    override fun getVersion(): NMSVersion {
        return NMSVersion.V1_17_R1
    }

    private class PlayerBossBar(val player: Player, val listener: ServerGamePacketListenerImpl, color: BarColor, component: Component): ChannelDuplexHandler() {
        private val uuid = UUID.randomUUID().apply {
            listener.send(ClientboundBossEventPacket.createAddPacket(HudBossBar(this, component, color)))
        }
        private var saveUUID = uuid

        private var last: HudBossBar = HudBossBar(uuid, Component.empty(), BarColor.RED)
        private val bufQueue = mutableMapOf<UUID, FriendlyByteBuf>()
        private var onUse = false
        private var toggle = false

        init {
            val pipeLine = listener.connection.channel.pipeline()
            pipeLine.toMap().forEach {
                if (it.value is Connection) pipeLine.addBefore(it.key, INJECT_NAME, this)
            }
        }

        fun update(color: BarColor, component: Component) {
            val bossBar = HudBossBar(uuid, component, color)
            last = bossBar
            listener.send(ClientboundBossEventPacket.createUpdateNamePacket(bossBar))
        }

        fun remove() {
            listener.send(ClientboundBossEventPacket.createRemovePacket(uuid))
            val channel = listener.connection.channel
            channel.eventLoop().submit {
                channel.pipeline().remove(INJECT_NAME)
            }
        }

        private fun writeBossBar(buf: FriendlyByteBuf, ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {

            val originalUUID = buf.readUUID()
            if (originalUUID == uuid) {
                super.write(ctx, msg, promise)
                return
            }
            if (toggle) {
                toggle = false
                super.write(ctx, msg, promise)
                return
            }
            val enum = buf.readEnum(operation)

            fun getBuf() = FriendlyByteBuf(Unpooled.buffer(1 shl 4))
                .writeUUID(uuid)

            fun sendProgress(targetBuf: FriendlyByteBuf = buf) = listener.send(ClientboundBossEventPacket(getBuf().apply {
                writeEnum(operationEnum[2])
                writeFloat(targetBuf.readFloat())
            }))
            fun sendStyle(targetBuf: FriendlyByteBuf = buf) = listener.send(ClientboundBossEventPacket(getBuf()
                .writeEnum(operationEnum[4])
                .writeEnum(targetBuf.readEnum(BossEvent.BossBarColor::class.java))
                .writeEnum(targetBuf.readEnum(BossEvent.BossBarOverlay::class.java)))
            )
            fun sendProperties(targetBuf: FriendlyByteBuf = buf) = listener.send(ClientboundBossEventPacket(getBuf().apply {
                writeEnum(operationEnum[5])
                writeByte(targetBuf.readUnsignedByte().toInt())
            }))
            fun sendName(targetBuf: FriendlyByteBuf = buf) {
                runCatching {
                    val hud = BetterHud.getInstance().getHudPlayer(player)
                    val comp = toAdventure(targetBuf.readComponent())
                    val key = Key.key("$INJECT_NAME:default")
                    fun applyFont(component: Component): Component {
                        return component.font(key).children(component.children().map {
                            applyFont(it)
                        })
                    }
                    fun getWidth(component: Component): Int {
                        val style = component.style()
                        return component.children().sumOf {
                            getWidth(it)
                        } + ((component as? TextComponent)?.content()?.sumOf {
                            var t = BetterHud.getInstance().getWidth(it)
                            if (style.hasDecoration(TextDecoration.BOLD)) t++
                            if (style.hasDecoration(TextDecoration.ITALIC)) t++
                            t + 1
                        } ?: 0)
                    }
                    hud.additionalComponent = WidthComponent(applyFont(comp), getWidth(comp))
                }
            }

            runCatching {
                if (saveUUID != originalUUID) {
                    when (val type = enum.ordinal) {
                        0 -> {
                            if (!onUse) {
                                sendName()
                                sendProgress()
                                sendStyle()
                                sendProperties()
                                saveUUID = originalUUID
                                onUse = true
                            } else {
                                bufQueue[originalUUID] = FriendlyByteBuf(Unpooled.copiedBuffer(buf.unwrap()))
                                super.write(ctx, msg, promise)
                            }
                        }
                        else -> {
                            if (type == 1) bufQueue.remove(originalUUID)
                            super.write(ctx, msg, promise)
                        }
                    }
                } else {
                    when (enum.ordinal) {
                        1 -> {
                            bufQueue.keys.firstOrNull()?.let { uuid ->
                                bufQueue.remove(uuid)?.let {
                                    toggle = true
                                    listener.send(ClientboundBossEventPacket.createRemovePacket(uuid))
                                    saveUUID = uuid
                                    sendName(it)
                                    sendProgress(it)
                                    sendStyle(it)
                                    sendProperties(it)
                                }
                            } ?: run {
                                saveUUID = uuid
                                onUse = false
                                BetterHud.getInstance().getHudPlayer(player).additionalComponent = null
                                listener.send(ClientboundBossEventPacket.createUpdateNamePacket(last))
                                listener.send(ClientboundBossEventPacket.createUpdateStylePacket(last))
                                listener.send(ClientboundBossEventPacket.createUpdatePropertiesPacket(last))
                                listener.send(ClientboundBossEventPacket.createUpdateProgressPacket(last))
                            }
                        }
                        2 -> {
                            sendProgress()
                        }
                        3 -> {
                            sendName()
                        }
                        4 -> {
                            sendStyle()
                        }
                        5 -> {
                            sendProperties()
                        }
                        else -> {}
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }

        override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
            if (msg is ClientboundBossEventPacket) {
                val buf = FriendlyByteBuf(Unpooled.buffer(1 shl 4)).apply {
                    msg.write(this)
                }
                writeBossBar(buf, ctx, msg, promise)
            } else {
                super.write(ctx, msg, promise)
            }
        }
    }

    private class HudBossBar(uuid: UUID, component: Component, color: BarColor): BossEvent(uuid, fromAdventure(component), getColor(color), BossBarOverlay.PROGRESS) {
        override fun getProgress(): Float {
            return 0F
        }
    }
}