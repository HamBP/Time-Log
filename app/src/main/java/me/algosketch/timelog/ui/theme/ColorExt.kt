package me.algosketch.timelog.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

fun Color.toHex(): String = "#%02X%02X%02X".format(
    (red * 255).roundToInt(),
    (green * 255).roundToInt(),
    (blue * 255).roundToInt()
)

fun String.toComposeColor(): Color {
    val rgb = removePrefix("#").toLong(16)
    return Color(
        red = ((rgb shr 16) and 0xFF) / 255f,
        green = ((rgb shr 8) and 0xFF) / 255f,
        blue = (rgb and 0xFF) / 255f,
    )
}
