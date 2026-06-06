package com.morphkit.engine

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.google.android.material.color.MaterialColors

/**
 * MorphKit 设计系统基座（M3 重构版）。
 *
 * 彻底废弃硬编码色彩，完美集成 Material Design 3 (M3) 语义色系统，
 * 同时保留 iOS 极简交互视觉风格。核心变更：
 *
 * 1. **颜色体系**：所有颜色从 M3 语义属性解析，支持 Android 12+ 动态壁纸取色
 * 2. **形状体系**：保留 iOS 风格圆角定义，通过主题属性可配置
 * 3. **排版体系**：保留 SF Pro 视觉权重对齐，独立于颜色系统
 *
 * ## iOS ↔ M3 语义映射
 *
 * | iOS 语义          | M3 语义属性                     | 用途                     |
 * |------------------|--------------------------------|--------------------------|
 * | tintColor        | colorPrimary                   | 主色、按钮、链接         |
 * | systemBackground | colorSurface / colorBackground | 页面背景                 |
 * | secondarySystemBackground | colorSurfaceVariant | 分组背景、卡片           |
 * | separator        | colorOutlineVariant            | 分割线、边框             |
 * | label            | colorOnSurface                 | 主文字                   |
 * | secondaryLabel   | colorOnSurfaceVariant          | 次级文字                 |
 *
 * ## 使用方式
 *
 * ```kotlin
 * class MorphButton @JvmOverloads constructor(
 *     context: Context,
 *     attrs: AttributeSet? = null,
 *     defStyleAttr: Int = 0
 * ) : AppCompatButton(context, attrs, defStyleAttr) {
 *
 *     init {
 *         background = MorphTheme.createShapeDrawable(context, MorphTheme.cornerMedium)
 *         setTextColor(context.morphColorOnPrimary())
 *         textSize = MorphTheme.typography.body.fontSize
 *         typeface = MorphTheme.typography.body.weight.toTypeface()
 *     }
 * }
 * ```
 */
object MorphTheme {

