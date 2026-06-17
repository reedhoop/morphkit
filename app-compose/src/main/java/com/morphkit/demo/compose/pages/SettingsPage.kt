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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.morphkit.core.MorphKit
import com.morphkit.core.StylePolicy
import com.morphkit.theme.compose.MorphButton
import com.morphkit.theme.MorphTokens
import com.morphkit.demo.compose.R

@Composable
fun SettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val backContentDescription = stringResource(R.string.back)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MorphTokens.Spacing.spacingBase.dp),
        verticalArrangement = Arrangement.spacedBy(MorphTokens.Spacing.spacingBase.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = stringResource(R.string.settings_style_policy),
            style = MaterialTheme.typography.titleMedium
        )

        val policies = remember {
            listOf(
                "AUTO" to StylePolicy.AUTO,
                "IOS" to StylePolicy.IOS,
                "PIXEL" to StylePolicy.PIXEL,
            )
        }

        policies.forEach { (label, policy) ->
            MorphButton(
                text = stringResource(R.string.settings_switch_to, label),
                onClick = {
                    // StylePolicy can only be set during MorphKit.init(), which runs once.
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_requires_restart, label),
                        Toast.LENGTH_SHORT
                    ).show()
                },
            )
        }

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = stringResource(R.string.settings_status),
            style = MaterialTheme.typography.titleMedium
        )

        Text(text = stringResource(R.string.settings_initialized, MorphKit.isInitialized()))
        Text(text = stringResource(R.string.settings_theme_resid, if (MorphKit.isInitialized()) MorphKit.getFinalThemeResId() else 0))

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backContentDescription)
        }
    }
}
