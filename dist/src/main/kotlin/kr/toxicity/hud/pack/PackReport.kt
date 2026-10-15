package kr.toxicity.hud.pack

data class PackReport(
    val files: Int,
    val fontJsons: Int,
    val fontJsonBytes: Long,
    val totalBytes: Long
) {
    companion object {
        fun of(pack: Map<String, ByteArray>): PackReport {
            var fonts = 0
            var fontBytes = 0L
            var total = 0L
            pack.forEach { (path, bytes) ->
                total += bytes.size
                if (isFontJson(path)) {
                    fonts++
                    fontBytes += bytes.size
                }
            }
            return PackReport(pack.size, fonts, fontBytes, total)
        }

        fun isFontJson(path: String) = path.endsWith(".json") && path.split('/').contains("font")
    }

    fun message() = "Resource pack: $files files, ${totalBytes.toMegabytes()} total, $fontJsons font json (${fontJsonBytes.toMegabytes()})"

    private fun Long.toMegabytes() = "%.2fMB".format(this / 1048576.0)
}
