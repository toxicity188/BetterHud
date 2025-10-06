package kr.toxicity.hud.bootstrap.fabric.compatibility

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.plugin.ReloadState.*
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.util.*
import net.minecraft.server.packs.metadata.pack.PackFormat
import net.minecraft.util.InclusiveRange

class PolymerResourcePackCompatibility : Compatibility {

    override val website: String = "https://modrinth.com/mod/polymer"

    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf()
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf()
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()

    override fun start() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register { builder ->
            ConfigManagerImpl.preReload()
            if (ConfigManagerImpl.mergeWithExternalResources) {
                when (val state = PLUGIN.reload()) {
                    is Success -> {
                        state.resourcePack.forEach { (path, data) ->
                            when (path) {
                                "pack.png" -> {}
                                "pack.mcmeta" -> data.toMcmeta().overlays?.entries?.forEach {
                                    builder.packMcMetaBuilder.addOverlay(
                                        InclusiveRange(PackFormat(it.formats.min, it.formats.max)),
                                        it.directory
                                    )
                                }
                                else -> builder.addData(path, data)
                            }
                        }
                        info("Polymer generation detected - reload completed: (${state.time} ms)")
                    }
                    is Failure -> {
                        state.throwable.handle("Fail to merge the resource pack with Polymer.")
                    }
                    is OnReload -> warn("This mod is still on reload!")
                }
            }
        }
        info(
            "BetterHud hooks Polymer resource pack.",
            "Be sure to set 'pack-type' to 'none' in your config."
        )
    }
}