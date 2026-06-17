package com.morphkit.demo.compose.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.morphkit.theme.compose.LocalMorphColors
import com.morphkit.theme.MorphTokens

@Composable
fun ThemePage(onBack: () -> Unit) {
    val morphColors = LocalMorphColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Theme & Colors",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = "MorphColorPalette",
            style = MaterialTheme.typography.titleMedium
        )

        // Color swatches
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Primary" to morphColors.primary,
                "OnPrimary" to morphColors.onPrimary,
                "Surface" to morphColors.surface,
                "OnSurface" to morphColors.onSurface,
                "Secondary" to morphColors.secondary,
                "Error" to morphColors.error,
            ).forEach { (label, color) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline)
                            .background(color)
                    )
                    Text(text = label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = "Typography Tokens",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "fontSizeBody = ${MorphTokens.Typography.fontSizeBody}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = "Spacing Tokens",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "spacingBase = ${MorphTokens.Spacing.spacingBase}",
            style = MaterialTheme.typography.bodyMedium
        )

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}
