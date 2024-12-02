package kr.toxicity.hud.pack

class PackFile(
    val path: String,
    val array: () -> ByteArray
) : () -> ByteArray by array