    // ═══════════════════════════════════════════════════════════════════════
    // 颜色体系 — M3 语义色解析（废弃硬编码）
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 获取 M3 主色（对应 iOS tintColor）。
     *
     * 使用 MaterialColors.getColor() 从主题解析，支持 Android 12+ 动态壁纸取色。
     * 对应 M3 attr: com.google.android.material.R.attr.colorPrimary
     *
     * @receiver Context 上下文，用于读取主题
     * @return 颜色值
     */
    @ColorInt
    fun Context.morphColorPrimary(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, 0)
    }

    /**
     * 获取 M3 主色容器色。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorPrimaryContainer
     */
    @ColorInt
    fun Context.morphColorPrimaryContainer(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimaryContainer, 0)
    }

    /**
     * 获取 M3 主色上的文字/图标色。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorOnPrimary
     */
    @ColorInt
    fun Context.morphColorOnPrimary(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary, 0)
    }

    /**
     * 获取 M3 主色容器上的文字/图标色。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorOnPrimaryContainer
     */
    @ColorInt
    fun Context.morphColorOnPrimaryContainer(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimaryContainer, 0)
    }

    /**
     * 获取 M3 表面色（对应 iOS systemBackground）。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorSurface
     */
    @ColorInt
    fun Context.morphColorSurface(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, 0)
    }

    /**
     * 获取 M3 表面变体色（对应 iOS secondarySystemBackground）。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorSurfaceVariant
     */
    @ColorInt
    fun Context.morphColorSurfaceVariant(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceVariant, 0)
    }

    /**
     * 获取 M3 表面色上的文字/图标色（对应 iOS label）。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorOnSurface
     */
    @ColorInt
    fun Context.morphColorOnSurface(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, 0)
    }

    /**
     * 获取 M3 表面变体色上的文字/图标色（对应 iOS secondaryLabel）。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorOnSurfaceVariant
     */
    @ColorInt
    fun Context.morphColorOnSurfaceVariant(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, 0)
    }

    /**
     * 获取 M3 轮廓变体色（对应 iOS separator）。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorOutlineVariant
     */
    @ColorInt
    fun Context.morphColorOutlineVariant(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant, 0)
    }

    /**
     * 获取 M3 背景色。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorBackground
     */
    @ColorInt
    fun Context.morphColorBackground(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorBackground, 0)
    }

    /**
     * 获取 M3 背景色上的文字/图标色。
     *
     * 对应 M3 attr: com.google.android.material.R.attr.colorOnBackground
     */
    @ColorInt
    fun Context.morphColorOnBackground(): Int {
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnBackground, 0)
    }

    /**
     * 通用主题属性颜色解析方法。
     *
     * @receiver Context 上下文
     * @param attr 主题属性资源 ID
     * @return 颜色值
     */
    @ColorInt
    fun Context.morphColor(@AttrRes attr: Int): Int {
        return MaterialColors.getColor(this, attr, 0)
    }

    /**
     * 创建带状态的颜色列表（ColorStateList）。
     *
     * 基于主色，自动生成不同状态下的颜色变体：
     * - 正常态：原始颜色
     * - 按下态：叠加 20% 遮罩（深色模式叠加白色，浅色模式叠加黑色）
     * - 禁用态：降低不透明度
     *
     * @param baseColor 基础颜色
     * @param isDarkMode 是否为暗黑模式
     * @return ColorStateList 实例
     */
    fun createColorStateList(baseColor: Int, isDarkMode: Boolean): android.content.res.ColorStateList {
        val pressedColor = overlayColor(baseColor, if (isDarkMode) Color.WHITE else Color.BLACK, 0.2f)
        val disabledColor = adjustAlpha(baseColor, 0.38f)

        return android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf()
            ),
            intArrayOf(
                disabledColor,
                pressedColor,
                baseColor
            )
        )
    }

    /**
     * 给颜色叠加遮罩。
     *
     * @param baseColor 基础颜色
     * @param overlayColor 遮罩颜色
     * @param alpha 遮罩透明度 [0f, 1f]
     * @return 叠加后的颜色
     */
    fun overlayColor(baseColor: Int, overlayColor: Int, alpha: Float): Int {
        val baseA = Color.alpha(baseColor)
        val baseR = Color.red(baseColor)
        val baseG = Color.green(baseColor)
        val baseB = Color.blue(baseColor)

        val overlayA = (Color.alpha(overlayColor) * alpha).toInt()
        val overlayR = Color.red(overlayColor)
        val overlayG = Color.green(overlayColor)
        val overlayB = Color.blue(overlayColor)

        val ratio = overlayA / 255f
        val inverseRatio = 1f - ratio

        val newA = baseA
        val newR = (baseR * inverseRatio + overlayR * ratio).toInt()
        val newG = (baseG * inverseRatio + overlayG * ratio).toInt()
        val newB = (baseB * inverseRatio + overlayB * ratio).toInt()

        return Color.argb(newA, newR.coerceIn(0, 255), newG.coerceIn(0, 255), newB.coerceIn(0, 255))
    }

    /**
     * 调整颜色的不透明度。
     *
     * @param color 原始颜色
     * @param alpha 新的不透明度 [0f, 1f]
     * @return 调整后的颜色
     */
    fun adjustAlpha(color: Int, alpha: Float): Int {
        val newAlpha = (Color.alpha(color) * alpha).toInt().coerceIn(0, 255)
        return Color.argb(newAlpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    /**
     * 判断当前是否为暗黑模式。
     *
     * 读取系统 [Configuration.uiMode] 中的 night 模式标志位，
     * 兼容 Android 10+ 的系统级暗黑模式切换。
     *
     * @param context 上下文
     * @return `true` 表示当前处于暗黑模式
     */
    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 形状体系 — iOS 连续性圆角（Continuity Corners 近似）
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * iOS 风格圆角大小定义。
     *
     * iOS 的连续性圆角（Continuity Corners / Squircle）使用连续曲率，
     * 在圆角起点与直线交接处无切线突变，视觉上比标准圆角更柔和。
     * Android 不原生支持连续曲率，此处用统一大圆角近似：
     *
     * | 级别 | 值 | iOS 参照 | 典型用途 |
     * |------|-----|----------|----------|
     * | [cornerSmall] | 8dp | 小组件内边距圆角 | 标签、Badge、小按钮 |
     * | [cornerMedium] | 12dp | 标准按钮/卡片 | 常规按钮、列表项、输入框 |
     * | [cornerLarge] | 16dp | 全宽大卡片/弹窗 | 全宽卡片、Bottom Sheet、Dialog |
     * | [cornerFull] | 50% | 胶囊形状 | Pill 按钮、搜索栏、开关 |
     *
     * **注意**：[cornerFull] 返回 [Int.MAX_VALUE]，在 `GradientDrawable` 中
     * 会被自动裁剪为 View 短边的 50%，实现胶囊效果。
     */

    /** 小圆角 8dp — 标签、Badge、小按钮 */
    @Px
    val cornerSmall: Int = 8.dp

    /** 中圆角 12dp — 标准按钮、列表项、输入框 */
    @Px
    val cornerMedium: Int = 12.dp

    /** 大圆角 16dp — 全宽卡片、Bottom Sheet、Dialog */
    @Px
    val cornerLarge: Int = 16.dp

    /**
     * 全圆角（胶囊形状）。
     *
     * 返回 [Int.MAX_VALUE] 而非具体像素值，
     * 因为 Android 的 `GradientDrawable.setCornerRadius()` 在值超过 View 短边一半时
     * 会自动裁剪为胶囊形。使用 [Int.MAX_VALUE] 保证任何尺寸的 View
     * 都能正确渲染为胶囊/药丸形状。
     */
    @Px
    val cornerFull: Int = Int.MAX_VALUE

    // ═══════════════════════════════════════════════════════════════════════
    // 排版体系 — SF Pro 视觉权重对齐
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * iOS 风格排版定义。
     *
     * iOS 使用 SF Pro 字体，其视觉权重比同号数 Android Roboto 偏细。
     * 为在 Android 上还原 iOS 视觉节奏，需将 Roboto 字重提升一级：
     *
     * | iOS SF Pro | Android Roboto 补偿 | 说明 |
     * |------------|---------------------|------|
     * | Regular    | Medium (500)        | 正文文本，Roboto Regular 视觉偏细，需提升至 Medium |
     * | Medium     | SemiBold (600)      | 列表标题、小节标题 |
     * | Semibold   | Bold (700)          | 页面大标题、强调文本 |
     * | Bold       | ExtraBold (800)     | 超大标题、Hero 区域 |
     *
     * 字号严格对齐 iOS 17 Dynamic Type 默认基准（不随用户字号缩放的固定基准值）：
     * - largeTitle: 34sp — 导航栏大标题
     * - title1: 28sp — 页面主标题
     * - title2: 22sp — 区块标题
     * - title3: 20sp — 子区块标题
     * - headline: 17sp (SemiBold) — 列表标题
     * - body: 17sp (Medium) — 正文
     * - callout: 16sp (Medium) — 辅助说明
     * - subheadline: 15sp (Medium) — 次级说明
     * - footnote: 13sp (Medium) — 脚注
     * - caption1: 12sp (Medium) — 图注
     * - caption2: 11sp (Medium) — 最小标注
     */
    val typography = MorphTypography()

    /**
     * 排版令牌类。
     *
     * 每个 [TextStyle] 包含 [fontSize]（sp）和 [weight]（[FontWeight]），
     * 可直接用于 TextView 的 textSize 与 typeface 设置。
     *
     * ```kotlin
     * val style = MorphTheme.typography.body
     * textView.textSize = style.fontSize
     * textView.typeface = style.weight.toTypeface()
     * ```
     */
    class MorphTypography {

        /** iOS Large Title — 34sp / Bold(700) → Android ExtraBold(800) */
        val largeTitle = TextStyle(fontSize = 34f, weight = FontWeight.EXTRA_BOLD)

        /** iOS Title 1 — 28sp / Bold(700) → Android ExtraBold(800) */
        val title1 = TextStyle(fontSize = 28f, weight = FontWeight.EXTRA_BOLD)

        /** iOS Title 2 — 22sp / Bold(700) → Android Bold(700) */
        val title2 = TextStyle(fontSize = 22f, weight = FontWeight.BOLD)

        /** iOS Title 3 — 20sp / Semibold(600) → Android Bold(700) */
        val title3 = TextStyle(fontSize = 20f, weight = FontWeight.BOLD)

        /** iOS Headline — 17sp / Semibold(600) → Android SemiBold(600) */
        val headline = TextStyle(fontSize = 17f, weight = FontWeight.SEMI_BOLD)

        /** iOS Body — 17sp / Regular(400) → Android Medium(500) */
        val body = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)

        /** iOS Callout — 16sp / Regular(400) → Android Medium(500) */
        val callout = TextStyle(fontSize = 16f, weight = FontWeight.MEDIUM)

        /** iOS Subheadline — 15sp / Regular(400) → Android Medium(500) */
        val subheadline = TextStyle(fontSize = 15f, weight = FontWeight.MEDIUM)

        /** iOS Footnote — 13sp / Regular(400) → Android Medium(500) */
        val footnote = TextStyle(fontSize = 13f, weight = FontWeight.MEDIUM)

        /** iOS Caption 1 — 12sp / Regular(400) → Android Medium(500) */
        val caption1 = TextStyle(fontSize = 12f, weight = FontWeight.MEDIUM)

        /** iOS Caption 2 — 11sp / Regular(400) → Android Medium(500) */
        val caption2 = TextStyle(fontSize = 11f, weight = FontWeight.MEDIUM)
    }

    /**
     * 排版令牌，包含字号与字重。
     *
     * @property fontSize 字号，单位 sp，对齐 iOS Dynamic Type 默认基准
     * @property weight   字重，已从 iOS SF Pro 映射到 Android Roboto 补偿权重
     */
    data class TextStyle(
        val fontSize: Float,
        val weight: FontWeight
    )

    /**
     * 字重枚举，映射 Android Roboto 可用的字重值。
     *
     * Android 的 `TextView.setTypeface(null, style)` 仅支持四个常量
     * (NORMAL/BOLD/ITALIC/BOLD_ITALIC)，无法精确表达 Medium/SemiBold 等。
     * 此处提供 [toTypefaceStyle] 降级映射，同时通过 [weightValue]
     * 供支持字重 API 的控件（如通过 Paint.setTextSize + Typeface.create）使用。
     *
     * | 枚举值 | weight | Android Typeface 降级 |
     * |--------|--------|----------------------|
     * | [MEDIUM] | 500 | NORMAL (400) — 最接近的降级 |
     * | [SEMI_BOLD] | 600 | BOLD (700) — 唯一可用加粗 |
     * | [BOLD] | 700 | BOLD (700) |
     * | [EXTRA_BOLD] | 800 | BOLD (700) — 最接近的降级 |
     */
    enum class FontWeight(val weight: Int) {

        /** Medium 500 — iOS Regular 的 Android 补偿 */
        MEDIUM(500),

        /** SemiBold 600 — iOS Medium 的 Android 补偿 */
        SEMI_BOLD(600),

        /** Bold 700 — 标准 Android Bold */
        BOLD(700),

        /** ExtraBold 800 — iOS Bold 的 Android 补偿 */
        EXTRA_BOLD(800);

        /**
         * 转换为 Android [android.graphics.Typeface] 样式常量。
         *
         * 由于 Android 原生 Typeface 仅支持 NORMAL / BOLD 两档字重，
         * 此方法将 Medium 降级为 NORMAL，SemiBold/ExtraBold 降级为 BOLD。
         *
         * 对于需要精确字重的场景，推荐使用 [android.graphics.Typeface.create]
         * 配合字体资源文件（如 Roboto-Medium.ttf）。
         *
         * @return Android [android.graphics.Typeface] 样式常量
         */
        fun toTypefaceStyle(): Int {
            return when (this) {
                MEDIUM -> android.graphics.Typeface.NORMAL
                SEMI_BOLD -> android.graphics.Typeface.BOLD
                BOLD -> android.graphics.Typeface.BOLD
                EXTRA_BOLD -> android.graphics.Typeface.BOLD
            }
        }

        /**
         * 创建对应的 [android.graphics.Typeface] 实例。
         *
         * 优先尝试通过系统字体族 "sans-serif" 的字重变体创建精确字重 Typeface：
         * - Android 5.0+ 支持 "sans-serif-medium" / "sans-serif-semibold" 等
         * - 降级方案：使用 [android.graphics.Typeface.defaultFromStyle] 创建
         *
         * @return 对应字重的 Typeface 实例
         */
        fun toTypeface(): android.graphics.Typeface {
            return when (this) {
                MEDIUM -> android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                SEMI_BOLD -> android.graphics.Typeface.create("sans-serif-semibold", android.graphics.Typeface.NORMAL)
                BOLD -> android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                EXTRA_BOLD -> android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 扩展属性 — dp ↔ px 转换
    // ═══════════════════════════════════════════════════════════════════════
}

/**
 * dp 值转像素扩展属性。
 *
 * 使用当前屏幕密度将 dp 值转换为 px，适配不同分辨率设备。
 * 此扩展在 Int 上调用，如 `12.dp`、`8.dp`。
 *
 * ```kotlin
 * val padding = 16.dp  // 自动转换为当前设备的像素值
 * view.setPadding(padding, padding, padding, padding)
 * ```
 */
@get:Px
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        android.content.res.Resources.getSystem().displayMetrics
    ).toInt()

/**
 * dp 值转像素扩展属性（Float 版本）。
 *
 * 保留浮点精度，适用于需要亚像素精度的场景（如文字大小、线条宽度）。
 *
 * ```kotlin
 * val strokeWidth = 1.5f.dp  // 保留小数精度
 * paint.strokeWidth = strokeWidth
 * ```
 */
@get:Px
val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        android.content.res.Resources.getSystem().displayMetrics
    )
