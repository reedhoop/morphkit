package com.morphkit.demo.compose.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.morphkit.theme.MorphTokens
import com.morphkit.demo.compose.R
import com.morphkit.demo.compose.Routes

@Composable
fun CatalogPage(onNavigate: (String) -> Unit) {
    val themeTitle = stringResource(R.string.theme_title)
    val settingsTitle = stringResource(R.string.settings_title)
    val buttonTitle = stringResource(R.string.catalog_item_button)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MorphTokens.Spacing.spacingBase.dp),
        verticalArrangement = Arrangement.spacedBy(MorphTokens.Spacing.spacingBase.dp)
    ) {
        Text(
            text = stringResource(R.string.catalog_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        val items = remember(themeTitle, settingsTitle, buttonTitle) {
            listOf(
                buttonTitle to Routes.BUTTON,
                themeTitle to Routes.THEME,
                settingsTitle to Routes.SETTINGS,
            )
        }

        items.forEach { (label, route) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(route) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(MorphTokens.Spacing.spacingBase.dp)
                )
            }
        }
    }
}
