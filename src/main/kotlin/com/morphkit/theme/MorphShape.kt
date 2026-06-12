package com.morphkit.theme

import android.content.Context
import androidx.annotation.Px

/**
 * MorphKit View 体系形状系统。
 *
 * 定义 iOS 连续性圆角近似的形状令牌，所有圆角值通过 Context 感知的
 * dp 转换确保在多窗口/折叠屏场景下返回正确的像素值。
 *
 * ## 使用方式
 *
 * ```kotlin
 * val radius = MorphShape.cornerMedium(context)
 * shapeDrawable.cornerRadius = radius.toFloat()
 * ```
 *
 * ## 圆角层级
 *
 * | 令牌 | dp 值 | 用途 |
 * |------|-------|------|
 * | cornerSmall | 8dp | 标签、Badge、小按钮 |
 * | cornerMedium | 12dp | 标准按钮、列表项、输入框 |
 * | cornerLarge | 16dp | 全宽卡片、Bottom Sheet、Dialog |
 * | cornerFull | Int.MAX_VALUE | 胶囊形状（哨兵值） |
 *
 * @see MorphTokens.Shapes.cornerRadiusButtonIos Token 层圆角常量
 */
object MorphShape {

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
}
