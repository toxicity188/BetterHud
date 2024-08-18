package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.TriggerManager
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.ifNull
import net.kyori.adventure.audience.Audience
import java.util.function.Function

object TriggerManagerImpl: BetterHudManager, TriggerManager {

    private val map = mutableMapOf<String, (YamlObject) -> HudTrigger<*>>()

    override fun start() {

    }

    override fun addTrigger(name: String, trigger: Function<YamlObject, HudTrigger<*>>) {
        map[name] = {
            trigger.apply(it)
        }
    }

    fun getTrigger(yamlObject: YamlObject): HudTrigger<*> {
        val clazz = yamlObject.get("class")?.asString()?.ifNull("class value not found.")
        val builder = map[clazz].ifNull("this class doesn't exist: $clazz")
        return builder(yamlObject)
    }


    override fun reload(sender: Audience, resource: GlobalResource) {
    }
    override fun end() {
    }
}