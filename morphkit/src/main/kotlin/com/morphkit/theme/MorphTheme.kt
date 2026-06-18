package com.morphkit.theme

import android.content.Context
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
 * ## 职责划分
 *
 * | 组件 | 职责 |
 * |------|------|
 * | [MorphTheme] | M3 语义色解析 |
 * | [MorphColors] | 颜色运算（叠加、混合、暗色检测） |
 * | [MorphTypography] | 排版令牌（字号、字重） |
 * | [MorphShape] | 形状令牌（圆角） |
 * | [MorphTokens] | 原始设计变量（唯一数据源） |
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
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, 0xFF212121.toInt())

    /** M3 主色容器色 */
    @ColorInt
    fun morphColorPrimaryContainer(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimaryContainer, MorphTokens.Colors.colorPrimaryContainer)

    /** M3 主色上的文字/图标色 */
    @ColorInt
    fun morphColorOnPrimary(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimary, 0xFFFFFFFF.toInt())

    /** M3 主色容器上的文字/图标色 */
    @ColorInt
    fun morphColorOnPrimaryContainer(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimaryContainer, 0xFF381E72.toInt())

    /** M3 表面色（对应 iOS systemBackground） */
    @ColorInt
    fun morphColorSurface(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, 0xFFF5F5F5.toInt())

    /** M3 表面变体色（对应 iOS secondarySystemBackground） */
    @ColorInt
    fun morphColorSurfaceVariant(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceVariant, 0xFFE7E0EC.toInt())

    /** M3 表面色上的文字/图标色（对应 iOS label） */
    @ColorInt
    fun morphColorOnSurface(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0xFF212121.toInt())

    /** M3 表面变体色上的文字/图标色（对应 iOS secondaryLabel） */
    @ColorInt
    fun morphColorOnSurfaceVariant(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, 0xFF49454F.toInt())

    /** M3 轮廓变体色（对应 iOS separator） */
    @ColorInt
    fun morphColorOutlineVariant(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOutlineVariant, 0xFFCAC4D0.toInt())

    /** M3 背景色（使用 Android 框架属性） */
    @ColorInt
    fun morphColorBackground(context: Context): Int =
        MaterialColors.getColor(context, android.R.attr.colorBackground, 0xFFF5F5F5.toInt())

    /** M3 背景色上的文字/图标色 */
    @ColorInt
    fun morphColorOnBackground(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0xFF212121.toInt())

    /** 通用主题属性颜色解析 */
    @ColorInt
    fun morphColor(context: Context, @AttrRes attr: Int): Int =
        MaterialColors.getColor(context, attr, 0xFF212121.toInt())

    // ═══════════════════════════════════════════════════════════════════════
    // 向后兼容 — 委托到 MorphColors / MorphTypography / MorphShape
    // 注意：以下方法标记为 ERROR 级别废弃，编译期即提示迁移
    // ═══════════════════════════════════════════════════════════════════════

    @Deprecated("Use MorphColors.createColorStateList()", ReplaceWith("MorphColors.createColorStateList(baseColor, isDarkMode)"), level = DeprecationLevel.ERROR)
    fun createColorStateList(baseColor: Int, isDarkMode: Boolean): android.content.res.ColorStateList =
        MorphColors.createColorStateList(baseColor, isDarkMode)

    @Deprecated("Use MorphColors.overlayColor()", ReplaceWith("MorphColors.overlayColor(baseColor, overlayColor, alpha)"), level = DeprecationLevel.ERROR)
    fun overlayColor(baseColor: Int, overlayColor: Int, alpha: Float): Int =
        MorphColors.overlayColor(baseColor, overlayColor, alpha)

    @Deprecated("Use MorphColors.adjustAlpha()", ReplaceWith("MorphColors.adjustAlpha(color, alpha)"), level = DeprecationLevel.ERROR)
    fun adjustAlpha(color: Int, alpha: Float): Int =
        MorphColors.adjustAlpha(color, alpha)

    @Deprecated("Use MorphColors.blendColor()", ReplaceWith("MorphColors.blendColor(from, to, ratio)"), level = DeprecationLevel.ERROR)
    fun blendColor(from: Int, to: Int, ratio: Float): Int =
        MorphColors.blendColor(from, to, ratio)

    @Deprecated("Use MorphColors.isDarkMode()", ReplaceWith("MorphColors.isDarkMode(context)"), level = DeprecationLevel.ERROR)
    fun isDarkMode(context: Context): Boolean =
        MorphColors.isDarkMode(context)

    @Deprecated("Use MorphShape.cornerSmall(context)", ReplaceWith("MorphShape.cornerSmall(context)"), level = DeprecationLevel.ERROR)
    @Px
    fun cornerSmall(context: Context): Int = MorphShape.cornerSmall(context)

    @Deprecated("Use MorphShape.cornerMedium(context)", ReplaceWith("MorphShape.cornerMedium(context)"), level = DeprecationLevel.ERROR)
    @Px
    fun cornerMedium(context: Context): Int = MorphShape.cornerMedium(context)

    @Deprecated("Use MorphShape.cornerLarge(context)", ReplaceWith("MorphShape.cornerLarge(context)"), level = DeprecationLevel.ERROR)
    @Px
    fun cornerLarge(context: Context): Int = MorphShape.cornerLarge(context)

    @Deprecated("Use MorphShape.cornerFull", ReplaceWith("MorphShape.cornerFull"), level = DeprecationLevel.ERROR)
    val cornerFull: Int = MorphShape.cornerFull

    @Deprecated("Use MorphTypography directly", ReplaceWith("MorphTypography"), level = DeprecationLevel.ERROR)
    val typography = MorphTypography
}

/**
 * dp 值转像素（Context 感知版本）。
 *
 * 使用 [Context.getResources] 获取当前 Activity 的 displayMetrics，
 * 避免 [android.content.res.Resources.getSystem] 在多窗口/折叠屏场景下
 * 返回不正确的 density 值。
 */
@Px
inline fun Int.dp(context: Context): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    context.resources.displayMetrics
).toInt()

/**
 * dp 值转像素（Float 版本，Context 感知）。
 */
@Px
inline fun Float.dp(context: Context): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    context.resources.displayMetrics
)
