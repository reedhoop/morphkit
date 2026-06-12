package com.morphkit.theme.compose

import androidx.compose.runtime.Immutable
import com.morphkit.theme.MorphTokens

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
        fun ios() = MorphShape(
            cornerRadiusButton = MorphTokens.Shapes.cornerRadiusButtonIos,
            cornerRadiusCard = MorphTokens.Shapes.cornerRadiusCardIos,
            cornerRadiusTextField = MorphTokens.Shapes.cornerRadiusTextFieldIos,
            cornerRadiusSmall = MorphTokens.Shapes.cornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.Shapes.cornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.Shapes.cornerRadiusLarge
        )

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
