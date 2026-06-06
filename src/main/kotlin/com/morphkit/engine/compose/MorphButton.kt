package com.morphkit.engine.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morphkit.engine.MorphTokens
import kotlinx.coroutines.launch

/**
 * MorphKit Compose 按钮 — 交互模式分发核心组件。
 *
 * 根据 [LocalMorphInteractionMode] 动态决定触控反馈行为：
 *
 * | 交互模式 | 触控反馈 | 视觉特征 | 风格 |
 * |---------|---------|---------|------|
 * | [MorphInteractionMode.IOS] | 按压整体变色，无涟漪 | 大圆角 12dp，零阴影 | iOS 极简 |
 * | [MorphInteractionMode.MATERIAL] | 保留 Material3 Ripple 涟漪 | 圆角 8dp，M3 标准 | Pixel 原生 |
 *
 * ## OEM 接入规范（强制）
 *
 * **禁止**直接使用 `androidx.compose.material3.Button`，
 * **必须**使用 [MorphButton]，以保证 ROM 级的交互和视觉统一。
 *
 * ## 使用方式
 *
 * ```kotlin
 * MorphTheme {
 *     MorphButton(
 *         text = "确认",
 *         onClick = { /* 处理点击 */ }
 *     )
 * }
 * ```
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param enabled 是否启用
 * @param interactionMode 交互模式，默认从 [LocalMorphInteractionMode] 读取
 *
 * @see MorphComposeTheme
 * @see MorphTokens
 */
@Composable
fun MorphButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionMode: MorphInteractionMode = LocalMorphInteractionMode.current
) {
    val colors = LocalMorphColors.current
    val shape = LocalMorphShape.current

    when (interactionMode) {
        MorphInteractionMode.IOS -> {
            IosButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = colors,
                cornerRadius = shape.cornerRadiusButton
            )
        }
        MorphInteractionMode.MATERIAL -> {
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
// iOS 风格按钮 — 按压变色，无涟漪
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * iOS 风格按钮实现。
 *
 * 核心策略：
 * 1. 使用 [Surface] 的 clickable 变体作为容器，设置圆角和零阴影
 * 2. 通过 [Animatable] 驱动按压遮罩透明度
 * 3. 按压时修改背景色的 Alpha 值实现按压变暗效果
 * 4. 完全不使用 Ripple
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
    val scope = rememberCoroutineScope()
    val pressAlpha = remember { Animatable(0f) }

    // 根据启用状态决定颜色
    val backgroundColor = if (enabled) {
        colors.primary
    } else {
        colors.primary.copy(alpha = MorphTokens.DisabledAlpha)
    }
    val contentColor = if (enabled) {
        colors.onPrimary
    } else {
        colors.onPrimary.copy(alpha = MorphTokens.DisabledAlpha)
    }

    // 按压态颜色：将白色遮罩混合到主色上，模拟 iOS 按压变亮效果
    val displayColor = if (enabled) {
        val overlayAlpha = pressAlpha.value * MorphTokens.PressOverlayMaxAlpha
        backgroundColor.lerp(Color.White, overlayAlpha)
    } else {
        backgroundColor
    }

    val buttonShape = RoundedCornerShape(cornerRadius.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(buttonShape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // iOS 模式：无涟漪
                        role = Role.Button,
                        onClick = {
                            // 按压进入动画
                            scope.launch {
                                pressAlpha.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(MorphTokens.PressInDuration.toInt())
                                )
                            }
                            onClick()
                        }
                    )
                } else {
                    Modifier.semantics { role = Role.Button }
                }
            ),
        shape = buttonShape,
        color = displayColor,
        shadowElevation = 0.dp
    ) {
        ProvideTextStyle(
            value = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
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

// ═════════════════════════════════════════════════════════════════════════════════
// Material 风格按钮 — 保留 Ripple 涟漪
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * Material 风格按钮实现。
 *
 * 核心策略：
 * 1. 使用 Material3 的 [androidx.compose.material3.Button] 作为基础
 * 2. 覆盖颜色和圆角为 MorphKit Token 值
 * 3. 完全保留系统 Ripple 涟漪行为，不干预
 */
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
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.primary.copy(alpha = MorphTokens.DisabledAlpha),
            disabledContentColor = colors.onPrimary.copy(alpha = MorphTokens.DisabledAlpha)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// 工具函数
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * 简单的颜色线性插值。
 *
 * 用于 iOS 按压态遮罩混合：将 [this] 颜色向 [target] 颜色混合 [fraction] 比例。
 */
private fun Color.lerp(target: Color, fraction: Float): Color {
    return Color(
        red = red + (target.red - red) * fraction,
        green = green + (target.green - green) * fraction,
        blue = blue + (target.blue - blue) * fraction,
        alpha = alpha + (target.alpha - alpha) * fraction
    )
}
