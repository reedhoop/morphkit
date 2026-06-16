package com.morphkit.theme

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.morphkit.theme.MorphTokens

/**
 * MorphKit color manipulation utilities.
 *
 * Extracted from [MorphTheme] to reduce its responsibility.
 * Provides pure color math operations (overlay, alpha, blend)
 * and dark-mode detection.
 *
 * @see MorphTheme for M3 semantic color resolution and design tokens
 */
object MorphColors {

    /**
     * Overlay [overlayColor] on top of [baseColor] with the given [alpha] ratio.
     *
     * The effective blend ratio is derived from the overlay's own alpha channel
     * multiplied by [alpha]:
     * ```
     * effectiveRatio = (Color.alpha(overlayColor) * alpha) / 255
     * ```
     *
     * @param baseColor    The background color (ARGB)
     * @param overlayColor The foreground overlay color (ARGB)
     * @param alpha        Overlay intensity in [0f, 1f]
     * @return The blended color (ARGB)
     */
    fun overlayColor(baseColor: Int, overlayColor: Int, alpha: Float): Int {
        val ratio = (Color.alpha(overlayColor) * alpha) / 255f
        val inverseRatio = 1f - ratio
        return Color.argb(
            Color.alpha(baseColor),
            (Color.red(baseColor) * inverseRatio + Color.red(overlayColor) * ratio).toInt().coerceIn(0, 255),
            (Color.green(baseColor) * inverseRatio + Color.green(overlayColor) * ratio).toInt().coerceIn(0, 255),
            (Color.blue(baseColor) * inverseRatio + Color.blue(overlayColor) * ratio).toInt().coerceIn(0, 255)
        )
    }

    /**
     * Adjust the alpha channel of [color] by multiplying it with [alpha].
     *
     * @param color The source color (ARGB)
     * @param alpha Alpha multiplier in [0f, 1f]
     * @return The color with adjusted alpha (RGB channels unchanged)
     */
    fun adjustAlpha(color: Int, alpha: Float): Int {
        return Color.argb(
            (Color.alpha(color) * alpha).toInt().coerceIn(0, 255),
            Color.red(color), Color.green(color), Color.blue(color)
        )
    }

    /**
     * Linearly blend two colors.
     *
     * @param from  Base color (ARGB)
     * @param to    Target color (ARGB)
     * @param ratio Blend ratio in [0f, 1f]; 0 returns [from], 1 returns [to]
     * @return The blended color (ARGB)
     */
    fun blendColor(from: Int, to: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = (Color.red(from) * inverseRatio + Color.red(to) * ratio).toInt().coerceIn(0, 255)
        val g = (Color.green(from) * inverseRatio + Color.green(to) * ratio).toInt().coerceIn(0, 255)
        val b = (Color.blue(from) * inverseRatio + Color.blue(to) * ratio).toInt().coerceIn(0, 255)
        val a = (Color.alpha(from) * inverseRatio + Color.alpha(to) * ratio).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    /**
     * Create a [android.content.res.ColorStateList] with pressed/disabled states.
     *
     * - Normal state:  [baseColor] as-is
     * - Pressed state: [baseColor] overlaid with 20% black (light mode) or white (dark mode)
     * - Disabled state: [baseColor] with alpha reduced to 38%
     *
     * @param baseColor  The base color for all states
     * @param isDarkMode Whether the current UI is in dark mode (affects pressed overlay color)
     */
    fun createColorStateList(baseColor: Int, isDarkMode: Boolean): android.content.res.ColorStateList {
        val pressedColor = overlayColor(baseColor, if (isDarkMode) Color.WHITE else Color.BLACK, MorphTokens.Interaction.pressOverlayMaxAlpha)
        val disabledColor = adjustAlpha(baseColor, MorphTokens.Interaction.disabledAlpha)

        return android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf()
            ),
            intArrayOf(disabledColor, pressedColor, baseColor)
        )
    }

    /**
     * Detect whether the device is currently in dark (night) mode.
     *
     * @param context Any context (Activity, Application, etc.)
     * @return `true` if `UI_MODE_NIGHT_YES` is set in the current configuration
     */
    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}
