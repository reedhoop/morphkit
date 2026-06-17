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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.morphkit.theme.compose.LocalMorphColors
import com.morphkit.theme.MorphTokens
import com.morphkit.demo.compose.R

@Composable
fun ThemePage(onBack: () -> Unit) {
    val morphColors = LocalMorphColors.current
    val backContentDescription = stringResource(R.string.back)

    val labelPrimary = stringResource(R.string.theme_color_primary)
    val labelOnPrimary = stringResource(R.string.theme_color_on_primary)
    val labelSurface = stringResource(R.string.theme_color_surface)
    val labelOnSurface = stringResource(R.string.theme_color_on_surface)
    val labelSecondary = stringResource(R.string.theme_color_secondary)
    val labelError = stringResource(R.string.theme_color_error)

    val swatches = remember(
        morphColors.primary,
        morphColors.onPrimary,
        morphColors.surface,
        morphColors.onSurface,
        morphColors.secondary,
        morphColors.error,
        labelPrimary,
        labelOnPrimary,
        labelSurface,
        labelOnSurface,
        labelSecondary,
        labelError,
    ) {
        listOf(
            labelPrimary to morphColors.primary,
            labelOnPrimary to morphColors.onPrimary,
            labelSurface to morphColors.surface,
            labelOnSurface to morphColors.onSurface,
            labelSecondary to morphColors.secondary,
            labelError to morphColors.error,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MorphTokens.Spacing.spacingBase.dp),
        verticalArrangement = Arrangement.spacedBy(MorphTokens.Spacing.spacingBase.dp)
    ) {
        Text(
            text = stringResource(R.string.theme_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = stringResource(R.string.theme_palette_title),
            style = MaterialTheme.typography.titleMedium
        )

        // Color swatches
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MorphTokens.Spacing.spacingSm.dp)
        ) {
            swatches.forEach { (label, color) ->
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
            text = stringResource(R.string.theme_typography_title),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "fontSizeBody = ${MorphTokens.Typography.fontSizeBody}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = stringResource(R.string.theme_spacing_title),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "spacingBase = ${MorphTokens.Spacing.spacingBase}",
            style = MaterialTheme.typography.bodyMedium
        )

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backContentDescription)
        }
    }
}
