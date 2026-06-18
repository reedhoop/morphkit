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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.morphkit.theme.compose.MorphButton
import com.morphkit.theme.compose.ButtonStyle
import com.morphkit.theme.MorphTokens
import com.morphkit.demo.compose.R

@Composable
fun ButtonPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val backContentDescription = stringResource(R.string.back)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MorphTokens.Spacing.spacingBase.dp),
        verticalArrangement = Arrangement.spacedBy(MorphTokens.Spacing.spacingBase.dp)
    ) {
        Text(
            text = stringResource(R.string.button_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        // Primary button
        MorphButton(
            text = stringResource(R.string.button_primary),
            onClick = {
                Toast.makeText(context, context.getString(R.string.button_primary_toast), Toast.LENGTH_SHORT).show()
            },
        )

        // Plain button
        MorphButton(
            text = stringResource(R.string.button_plain),
            onClick = {
                Toast.makeText(context, context.getString(R.string.button_plain_toast), Toast.LENGTH_SHORT).show()
            },
            style = ButtonStyle.PLAIN,
        )

        // Disabled button
        MorphButton(
            text = stringResource(R.string.button_disabled),
            // disabled, onClick 不会触发
            onClick = {},
            enabled = false,
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backContentDescription)
        }
    }
}
