package com.morphkit.demo.compose.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.morphkit.theme.MorphTokens
import com.morphkit.demo.compose.R

/**
 * H8: Compose Demo Selection 页面。
 *
 * 展示 M3 Checkbox / RadioButton 在 MorphTheme 下的交互。
 * 控件颜色从 MaterialTheme.colorScheme 读取，自动继承 MorphKit 主题色。
 */
@Composable
fun SelectionPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val backContentDescription = stringResource(R.string.back)

    var notifications by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(0) }

    val optionA = stringResource(R.string.selection_option_a)
    val optionB = stringResource(R.string.selection_option_b)
    val optionC = stringResource(R.string.selection_option_c)
    val options = remember(optionA, optionB, optionC) {
        listOf(optionA, optionB, optionC)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MorphTokens.Spacing.spacingBase.dp),
        verticalArrangement = Arrangement.spacedBy(MorphTokens.Spacing.spacingBase.dp)
    ) {
        Text(
            text = stringResource(R.string.selection_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = stringResource(R.string.selection_checkbox),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = notifications,
                onCheckedChange = {
                    notifications = it
                    Toast.makeText(
                        context,
                        context.getString(R.string.selection_checked_toast, it.toString()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            Text(text = stringResource(R.string.selection_check_notifications))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )
            Text(text = stringResource(R.string.selection_check_dark_mode))
        }

        Spacer(modifier = Modifier.height(MorphTokens.Spacing.spacingBase.dp))

        Text(
            text = stringResource(R.string.selection_radio),
            style = MaterialTheme.typography.titleMedium
        )

        options.forEachIndexed { index, label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedOption == index,
                        onClick = {
                            selectedOption = index
                            Toast.makeText(
                                context,
                                context.getString(R.string.selection_selected_toast, label),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
            ) {
                RadioButton(
                    selected = selectedOption == index,
                    onClick = null // 由 Row 的 selectable 处理点击
                )
                Text(text = label)
            }
        }

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backContentDescription)
        }
    }
}
