package com.morphkit.widget.text

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.morphkit.R
import com.morphkit.core.InteractionMode
import com.morphkit.theme.MorphColors
import com.morphkit.theme.MorphTheme
import com.morphkit.theme.MorphTokens

/**
 * MorphKit iOS 字重规范 TextView。
 *
 * 基于 [AppCompatTextView]，还原 iOS 文字的视觉权重与颜色规范：
 *
 * ## 排版规范
 * - 拦截 XML `textStyle` 属性，强制重新映射到 iOS 对齐的字重
 * - `bold` → [Typeface.BOLD]（确保粗体饱满，对齐 iOS Bold 视觉）
 * - `normal` → [MorphTheme.FontWeight.MEDIUM]（正文补偿，对齐 iOS Regular 视觉）
 * - `italic` → [Typeface.ITALIC] + MEDIUM（保持字重不降级）
 * - `bold|italic` → [Typeface.BOLD_ITALIC]
 *
 * ## 颜色规范
 * - 默认文字色：纯黑（浅色）/ 纯白（深色），即 iOS `labelColor`
 * - 次级文字色：iOS `tertiaryLabelColor`（#8A8A8E / #636366），
 *   通过 [MorphTheme.morphColorOnSurfaceVariant] 获取
 *
 * ## 使用方式
 *
 * ```kotlin
 * MorphKit.init(this) {
 *     groupReplace(listOf("TextView", "AppCompatTextView")) { ctx, attrs ->
 *         MorphTextView(ctx, attrs)
 *     }
 * }
 * ```
 *
 * @see MorphTheme.morphColorOnSurface 主文字颜色
 * @see MorphTheme.morphColorOnSurfaceVariant 次级文字颜色
 * @see MorphTheme.typography 排版令牌
 */
class MorphTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphTextViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // ═══════════════════════════════════════════════════════════════════════
    // 内部状态
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 交互模式，从 XML 属性 `morphInteractionMode` 读取。
     *
     * - [InteractionMode.IOS]（默认）：启用 iOS 字重补偿和颜色规范
     * - [InteractionMode.MATERIAL]：跳过 iOS 特定处理，使用 M3 默认文字样式
     */
    val interactionMode: InteractionMode

    /**
     * 是否为次级文字模式。
     *
     * 设置为 `true` 时，文字颜色自动切换为 [MorphTheme.morphColorOnSurfaceVariant]，
     * 适用于副标题、说明文字、时间戳等 iOS 风格的灰色辅助信息。
     *
     * 可在 XML 中通过代码设置：
     * ```kotlin
     * morphTextView.isSecondaryText = true
     * ```
     */
    var isSecondaryText: Boolean = false
        set(value) {
            field = value
            applyTextColor()
        }

    /**
     * 是否为三级文字模式。
     *
     * 设置为 `true` 时，文字颜色自动切换为 [MorphTheme.morphColorOnSurfaceVariant]（带透明度衰减），
     * 颜色比 [isSecondaryText] 更淡，适用于占位符、禁用态文字。
     */
    var isTertiaryText: Boolean = false
        set(value) {
            field = value
            applyTextColor()
        }

    // ═══════════════════════════════════════════════════════════════════════
    // 初始化
    // ═══════════════════════════════════════════════════════════════════════

    init {
        // ── 读取 XML 属性 ──
        interactionMode = if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MorphTextView, defStyleAttr, 0)
            try {
                val modeValue = a.getInt(R.styleable.MorphTextView_morphInteractionMode, 0)
                if (modeValue == 1) InteractionMode.MATERIAL else InteractionMode.IOS
            } finally {
                a.recycle()
            }
        } else {
            InteractionMode.IOS
        }

        if (interactionMode == InteractionMode.IOS) {
            // ── 拦截并重新映射 textStyle ──
            // Android XML 中 android:textStyle 的值在 super 构造时已被应用，
            // 此处读取当前 typeface.style，按 iOS 规范重新映射
            remapTextStyle()

            // ── 应用默认文字颜色 ──
            applyTextColor()
        }
        // MATERIAL mode: skip iOS-specific textStyle remapping and text color,
        // use default M3 text styling from the theme
    }

    // ═══════════════════════════════════════════════════════════════════════
    // textStyle 重映射
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 拦截并重新映射 Android textStyle 到 iOS 对齐的字重。
     *
     * 映射规则：
     *
     * | XML textStyle | Android 原始行为 | MorphKit 重映射 | 原因 |
     * |---------------|-----------------|-----------------|------|
     * | normal (0)    | Typeface.NORMAL | sans-serif-medium (MEDIUM 500) | Roboto Regular 偏细，需补偿至 Medium 对齐 iOS Regular |
     * | bold (1)      | Typeface.BOLD   | sans-serif-bold (BOLD 700) | 确保 Bold 饱满，对齐 iOS Bold |
     * | italic (2)    | Typeface.ITALIC | sans-serif-medium + ITALIC | 斜体仍需 MEDIUM 字重补偿 |
     * | bold\|italic (3) | Typeface.BOLD_ITALIC | sans-serif-bold + ITALIC | 粗斜体保持 BOLD 权重 |
     *
     * **为什么需要重映射？**
     *
     * Android 默认 `textStyle=normal` 使用 Roboto Regular (400)，
     * 在视觉上比 iOS SF Pro Regular 偏细约一个字重级别。
     * 为在 Android 上还原 iOS 的文字视觉节奏，
     * 必须将 normal 提升至 Medium (500)，即 [MorphTheme.FontWeight.MEDIUM]。
     *
     * 而 `textStyle=bold` 使用 Roboto Bold (700) 已足够饱满，
     * 与 iOS SF Pro Bold 视觉上对齐，无需额外补偿。
     */
    private fun remapTextStyle() {
        val currentStyle = typeface?.style ?: Typeface.NORMAL

        val remappedTypeface = when (currentStyle) {
            Typeface.NORMAL -> {
                // normal → MEDIUM 500（iOS Regular 补偿）
                MorphTheme.FontWeight.MEDIUM.toTypeface()
            }

            Typeface.BOLD -> {
                // bold → 保持 BOLD 700（iOS Bold 对齐）
                // 使用 Typeface.defaultFromStyle 确保粗体饱满
                Typeface.defaultFromStyle(Typeface.BOLD)
            }

            Typeface.ITALIC -> {
                // italic → MEDIUM + ITALIC（保持字重补偿 + 斜体）
                val mediumTf = MorphTheme.FontWeight.MEDIUM.toTypeface()
                Typeface.create(mediumTf, Typeface.ITALIC)
            }

            Typeface.BOLD_ITALIC -> {
                // bold|italic → BOLD + ITALIC
                Typeface.defaultFromStyle(Typeface.BOLD_ITALIC)
            }

            else -> {
                // 其他未知 style，降级为 MEDIUM
                MorphTheme.FontWeight.MEDIUM.toTypeface()
            }
        }

        typeface = remappedTypeface
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 文字颜色
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 根据当前文字级别应用颜色。
     *
     * | 级别 | 颜色 | iOS 对应 | 用途 |
     * |------|------|----------|------|
     * | 默认 | [MorphTheme.morphColorOnSurface] | UIColor.label | 主文字、标题 |
     * | [isSecondaryText] | [MorphTheme.morphColorOnSurfaceVariant] | UIColor.secondaryLabel | 副标题 |
     * | [isTertiaryText] | [MorphTheme.morphColorOnSurfaceVariant] + 55% 透明度 | UIColor.tertiaryLabel | 占位符、时间戳 |
     */
    private fun applyTextColor() {
        val color: Int = when {
            isTertiaryText -> MorphColors.adjustAlpha(MorphTheme.morphColorOnSurfaceVariant(context), MorphTokens.tertiaryTextAlpha)
            isSecondaryText -> MorphTheme.morphColorOnSurfaceVariant(context)
            else -> MorphTheme.morphColorOnSurface(context)
        }
        setTextColor(color)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // setTypeface 拦截
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 拦截外部 setTypeface 调用。
     *
     * 当外部代码（如 DataBinding、代码设置）调用 `setTypeface(tf, style)` 时，
     * 确保 style 仍按 iOS 规范重映射，避免外部传入的 style 覆盖我们的字重补偿。
     *
     * @param tf     外部设置的 Typeface
     * @param style  外部设置的 style 常量
     */
    override fun setTypeface(tf: Typeface?, style: Int) {
        // 优先使用外部传入的 tf，但 style 仍走重映射逻辑
        if (tf != null) {
            val remappedStyle = when (style) {
                Typeface.NORMAL -> Typeface.NORMAL  // 外部 tf 已自带字重，不再二次补偿
                Typeface.BOLD -> Typeface.BOLD
                Typeface.ITALIC -> Typeface.ITALIC
                Typeface.BOLD_ITALIC -> Typeface.BOLD_ITALIC
                else -> style
            }
            super.setTypeface(tf, remappedStyle)
        } else {
            // tf 为 null，使用 style 重映射
            super.setTypeface(null, style)
            remapTextStyle()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 生命周期
    // ═══════════════════════════════════════════════════════════════════════

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 适配暗黑模式切换（仅 iOS 模式）
        if (interactionMode == InteractionMode.IOS) {
            applyTextColor()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Activity 不重建时（configChanges 包含 uiMode），手动刷新文字颜色（仅 iOS 模式）
        if (interactionMode == InteractionMode.IOS) {
            applyTextColor()
        }
    }
}
