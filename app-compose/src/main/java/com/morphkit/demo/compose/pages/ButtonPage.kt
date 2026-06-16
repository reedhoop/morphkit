package com.morphkit.demo.compose.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.morphkit.theme.compose.MorphButton
import com.morphkit.theme.compose.ButtonStyle
import com.morphkit.theme.MorphTokens

@Composable
fun ButtonPage(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "MorphButton (Compose)",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        // Primary button
        MorphButton(
            text = "Primary Button",
            onClick = { },
        )

        // Plain button
        MorphButton(
            text = "Plain Button",
            onClick = { },
            style = ButtonStyle.PLAIN,
        )

        // Disabled button
        MorphButton(
            text = "Disabled Button",
            onClick = { },
            enabled = false,
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        IconButton(onClick = onBack) {
            Text("← Back")
        }
    }
}
