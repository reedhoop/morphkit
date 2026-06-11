package com.morphkit.theme.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morphkit.core.InteractionMode
import com.morphkit.theme.MorphTokens
import kotlinx.coroutines.flow.collectLatest

/**
 * MorphKit Compose 按钮 — 交互模式分发核心组件。
 *
 * 根据 [LocalMorphInteractionMode] 动态决定触控反馈行为：
 *
 * | 交互模式 | 触控反馈 | 视觉特征 | 风格 |
 * |---------|---------|---------|------|
 * | [InteractionMode.IOS] | 按压变色（亮色变暗/暗色变亮），无涟漪 | 大圆角 12dp，零阴影 | iOS 极简 |
 * | [InteractionMode.MATERIAL] | 保留 Material3 Ripple 涟漪 | 圆角 8dp，M3 标准 | Pixel 原生 |
 *
 * ## OEM 接入规范（强制）
 *
 * **禁止**直接使用 `androidx.compose.material3.Button`，
 * **必须**使用 [MorphButton]，以保证 ROM 级的交互和视觉统一。
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param enabled 是否启用
 * @param interactionMode 交互模式，默认从 [LocalMorphInteractionMode] 读取
 */
@Composable
fun MorphButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionMode: InteractionMode = LocalMorphInteractionMode.current
) {
    val colors = LocalMorphColors.current
    val shape = LocalMorphShape.current

    when (interactionMode) {
        InteractionMode.IOS -> {
            IosButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = colors,
                cornerRadius = shape.cornerRadiusButton
            )
        }
        InteractionMode.MATERIAL -> {
            MaterialButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = colors,
                cornerRadius = shape.cornerRadiusButton
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// iOS 风格按钮 — 基于 InteractionSource 的按压变色 + 焦点指示器，无涟漪
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * iOS 风格按钮实现。
 *
 * 核心策略：
 * 1. 使用 [InteractionSource] 监听 [PressInteraction] + [FocusInteraction]
 * 2. 按压时：亮色模式下变暗（与 View 层一致），暗色模式下变亮
 * 3. 键盘焦点时：translationZ 视觉抬升（无障碍合规，与 View StateListAnimator 对齐）
 * 4. 文本居中对齐（与 View AppCompatButton 默认 gravity=CENTER 一致）
 * 5. 字体 17sp（与 View MorphTheme.typography.body 一致）
 */
@Composable
private fun IosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    colors: MorphColorPalette,
    cornerRadius: Int
) {
    val pressAlpha = remember { Animatable(0f) }
    val focusElevation = remember { Animatable(0f) }
    val interactionSource = remember { MutableInteractionSource() }

    // 监听 InteractionSource 的按压/松开/焦点事件，驱动动画
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    pressAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(MorphTokens.pressInDuration.toInt())
                    )
                }
                is PressInteraction.Release -> {
                    pressAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(MorphTokens.pressOutDuration.toInt())
                    )
                }
                is PressInteraction.Cancel -> {
                    pressAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(MorphTokens.pressOutDuration.toInt())
                    )
                }
                is FocusInteraction.Focus -> {
                    focusElevation.animateTo(
                        targetValue = 4f,
                        animationSpec = tween(200)
                    )
                }
                is FocusInteraction.Unfocus -> {
                    focusElevation.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(200)
                    )
                }
            }
        }
    }

    val backgroundColor = if (enabled) {
        colors.primary
    } else {
        colors.primary.copy(alpha = MorphTokens.disabledAlpha)
    }
    val contentColor = if (enabled) {
        colors.onPrimary
    } else {
        colors.onPrimary.copy(alpha = MorphTokens.disabledAlpha)
    }

    // 按压态颜色：与 View 层 MorphButton 一致
    // 亮色模式：叠加黑色变暗（View 用 Color.BLACK）
    // 暗色模式：叠加白色变亮（View 用 Color.WHITE）
    val displayColor = if (enabled) {
        val overlayAlpha = pressAlpha.value * MorphTokens.pressOverlayMaxAlpha
        val overlayColor = if (isDarkMode()) Color.White else Color.Black
        lerp(backgroundColor, overlayColor, overlayAlpha)
    } else {
        backgroundColor
    }

    val buttonShape = RoundedCornerShape(cornerRadius.dp)

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = 88.dp, minHeight = 48.dp)
            .shadow(elevation = focusElevation.value.dp, shape = buttonShape, clip = false)
            .clip(buttonShape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null, // iOS 模式：无涟漪
                        role = Role.Button,
                        onClick = onClick
                    )
                } else {
                    Modifier.semantics { role = Role.Button }
                }
            ),
        shape = buttonShape,
        color = displayColor,
        shadowElevation = 0.dp
    ) {
        // 字体对齐 View 层：MorphTheme.typography.body = 17sp MEDIUM
        ProvideTextStyle(
            value = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MorphTokens.spacingLg.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * 判断当前 Compose 是否处于暗色模式。
 */
@Composable
private fun isDarkMode(): Boolean {
    return androidx.compose.foundation.isSystemInDarkTheme()
}

// ═════════════════════════════════════════════════════════════════════════════════
// Material 风格按钮 — 保留 Ripple 涟漪
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun MaterialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    colors: MorphColorPalette,
    cornerRadius: Int
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.primary.copy(alpha = MorphTokens.disabledAlpha),
            disabledContentColor = colors.onPrimary.copy(alpha = MorphTokens.disabledAlpha)
        ),
        contentPadding = PaddingValues(horizontal = MorphTokens.spacingXl.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}
