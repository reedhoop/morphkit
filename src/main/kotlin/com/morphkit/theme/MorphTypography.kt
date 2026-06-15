package com.morphkit.theme

/**
 * MorphKit View 体系排版系统。
 *
 * 定义 iOS SF Pro 视觉权重对齐的排版令牌，所有字号和字重
 * 与 iOS Human Interface Guidelines 的文字层级一一对应。
 *
 * ## 使用方式
 *
 * ```kotlin
 * val typo = MorphTypography.body
 * textView.textSize = typo.fontSize
 * textView.typeface = typo.weight.toTypeface()
 * ```
 *
 * ## 排版层级
 *
 * | 令牌 | 字号 (sp) | 字重 | iOS 对应 |
 * |------|----------|------|---------|
 * | largeTitle | 34 | EXTRA_BOLD (800) | Large Title |
 * | title1 | 28 | EXTRA_BOLD (800) | Title 1 |
 * | title2 | 22 | BOLD (700) | Title 2 |
 * | title3 | 20 | BOLD (700) | Title 3 |
 * | headline | 17 | SEMI_BOLD (600) | Headline |
 * | body | 17 | MEDIUM (500) | Body |
 * | callout | 16 | MEDIUM (500) | Callout |
 * | subheadline | 15 | MEDIUM (500) | Subheadline |
 * | footnote | 13 | MEDIUM (500) | Footnote |
 * | caption1 | 12 | MEDIUM (500) | Caption 1 |
 * | caption2 | 11 | MEDIUM (500) | Caption 2 |
 *
 * @see FontWeight 字重枚举及 Typeface 转换
 * @see TextStyle 字号 + 字重组合
 */
object MorphTypography {
    val largeTitle = TextStyle(fontSize = MorphTokens.Typography.fontSizeLargeTitle, weight = FontWeight.EXTRA_BOLD)
    val title1 = TextStyle(fontSize = MorphTokens.Typography.fontSizeTitle1, weight = FontWeight.EXTRA_BOLD)
    val title2 = TextStyle(fontSize = MorphTokens.Typography.fontSizeTitle2, weight = FontWeight.BOLD)
    val title3 = TextStyle(fontSize = MorphTokens.Typography.fontSizeTitle3, weight = FontWeight.BOLD)
    val headline = TextStyle(fontSize = MorphTokens.Typography.fontSizeHeadline, weight = FontWeight.SEMI_BOLD)
    val body = TextStyle(fontSize = MorphTokens.Typography.fontSizeBody, weight = FontWeight.MEDIUM)
    val callout = TextStyle(fontSize = MorphTokens.Typography.fontSizeCallout, weight = FontWeight.MEDIUM)
    val subheadline = TextStyle(fontSize = MorphTokens.Typography.fontSizeSubheadline, weight = FontWeight.MEDIUM)
    val footnote = TextStyle(fontSize = MorphTokens.Typography.fontSizeFootnote, weight = FontWeight.MEDIUM)
    val caption1 = TextStyle(fontSize = MorphTokens.Typography.fontSizeCaption1, weight = FontWeight.MEDIUM)
    val caption2 = TextStyle(fontSize = MorphTokens.Typography.fontSizeCaption2, weight = FontWeight.MEDIUM)
}

/**
 * 字号 + 字重组合。
 *
 * @property fontSize 字号（sp）
 * @property weight 字重
 */
data class TextStyle(val fontSize: Float, val weight: FontWeight)

/**
 * MorphKit 字重枚举 — iOS SF Pro 视觉权重对齐。
 *
 * Android Roboto Regular (400) 在视觉上比 iOS SF Pro Regular 偏细约一个字重级别，
 * 因此 normal 文本需补偿至 [MEDIUM] (500) 以对齐 iOS Regular 的视觉节奏。
 *
 * | 字重 | 数值 | Android Typeface | 用途 |
 * |------|------|-----------------|------|
 * | MEDIUM | 500 | sans-serif-medium | 正文补偿 |
 * | SEMI_BOLD | 600 | sans-serif-semibold | 标题 |
 * | BOLD | 700 | sans-serif BOLD | 强调标题 |
 * | EXTRA_BOLD | 800 | sans-serif-black | 大标题 |
 */
enum class FontWeight(val weight: Int) {
    MEDIUM(500),
    SEMI_BOLD(600),
    BOLD(700),
    EXTRA_BOLD(800);

    fun toTypeface(): android.graphics.Typeface = when (this) {
        MEDIUM -> android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        SEMI_BOLD -> android.graphics.Typeface.create("sans-serif-semibold", android.graphics.Typeface.NORMAL)
        BOLD -> android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        EXTRA_BOLD -> android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
    }
}
