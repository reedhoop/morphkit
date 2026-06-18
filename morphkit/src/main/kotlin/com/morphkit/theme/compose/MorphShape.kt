package com.morphkit.theme.compose

import androidx.compose.runtime.Immutable
import com.morphkit.theme.MorphTokens

/**
 * MorphKit Compose 形状体系。
 *
 * 定义各组件的圆角半径（单位：dp），与 View 层 [com.morphkit.theme.MorphShape] 对齐。
 *
 * L9 说明：字段类型为 `Int`（dp 数值），消费方需通过 `.dp` 扩展转换为 `Dp`，
 * 如 `RoundedCornerShape(shape.cornerRadiusButton.dp)`。
 * 保留 Int 而非改为 Dp 的原因：与 [MorphTokens.Shapes] Token 层（Int 常量）保持一致，
 * 避免在 data class 中引入 Dp 依赖导致 Token 层与 Compose 层耦合。
 *
 * @property cornerRadiusButton 按钮圆角半径（dp）
 * @property cornerRadiusCard 卡片圆角半径（dp）
 * @property cornerRadiusTextField 输入框圆角半径（dp）
 * @property cornerRadiusSmall 小圆角半径（dp），用于标签、Badge
 * @property cornerRadiusMedium 中圆角半径（dp），用于列表项
 * @property cornerRadiusLarge 大圆角半径（dp），用于 Dialog、Bottom Sheet
 */
@Immutable
data class MorphShape(
    val cornerRadiusButton: Int,
    val cornerRadiusCard: Int,
    val cornerRadiusTextField: Int,
    val cornerRadiusSmall: Int,
    val cornerRadiusMedium: Int,
    val cornerRadiusLarge: Int
) {
    companion object {
        /** 创建 iOS 风格形状实例（大圆角，连续性设计） */
        fun ios() = MorphShape(
            cornerRadiusButton = MorphTokens.Shapes.cornerRadiusButtonIos,
            cornerRadiusCard = MorphTokens.Shapes.cornerRadiusCardIos,
            cornerRadiusTextField = MorphTokens.Shapes.cornerRadiusTextFieldIos,
            cornerRadiusSmall = MorphTokens.Shapes.cornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.Shapes.cornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.Shapes.cornerRadiusLarge
        )

        /** 创建 Pixel (Material3) 风格形状实例（标准圆角） */
        fun pixel() = MorphShape(
            cornerRadiusButton = MorphTokens.Shapes.cornerRadiusButtonPixel,
            cornerRadiusCard = MorphTokens.Shapes.cornerRadiusCardPixel,
            cornerRadiusTextField = MorphTokens.Shapes.cornerRadiusTextFieldPixel,
            cornerRadiusSmall = MorphTokens.Shapes.cornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.Shapes.cornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.Shapes.cornerRadiusLarge
        )
    }
}
