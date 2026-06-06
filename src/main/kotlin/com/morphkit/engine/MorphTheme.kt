package com.morphkit.engine

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.Px

/**
 * MorphKit 设计系统基座。
 *
 * 提取 iOS 17 最新设计语言的核心视觉特征，供所有 Morph 控件统一消费。
 * 涵盖三大体系：
 *
 * 1. **颜色体系**：模拟 iOS 系统语义色，自动适配暗黑模式
 * 2. **形状体系**：模拟 iOS 连续性圆角（Continuity Corners），用统一大圆角近似
 * 3. **排版体系**：模拟 SF Pro 视觉权重，将 Android Roboto 的字重对齐到 iOS 风格
 *
 * ## 设计哲学
 *
 * iOS 17 的视觉语言有三个核心特征：
 * - **纯净对比**：系统背景纯白/纯黑，无灰色杂质；分组背景用极浅灰做层次分离
 * - **连续曲率**：所有圆角使用连续性曲率（Squircle），视觉上比标准圆角更柔和
 * - **权重对齐**：SF Pro 的字重在视觉上比同号数 Roboto 偏细，需要提升一级补偿
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
 *         setTextColor(MorphTheme.tintColor(context))
 *         textSize = MorphTheme.typography.body.fontSize
 *         typeface = MorphTheme.typography.body.weight.toTypeface()
 *     }
 * }
 * ```
 */
object MorphTheme {

    // ═══════════════════════════════════════════════════════════════════════
    // 颜色体系 — iOS 系统语义色
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * iOS 系统主背景色。
     *
     * - 浅色模式：纯白 `#FFFFFF`（极简无灰色杂质）
     * - 深色模式：纯黑 `#000000`（OLED 纯黑，零灰度偏移）
     *
     * 对应 iOS UIColor.systemBackground。
     * 适用于页面主背景、全屏容器。
     *
     * @param context 上下文，用于读取当前系统 Configuration
     * @return 颜色值
     */
    @ColorInt
    fun systemBackground(context: Context): Int {
        return if (isDarkMode(context)) COLOR_DARK_PURE_BLACK else COLOR_LIGHT_PURE_WHITE
    }

    /**
     * iOS 分组背景灰。
     *
     * - 浅色模式：`#F2F2F7`（iOS 标志性分组背景灰，略带蓝调冷灰）
     * - 深色模式：`#1C1C1E`（深灰而非纯黑，提供层次分离）
     *
     * 对应 iOS UIColor.secondarySystemBackground。
     * 适用于分组列表背景、卡片容器背景、输入框底色。
     *
     * @param context 上下文
     * @return 颜色值
     */
    @ColorInt
    fun secondarySystemBackground(context: Context): Int {
        return if (isDarkMode(context)) COLOR_DARK_SECONDARY_BG else COLOR_LIGHT_SECONDARY_BG
    }

    /**
     * iOS 标志性高亮蓝。
     *
     * - 浅色模式：`#007AFF`（iOS 系统蓝，用于按钮/链接/开关/Tab 高亮）
     * - 深色模式：`#0A84FF`（深色模式下略微提亮，保证暗背景上的可读性）
     *
     * 对应 iOS UIColor.systemBlue / tintColor。
     * 适用于所有可交互元素的高亮态、选中态、链接色。
     *
     * @param context 上下文
     * @return 颜色值
     */
    @ColorInt
    fun tintColor(context: Context): Int {
        return if (isDarkMode(context)) COLOR_DARK_TINT else COLOR_LIGHT_TINT
    }

    /**
     * iOS 主文字颜色。
     *
     * - 浅色模式：纯黑 `#000000`
     * - 深色模式：纯白 `#FFFFFF`
     *
     * 对应 iOS UIColor.label。
     * 适用于所有主文字、标题、正文。
     *
     * @param context 上下文
     * @return 颜色值
     */
    @ColorInt
    fun labelColor(context: Context): Int {
        return if (isDarkMode(context)) COLOR_LIGHT_PURE_WHITE else COLOR_DARK_PURE_BLACK
    }

