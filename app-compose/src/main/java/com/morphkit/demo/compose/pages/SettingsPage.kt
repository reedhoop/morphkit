package com.morphkit.demo.compose.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.morphkit.core.MorphKit
import com.morphkit.core.StylePolicy
import com.morphkit.theme.compose.MorphButton
import com.morphkit.theme.MorphTokens

@Composable
fun SettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = "Style Policy",
            style = MaterialTheme.typography.titleMedium
        )

        listOf(
            "AUTO" to StylePolicy.AUTO,
            "IOS" to StylePolicy.IOS,
            "PIXEL" to StylePolicy.PIXEL,
        ).forEach { (label, policy) ->
            MorphButton(
                text = "Switch to $label",
                onClick = {
                    // StylePolicy can only be set during MorphKit.init(), which runs once.
                    Toast.makeText(context, "Style: $label (requires restart)", Toast.LENGTH_SHORT).show()
                },
            )
        }

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = "Status",
            style = MaterialTheme.typography.titleMedium
        )

        Text(text = "Initialized: ${MorphKit.isInitialized()}")
        Text(text = "Theme ResId: ${MorphKit.getFinalThemeResId()}")

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}
