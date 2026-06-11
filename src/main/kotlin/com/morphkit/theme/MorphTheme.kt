package com.morphkit.theme

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.google.android.material.color.MaterialColors

/**
 * MorphKit 设计系统基座（M3 重构版）。
 *
 * 彻底废弃硬编码色彩，完美集成 Material Design 3 (M3) 语义色系统，
 * 同时保留 iOS 极简交互视觉风格。
 *
 * ## iOS ↔ M3 语义映射
 *
 * | iOS 语义                   | M3 语义属性              | 用途           |
 * |---------------------------|-------------------------|----------------|
 * | tintColor                 | colorPrimary            | 主色、按钮、链接 |
 * | systemBackground          | colorSurface            | 页面背景       |
 * | secondarySystemBackground | colorSurfaceVariant     | 分组背景、卡片  |
 * | separator                 | colorOutlineVariant     | 分割线、边框   |
 * | label                     | colorOnSurface          | 主文字         |
 * | secondaryLabel            | colorOnSurfaceVariant   | 次级文字       |
 */
object MorphTheme {

    // ═══════════════════════════════════════════════════════════════════════
    // 颜色体系 — M3 语义色解析（废弃硬编码）
    // ═══════════════════════════════════════════════════════════════════════

    /** M3 主色（对应 iOS tintColor） */
    @ColorInt
    fun morphColorPrimary(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, 0)

    /** M3 主色容器色 */
    @ColorInt
    fun morphColorPrimaryContainer(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimaryContainer, 0)

    /** M3 主色上的文字/图标色 */
    @ColorInt
    fun morphColorOnPrimary(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimary, 0)

    /** M3 主色容器上的文字/图标色 */
    @ColorInt
    fun morphColorOnPrimaryContainer(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimaryContainer, 0)

    /** M3 表面色（对应 iOS systemBackground） */
    @ColorInt
    fun morphColorSurface(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, 0)

    /** M3 表面变体色（对应 iOS secondarySystemBackground） */
    @ColorInt
    fun morphColorSurfaceVariant(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceVariant, 0)

    /** M3 表面色上的文字/图标色（对应 iOS label） */
    @ColorInt
    fun morphColorOnSurface(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0)

    /** M3 表面变体色上的文字/图标色（对应 iOS secondaryLabel） */
    @ColorInt
    fun morphColorOnSurfaceVariant(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, 0)

    /** M3 轮廓变体色（对应 iOS separator） */
    @ColorInt
    fun morphColorOutlineVariant(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOutlineVariant, 0)

    /** M3 背景色（使用 Android 框架属性） */
    @ColorInt
    fun morphColorBackground(context: Context): Int =
        MaterialColors.getColor(context, android.R.attr.colorBackground, 0)

    /** M3 背景色上的文字/图标色 */
    @ColorInt
    fun morphColorOnBackground(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0)

    /** 通用主题属性颜色解析 */
    @ColorInt
    fun morphColor(context: Context, @AttrRes attr: Int): Int =
        MaterialColors.getColor(context, attr, 0)