    /**
     * iOS 次级文字颜色。
     *
     * - 浅色模式：`#3C3C43`（带 60% 不透明度的深灰）
     * - 深色模式：`#EBEBF5`（带 60% 不透明度的浅灰）
     *
     * 对应 iOS UIColor.secondaryLabel。
     * 适用于副标题、辅助说明。
     *
     * @param context 上下文
     * @return 颜色值
     */
    @ColorInt
    fun secondaryLabelColor(context: Context): Int {
        return if (isDarkMode(context)) COLOR_DARK_SECONDARY_LABEL else COLOR_LIGHT_SECONDARY_LABEL
    }

    /**
     * iOS 三级文字颜色。
     *
     * - 浅色模式：`#8A8A8E`（iOS 标志性淡灰）
     * - 深色模式：`#636366`（深灰模式下更深的灰）
     *
     * 对应 iOS UIColor.tertiaryLabel。
     * 适用于占位符、时间戳、最弱层级的辅助文字。
     *
     * @param context 上下文
     * @return 颜色值
     */
    @ColorInt
    fun tertiaryLabelColor(context: Context): Int {
        return if (isDarkMode(context)) COLOR_DARK_TERTIARY_LABEL else COLOR_LIGHT_TERTIARY_LABEL
    }

    /**
     * iOS 分组分割线颜色。
     *
     * - 浅色模式：`#C6C6C8`（极浅灰，iOS 表格分割线标准色）
     * - 深色模式：`#38383A`（深灰，保证暗背景下的微妙存在感）
     *
     * 对应 iOS UIColor.separator / opaqueSeparator。
     * 适用于列表分割线、卡片边框、分组边界线。
     *
     * 设计要点：iOS 分割线的核心特征是「存在但克制」——
     * 用户能感知到分组边界的存在，但分割线本身不抢夺视觉注意力。
     * 这与 Material Design 的强对比分割线风格截然不同。
     *
     * @param context 上下文
     * @return 颜色值
     */
    @ColorInt
    fun separatorColor(context: Context): Int {
        return if (isDarkMode(context)) COLOR_DARK_SEPARATOR else COLOR_LIGHT_SEPARATOR
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

    // ── 颜色常量 ──

    /** 浅色模式：纯白背景 #FFFFFF */
    private val COLOR_LIGHT_PURE_WHITE = 0xFFFFFFFFL.toInt()

    /** 深色模式：纯黑背景 #000000（OLED 纯黑） */
    private val COLOR_DARK_PURE_BLACK = 0xFF000000L.toInt()

    /** 浅色模式：iOS 分组背景灰 #F2F2F7 */
    private val COLOR_LIGHT_SECONDARY_BG = 0xFFF2F2F7L.toInt()

    /** 深色模式：iOS 分组深灰 #1C1C1E */
    private val COLOR_DARK_SECONDARY_BG = 0xFF1C1C1EL.toInt()

    /** 浅色模式：iOS 系统蓝 #007AFF */
    private val COLOR_LIGHT_TINT = 0xFF007AFFL.toInt()

    /** 深色模式：iOS 系统蓝（提亮）#0A84FF */
    private val COLOR_DARK_TINT = 0xFF0A84FFL.toInt()

    /** 浅色模式：iOS 次级文字 #3C3C43（60% 不透明度） */
    private val COLOR_LIGHT_SECONDARY_LABEL = 0x993C3C43L.toInt()

    /** 深色模式：iOS 次级文字 #EBEBF5（60% 不透明度） */
    private val COLOR_DARK_SECONDARY_LABEL = 0x99EBEBF5L.toInt()

    /** 浅色模式：iOS 三级文字 #8A8A8E */
    private val COLOR_LIGHT_TERTIARY_LABEL = 0xFF8A8A8EL.toInt()

    /** 深色模式：iOS 三级文字 #636366 */
    private val COLOR_DARK_TERTIARY_LABEL = 0xFF636366L.toInt()

    /** 浅色模式：iOS 分组分割线 #C6C6C8 */
    private val COLOR_LIGHT_SEPARATOR = 0xFFC6C6C8L.toInt()

    /** 深色模式：iOS 分组分割线 #38383A */
    private val COLOR_DARK_SEPARATOR = 0xFF38383AL.toInt()

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
