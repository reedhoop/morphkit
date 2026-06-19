package com.morphkit.demo.compose.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.morphkit.theme.MorphTokens
import com.morphkit.demo.compose.R

/**
 * H8: Compose Demo Card 页面。
 *
 * 展示 M3 Card / ElevatedCard / OutlinedCard 在 MorphTheme 下的视觉效果。
 * 卡片颜色全部从 MaterialTheme.colorScheme 读取，而 colorScheme 由
 * MorphTheme 提供，因此自动继承 MorphKit 主题色。
 */
@Composable
fun CardPage(onBack: () -> Unit) {
    val backContentDescription = stringResource(R.string.back)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MorphTokens.Spacing.spacingBase.dp),
        verticalArrangement = Arrangement.spacedBy(MorphTokens.Spacing.spacingBase.dp)
    ) {
        Text(
            text = stringResource(R.string.card_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(MorphTokens.Spacing.spacingBase.dp)) {
                Text(
                    text = stringResource(R.string.card_standard),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.card_standard_body),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(MorphTokens.Spacing.spacingBase.dp)) {
                Text(
                    text = stringResource(R.string.card_elevated),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.card_elevated_body),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(MorphTokens.Spacing.spacingBase.dp)) {
                Text(
                    text = stringResource(R.string.card_outlined),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.card_outlined_body),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backContentDescription)
        }
    }
}
