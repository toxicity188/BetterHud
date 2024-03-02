package kr.toxicity.hud.image

data class ImageLocation(val x: Int, val y: Int) {
    companion object {
        val zero = ImageLocation(0, 0)
    }
}