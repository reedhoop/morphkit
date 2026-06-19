package com.morphkit.demo.compose.pages

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.morphkit.theme.MorphTokens
import com.morphkit.demo.compose.R

/**
 * H8: Compose Demo Text 页面。
 *
 * 展示 MorphTheme 提供的 typography 体系如何作用于 M3 Text 组件。
 * MorphTheme 包装了 MaterialTheme，所有 Text 自动继承 MorphKit 的
 * 颜色与排版 Token，无需业务方手动指定。
 */
@Composable
fun TextPage(onBack: () -> Unit) {
    val backContentDescription = stringResource(R.string.back)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MorphTokens.Spacing.spacingBase.dp),
        verticalArrangement = Arrangement.spacedBy(MorphTokens.Spacing.spacingBase.dp)
    ) {
        Text(
            text = stringResource(R.string.text_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = stringResource(R.string.text_body),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = stringResource(R.string.text_section_styles),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(R.string.text_style_display),
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = stringResource(R.string.text_style_headline),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.text_style_body),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = stringResource(R.string.text_style_label),
            style = MaterialTheme.typography.labelSmall
        )

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backContentDescription)
        }
    }
}
