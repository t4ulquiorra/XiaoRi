package echo.music.iad1tya.ui.player

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.palette.graphics.Palette

data class GradientOffset(
    val start: Offset,
    val end: Offset,
)

enum class GradientAngle {
    CW0,
    CW45,
    CW90,
    CW135,
    CW180,
    CW225,
    CW270,
    CW315,
}

fun GradientOffset(angle: GradientAngle): GradientOffset =
    when (angle) {
        GradientAngle.CW45 -> {
            GradientOffset(
                start = Offset.Zero,
                end = Offset.Infinite,
            )
        }

        GradientAngle.CW90 -> {
            GradientOffset(
                start = Offset.Zero,
                end = Offset(0f, Float.POSITIVE_INFINITY),
            )
        }

        GradientAngle.CW135 -> {
            GradientOffset(
                start = Offset(Float.POSITIVE_INFINITY, 0f),
                end = Offset(0f, Float.POSITIVE_INFINITY),
            )
        }

        GradientAngle.CW180 -> {
            GradientOffset(
                start = Offset(Float.POSITIVE_INFINITY, 0f),
                end = Offset.Zero,
            )
        }

        GradientAngle.CW225 -> {
            GradientOffset(
                start = Offset.Infinite,
                end = Offset.Zero,
            )
        }

        GradientAngle.CW270 -> {
            GradientOffset(
                start = Offset(0f, Float.POSITIVE_INFINITY),
                end = Offset.Zero,
            )
        }

        GradientAngle.CW315 -> {
            GradientOffset(
                start = Offset(0f, Float.POSITIVE_INFINITY),
                end = Offset(Float.POSITIVE_INFINITY, 0f),
            )
        }

        else -> {
            GradientOffset(
                start = Offset.Zero,
                end = Offset(Float.POSITIVE_INFINITY, 0f),
            )
        }
    }

fun Palette?.getColorFromPalette(): Color {
    val p = this ?: return Color(0xFF121212)
    val defaultColor = 0x000000
    var startColor = p.getDarkVibrantColor(defaultColor)
    if (startColor == defaultColor) {
        startColor = p.getDarkMutedColor(defaultColor)
        if (startColor == defaultColor) {
            startColor = p.getVibrantColor(defaultColor)
            if (startColor == defaultColor) {
                startColor = p.getMutedColor(defaultColor)
                if (startColor == defaultColor) {
                    startColor = p.getLightVibrantColor(defaultColor)
                    if (startColor == defaultColor) {
                        startColor = p.getLightMutedColor(defaultColor)
                    }
                }
            }
        }
    }
    return Color(startColor)
}

fun Palette?.toImmersiveBackground(): Color {
    val p = this ?: return Color.Black
    val rgb =
        p.getDominantColor(0).takeIf { it != 0 }
            ?: p.getMutedColor(0).takeIf { it != 0 }
            ?: p.getVibrantColor(0).takeIf { it != 0 }
            ?: return Color.Black
    val base = Color(rgb)
    val luminance = 0.299f * base.red + 0.587f * base.green + 0.114f * base.blue
    val darkenFactor = 0.35f + 0.45f * luminance
    return lerp(base, Color.Black, darkenFactor)
}

fun smoothScrimBrush(
    from: Color,
    to: Color,
    startFraction: Float = 0f,
    endFraction: Float = 1f,
    startY: Float = 0f,
    endY: Float = Float.POSITIVE_INFINITY,
    steps: Int = 24,
): Brush =
    Brush.verticalGradient(
        colorStops =
            Array(steps + 1) { i ->
                val t = i / steps.toFloat()
                val position = startFraction + (endFraction - startFraction) * t
                position to lerp(from, to, t * t * (3f - 2f * t))
            },
        startY = startY,
        endY = endY,
    )

fun artworkScrimBrush(
    color: Color,
    steps: Int = 24,
): Brush = smoothScrimBrush(from = color.copy(alpha = 0f), to = color, steps = steps)
