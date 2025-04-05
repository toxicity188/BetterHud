package kr.toxicity.hud.bootstrap.bukkit.util

import kr.toxicity.hud.api.bukkit.nms.NMS
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.VOLATILE_CODE
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin

val LivingEntity.maximumHealth
    get() = getAttribute(ATTRIBUTE_MAX_HEALTH)!!.value

const val ENTITY_ADAPTER_KEY = "betterhud_entity_adapter"

inline val <reified T : Entity> T.adapt
    get() = if (BOOTSTRAP.isFolia) getMetadata(ENTITY_ADAPTER_KEY).firstNotNullOfOrNull {
        it as? T
    } ?: (VOLATILE_CODE as NMS).getFoliaAdaptedEntity(this).also {
        setMetadata(ENTITY_ADAPTER_KEY, FixedMetadataValue(BOOTSTRAP as Plugin, it))
    } else this