    /**
     * 创建带状态的颜色列表（ColorStateList）。
     *
     * - 正常态：原始颜色
     * - 按下态：叠加 20% 遮罩
     * - 禁用态：降低不透明度至 38%
     */
    fun createColorStateList(baseColor: Int, isDarkMode: Boolean): android.content.res.ColorStateList {
        val pressedColor = overlayColor(baseColor, if (isDarkMode) Color.WHITE else Color.BLACK, 0.2f)
        val disabledColor = adjustAlpha(baseColor, 0.38f)

        return android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf()
            ),
            intArrayOf(disabledColor, pressedColor, baseColor)
        )
    }

    /** 给颜色叠加遮罩 */
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

    /** 调整颜色的不透明度 */
    fun adjustAlpha(color: Int, alpha: Float): Int {
        return Color.argb(
            (Color.alpha(color) * alpha).toInt().coerceIn(0, 255),
            Color.red(color), Color.green(color), Color.blue(color)
        )
    }

    /**
     * 线性混合两个颜色。
     *
     * @param from   基础色（ARGB）
     * @param to     目标色（ARGB）
     * @param ratio  混合比例 [0f, 1f]，0 返回 from，1 返回 to
     * @return 混合后的颜色
     */
    fun blendColor(from: Int, to: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = (Color.red(from) * inverseRatio + Color.red(to) * ratio).toInt().coerceIn(0, 255)
        val g = (Color.green(from) * inverseRatio + Color.green(to) * ratio).toInt().coerceIn(0, 255)
        val b = (Color.blue(from) * inverseRatio + Color.blue(to) * ratio).toInt().coerceIn(0, 255)
        val a = (Color.alpha(from) * inverseRatio + Color.alpha(to) * ratio).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    /** 判断当前是否为暗黑模式 */
    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 形状体系 — iOS 连续性圆角近似
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 小圆角 8dp — 标签、Badge、小按钮。
     *
     * @param context 用于获取正确的 displayMetrics，支持多窗口/折叠屏场景
     */
    @Px
    fun cornerSmall(context: Context): Int = 8.dp(context)

    /**
     * 中圆角 12dp — 标准按钮、列表项、输入框。
     *
     * @param context 用于获取正确的 displayMetrics，支持多窗口/折叠屏场景
     */
    @Px
    fun cornerMedium(context: Context): Int = 12.dp(context)

    /**
     * 大圆角 16dp — 全宽卡片、Bottom Sheet、Dialog。
     *
     * @param context 用于获取正确的 displayMetrics，支持多窗口/折叠屏场景
     */
    @Px
    fun cornerLarge(context: Context): Int = 16.dp(context)

    /**
     * 全圆角（胶囊形状）— 哨兵值 [Int.MAX_VALUE]。
     *
     * 用于 [android.graphics.drawable.GradientDrawable.setCornerRadius]，
     * 表示半径足够大以形成完美胶囊形状。
     * 注意：此值非真实像素值，不标注 [Px]，与 [cornerSmall] / [cornerMedium] / [cornerLarge] 语义不同。
     */
    val cornerFull: Int = Int.MAX_VALUE

    // ═══════════════════════════════════════════════════════════════════════
    // 排版体系 — SF Pro 视觉权重对齐
    // ═══════════════════════════════════════════════════════════════════════

    val typography = MorphTypography()

    class MorphTypography {
        val largeTitle = TextStyle(fontSize = 34f, weight = FontWeight.EXTRA_BOLD)
        val title1 = TextStyle(fontSize = 28f, weight = FontWeight.EXTRA_BOLD)
        val title2 = TextStyle(fontSize = 22f, weight = FontWeight.BOLD)
        val title3 = TextStyle(fontSize = 20f, weight = FontWeight.BOLD)
        val headline = TextStyle(fontSize = 17f, weight = FontWeight.SEMI_BOLD)
        val body = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)
        val callout = TextStyle(fontSize = 16f, weight = FontWeight.MEDIUM)
        val subheadline = TextStyle(fontSize = 15f, weight = FontWeight.MEDIUM)
        val footnote = TextStyle(fontSize = 13f, weight = FontWeight.MEDIUM)
        val caption1 = TextStyle(fontSize = 12f, weight = FontWeight.MEDIUM)
        val caption2 = TextStyle(fontSize = 11f, weight = FontWeight.MEDIUM)
    }

    data class TextStyle(val fontSize: Float, val weight: FontWeight)

    enum class FontWeight(val weight: Int) {
        MEDIUM(500),
        SEMI_BOLD(600),
        BOLD(700),
        EXTRA_BOLD(800);

        fun toTypeface(): android.graphics.Typeface = when (this) {
            MEDIUM -> android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            SEMI_BOLD -> android.graphics.Typeface.create("sans-serif-semibold", android.graphics.Typeface.NORMAL)
            BOLD -> android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
            EXTRA_BOLD -> android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
        }
    }
}

/**
 * dp 值转像素（Context 感知版本）。
 *
 * 使用 [Context.getResources] 获取当前 Activity 的 displayMetrics，
 * 避免 [android.content.res.Resources.getSystem] 在多窗口/折叠屏场景下
 * 返回不正确的 density 值。
 */
@Px
fun Int.dp(context: Context): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    context.resources.displayMetrics
).toInt()

/**
 * dp 值转像素（Float 版本，Context 感知）。
 */
@Px
fun Float.dp(context: Context): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    context.resources.displayMetrics
)
