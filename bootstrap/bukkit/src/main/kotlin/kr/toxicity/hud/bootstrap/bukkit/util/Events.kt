package kr.toxicity.hud.bootstrap.bukkit.util

import kr.toxicity.hud.util.BOOTSTRAP
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

fun registerListener(listener: Listener) {
    Bukkit.getPluginManager().registerEvents(listener, BOOTSTRAP as Plugin)
}