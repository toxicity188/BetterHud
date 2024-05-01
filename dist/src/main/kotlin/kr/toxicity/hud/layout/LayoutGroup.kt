package kr.toxicity.hud.layout

import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.equation.AnimationLocation
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.configuration.ConfigurationSection
import java.text.DecimalFormat

class LayoutGroup(
    override val path: String,
    section: ConfigurationSection
): HudConfiguration {

    private val loc = ImageLocation(section)

    val align = section.getString("align")?.let {
        runCatching {
            LayoutAlign.valueOf(it.uppercase())
        }.getOrNull()
    } ?: LayoutAlign.LEFT
    val offset = section.getString("offset")?.let {
        runCatching {
            LayoutOffset.valueOf(it.uppercase())
        }.getOrNull()
    } ?: LayoutOffset.CENTER

    val image: List<ImageLayout> = ArrayList<ImageLayout>().apply {
        section.getConfigurationSection("images")?.forEachSubConfiguration { s, configurationSection ->
            add(
                ImageLayout(
                    configurationSection.getString("name").ifNull("name value not set: $s").let { n ->
                        ImageManager.getImage(n).ifNull("this image doesn't exist: $n")
                    },
                    configurationSection.getString("color")?.toTextColor() ?: NamedTextColor.WHITE,
                    ImageLocation(configurationSection) + loc,
                    configurationSection.getDouble("scale", 1.0),
                    configurationSection.getBoolean("outline"),
                    configurationSection.getInt("layer"),
                    configurationSection.getString("follow"),
                    configurationSection.toConditions()
                )
            )
        }
    }
    val text: List<TextLayout> = ArrayList<TextLayout>().apply {
        section.getConfigurationSection("texts")?.forEachSubConfiguration { s, configurationSection ->
            val scale = configurationSection.getDouble("scale", 1.0)
            add(
                TextLayout(
                    configurationSection.getString("pattern").ifNull("pattern value not set: $s"),
                    configurationSection.getString("name").ifNull("name value not set: $s").let { n ->
                        TextManager.getText(n).ifNull("this text doesn't exist: $n")
                    },
                    ImageLocation(configurationSection) + loc,
                    scale,
                    configurationSection.getInt("space", 2).coerceAtLeast(0),
                    configurationSection.getString("align").toLayoutAlign(),
                    configurationSection.getString("color")?.toTextColor() ?: NamedTextColor.WHITE,
                    configurationSection.getBoolean("outline"),
                    configurationSection.getInt("layer"),
                    configurationSection.getBoolean("deserialize-text"),
                    configurationSection.getString("number-equation")?.let {
                        TEquation(it)
                    } ?: TEquation.t,
                    configurationSection.getString("number-format")?.let {
                        DecimalFormat(it)
                    } ?: ConfigManagerImpl.numberFormat,
                    configurationSection.getBoolean("disable-number-format", true),
                    configurationSection.getString("background")?.let {
                        BackgroundManager.getBackground(it)
                    },
                    configurationSection.getDouble("background-scale", scale),
                    configurationSection.getString("follow"),
                    configurationSection.toConditions()
                )
            )
        }
    }
    val head: List<HeadLayout> = ArrayList<HeadLayout>().apply {
        section.getConfigurationSection("heads")?.forEachSubConfiguration { s, configurationSection ->
            add(
                HeadLayout(
                    configurationSection.getString("name").ifNull("name value not set: $s").let {
                        PlayerHeadManager.getHead(it).ifNull("this head doesn't exist: $it in $s")
                    },
                    ImageLocation(configurationSection) + loc,
                    configurationSection.getBoolean("outline"),
                    configurationSection.getInt("layer"),
                    configurationSection.getString("align").toLayoutAlign(),
                    configurationSection.getString("follow"),
                    configurationSection.toConditions()
                )
            )
        }
    }
    val conditions = section.toConditions()

    val animation = section.getConfigurationSection("animations")?.let { animations ->
        AnimationLocation(animations)
    } ?: AnimationLocation.zero
}