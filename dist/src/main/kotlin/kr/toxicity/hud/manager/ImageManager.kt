package kr.toxicity.hud.manager

import kr.toxicity.hud.image.*
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import org.bukkit.configuration.MemoryConfiguration
import java.io.File

object ImageManager: BetterHudManager {

    private val imageMap = HashMap<String, HudImage>()
    private val emptySetting = MemoryConfiguration()

    override fun start() {
    }

    fun getImage(name: String) = imageMap[name]



    override fun reload(resource: GlobalResource) {

        imageMap.clear()
        val assets = DATA_FOLDER.subFolder("assets")
        DATA_FOLDER.subFolder("images").forEachAllYaml { file, s, configurationSection ->
            runCatching {
                imageMap[s] = when (val type = ImageType.valueOf(configurationSection.getString("type").ifNull("type value not set.").uppercase())) {
                    ImageType.SINGLE -> {
                        HudImage(
                            s,
                            listOf(NamedLoadedImage(
                                "$s.png",
                                File(assets, configurationSection.getString("file").ifNull("file value not set."))
                                    .toImage()
                                    .removeEmptySide()
                                    .ifNull("Invalid image."),
                            )),
                            type,
                            configurationSection.getConfigurationSection("setting") ?: emptySetting
                        )
                    }
                    ImageType.LISTENER -> {
                        val splitType = (configurationSection.getString("split-type")?.let { splitType ->
                            runCatching {
                                SplitType.valueOf(splitType.uppercase())
                            }.getOrNull()
                        } ?: SplitType.LEFT)
                        HudImage(
                            s,
                            splitType.split(s, File(assets, configurationSection.getString("file").ifNull("file value not set."))
                                .toImage()
                                .removeEmptySide()
                                .ifNull("Invalid image.").image, configurationSection.getInt("split", 25).coerceAtLeast(1)),
                            type,
                            configurationSection.getConfigurationSection("setting").ifNull("setting configuration not found.")
                        )
                    }
                    ImageType.SEQUENCE -> {
                        HudImage(
                            s,
                            configurationSection.getStringList("files").ifEmpty {
                                warn("files is empty.")
                                return@forEachAllYaml
                            }.mapIndexed { index, string ->
                                File(assets, string)
                                    .toImage()
                                    .removeEmptyWidth()
                                    .ifNull("Invalid image: $string")
                                    .toNamed("${s}_${index + 1}.png")
                            },
                            type,
                            configurationSection.getConfigurationSection("setting") ?: emptySetting
                        )
                    }
                }
            }.onFailure { e ->
                warn("Unable to load this image: $s in ${file.name}")
                warn("Reason: ${e.message}")
            }
        }
        val saveLocation = resource.textures.subFolder("image")
        imageMap.values.forEach { value ->
            val list = value.image
            if (list.isNotEmpty()) {
                val imageSaveLocation = if (list.size == 1) saveLocation else saveLocation.subFolder(value.name)
                list.forEach {
                    val file = File(imageSaveLocation, it.name)
                    if (!file.exists()) it.image.image.save(file)
                }
            }
        }
    }

    override fun end() {
    }
}