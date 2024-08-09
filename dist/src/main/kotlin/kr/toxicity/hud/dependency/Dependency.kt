package kr.toxicity.hud.dependency

data class Dependency(
    val group: String,
    val name: String,
    val version: String,
    val relocation: Relocation
)