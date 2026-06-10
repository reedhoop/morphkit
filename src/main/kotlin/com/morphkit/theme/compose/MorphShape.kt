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
            cornerRadiusButton = MorphTokens.cornerRadiusButtonIos,
            cornerRadiusCard = MorphTokens.cornerRadiusCardIos,
            cornerRadiusTextField = MorphTokens.cornerRadiusTextFieldIos,
            cornerRadiusSmall = MorphTokens.cornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.cornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.cornerRadiusLarge
        )

        fun pixel() = MorphShape(
            cornerRadiusButton = MorphTokens.cornerRadiusButtonPixel,
            cornerRadiusCard = MorphTokens.cornerRadiusCardPixel,
            cornerRadiusTextField = MorphTokens.cornerRadiusTextFieldPixel,
            cornerRadiusSmall = MorphTokens.cornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.cornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.cornerRadiusLarge
        )
    }
}
