package com.morphkit.theme

import androidx.annotation.ColorInt

/**
 * MorphKit 统一设计 Token 层。
 *
 * 定义 MorphKit 设计系统的所有原始设计变量，作为 View 体系和 Compose 体系
 * 的**唯一数据源**。View 体系的 [MorphTheme] 和 Compose 体系的
 * [MorphComposeTheme] 均从此处读取 Token，确保双体系视觉完全一致。
 *
 * ## Token 分层架构
 *
 * ```
 * MorphTokens (原始 Token — 本文件)
 *   ├─ MorphTheme (View 体系) — 转换为 Int / Typeface / dp
 *   └─ MorphComposeTheme (Compose 体系) — 转换为 Color / Typography / Dp
 * ```
 *
 * ## 命名规范
 *
 * 所有 Token 遵循 Kotlin `camelCase` 属性命名，并按类别添加语义前缀：
 *
 * | 类别     | 前缀               | 示例                                    |
 * |---------|--------------------|----------------------------------------|
 * | 颜色     | `color`            | `colorPrimary`, `colorSurface`          |
 * | 形状     | `cornerRadius`     | `cornerRadiusButtonIos`, `cornerRadiusSmall` |
 * | 排版     | `fontSize`         | `fontSizeLargeTitle`, `fontSizeBody`    |
 * | 交互     | 描述性 camelCase    | `pressOverlayMaxAlpha`, `disabledAlpha` |
 *
 * ## iOS ↔ M3 语义映射
 *
 * | iOS 语义                   | M3 语义属性              | Token 名称              |
 * |---------------------------|-------------------------|------------------------|
 * | tintColor                 | colorPrimary            | [colorPrimary]          |
 * | systemBackground          | colorSurface            | [colorSurface]          |
 * | secondarySystemBackground | colorSurfaceVariant     | [colorSurfaceVariant]   |
 * | separator                 | colorOutlineVariant     | [colorOutlineVariant]   |
 * | label                     | colorOnSurface          | [colorOnSurface]        |
 * | secondaryLabel            | colorOnSurfaceVariant   | [colorOnSurfaceVariant] |
 *
 * @see MorphTheme View 体系设计系统
 * @see MorphComposeTheme Compose 体系设计系统
 */
object MorphTokens {

    // ═══════════════════════════════════════════════════════════════════════
    // 颜色 Token — 调色板基色 + M3 语义色
    // ═══════════════════════════════════════════════════════════════════════

    // ── 调色板基色 ──

    /** iOS 蓝 #007AFF / M3 Primary 基色 */
    @ColorInt
    val colorBlue500: Int = 0xFF007AFF.toInt()

    /** iOS 蓝色变体（浅色），暗色模式下 primary 使用 */
    @ColorInt
    val colorBlue100: Int = 0xFFD1E8FF.toInt()

    /** iOS 蓝色变体（深色），暗色模式下 primaryContainer 使用 */
    @ColorInt
    val colorBlue700: Int = 0xFF0055CC.toInt()

    /** iOS 系统红色（destructive 操作 / error） */
    @ColorInt
    val colorRed500: Int = 0xFFFF3B30.toInt()

    /** iOS 系统绿色（成功状态） */
    @ColorInt
    val colorGreen500: Int = 0xFF34C759.toInt()

    /** iOS 系统橙色（警告） */
    @ColorInt
    val colorOrange500: Int = 0xFFFF9500.toInt()

    // ── 语义色（M3 对齐） ──

    /** 主色容器色 */
    @ColorInt
    val colorPrimaryContainer: Int = 0xFFD1E8FF.toInt()

    /** 主色上的文字/图标色 */
    @ColorInt
    val colorOnPrimary: Int = 0xFFFFFFFF.toInt()

    /** 主色容器上的文字/图标色 */
    @ColorInt
    val colorOnPrimaryContainer: Int = 0xFF001D3F.toInt()

    /** 表面色（页面背景） */
    @ColorInt
    val colorSurface: Int = 0xFFF2F2F7.toInt()

    /** 暗色模式表面色 */
    @ColorInt
    val colorSurfaceDark: Int = 0xFF000000.toInt()

    /** 表面变体色（分组背景、卡片） */
    @ColorInt
    val colorSurfaceVariant: Int = 0xFFE5E5EA.toInt()

    /** 暗色模式表面变体色 */
    @ColorInt
    val colorSurfaceVariantDark: Int = 0xFF1C1C1E.toInt()

    /** 表面上的文字/图标色 */
    @ColorInt
    val colorOnSurface: Int = 0xFF1C1C1E.toInt()

    /** 暗色模式表面上的文字/图标色 */
    @ColorInt
    val colorOnSurfaceDark: Int = 0xFFE5E5EA.toInt()

    /** 表面变体上的文字/图标色 */
    @ColorInt
    val colorOnSurfaceVariant: Int = 0xFF3C3C43.toInt()

    /** 暗色模式表面变体上的文字/图标色 */
    @ColorInt
    val colorOnSurfaceVariantDark: Int = 0xFFC7C7CC.toInt()

    /** 轮廓变体色（分割线、边框） */
    @ColorInt
    val colorOutlineVariant: Int = 0xFFC7C7CC.toInt()

    /** 暗色模式轮廓变体色 */
    @ColorInt
    val colorOutlineVariantDark: Int = 0xFF3C3C43.toInt()

    /** 背景色 */
    @ColorInt
    val colorBackground: Int = 0xFFFFFFFF.toInt()

    /** 暗色模式背景色 */
    @ColorInt
    val colorBackgroundDark: Int = 0xFF000000.toInt()

    // ── Secondary 语义色（M3 对齐 — iOS 以灰色系表达次要层级） ──

    /** 次要色 */
    @ColorInt
    val colorSecondary: Int = 0xFF8E8E93.toInt()

    /** 暗色模式次要色 */
    @ColorInt
    val colorSecondaryDark: Int = 0xFFA8A8AD.toInt()

    /** 次要色上的文字/图标色 */
    @ColorInt
    val colorOnSecondary: Int = 0xFFFFFFFF.toInt()

    /** 暗色模式次要色上的文字/图标色 */
    @ColorInt
    val colorOnSecondaryDark: Int = 0xFF1C1C1E.toInt()

    /** 次要色容器色 */
    @ColorInt
    val colorSecondaryContainer: Int = 0xFFE5E5EA.toInt()

    /** 暗色模式次要色容器色 */
    @ColorInt
    val colorSecondaryContainerDark: Int = 0xFF2C2C2E.toInt()

    /** 次要色容器上的文字/图标色 */
    @ColorInt
    val colorOnSecondaryContainer: Int = 0xFF1C1C1E.toInt()

    /** 暗色模式次要色容器上的文字/图标色 */
    @ColorInt
    val colorOnSecondaryContainerDark: Int = 0xFFC7C7CC.toInt()

    // ── Tertiary 语义色（M3 对齐 — iOS 以暖色系表达第三层级） ──

    /** 第三色 */
    @ColorInt
    val colorTertiary: Int = 0xFFFF9500.toInt()

    /** 暗色模式第三色 */
    @ColorInt
    val colorTertiaryDark: Int = 0xFFFFB340.toInt()

    /** 第三色上的文字/图标色 */
    @ColorInt
    val colorOnTertiary: Int = 0xFFFFFFFF.toInt()

    /** 暗色模式第三色上的文字/图标色 */
    @ColorInt
    val colorOnTertiaryDark: Int = 0xFF3A2200.toInt()

    /** 第三色容器色 */
    @ColorInt
    val colorTertiaryContainer: Int = 0xFFFFE8CC.toInt()

    /** 暗色模式第三色容器色 */
    @ColorInt
    val colorTertiaryContainerDark: Int = 0xFF3A2200.toInt()

    /** 第三色容器上的文字/图标色 */
    @ColorInt
    val colorOnTertiaryContainer: Int = 0xFF3A2200.toInt()

    /** 暗色模式第三色容器上的文字/图标色 */
    @ColorInt
    val colorOnTertiaryContainerDark: Int = 0xFFFFDDB3.toInt()

    // ── Error 补充语义色（M3 对齐） ──

    /** 错误色上的文字/图标色 */
    @ColorInt
    val colorOnError: Int = 0xFFFFFFFF.toInt()

    /** 暗色模式错误色上的文字/图标色 */
    @ColorInt
    val colorOnErrorDark: Int = 0xFF3A0000.toInt()

    /** 错误色容器色 */
    @ColorInt
    val colorErrorContainer: Int = 0xFFFFDAD6.toInt()

    /** 暗色模式错误色容器色 */
    @ColorInt
    val colorErrorContainerDark: Int = 0xFF5C0000.toInt()

    /** 错误色容器上的文字/图标色 */
    @ColorInt
    val colorOnErrorContainer: Int = 0xFF410002.toInt()

    /** 暗色模式错误色容器上的文字/图标色 */
    @ColorInt
    val colorOnErrorContainerDark: Int = 0xFFFFDAD6.toInt()

    // ── Outline 补充（M3 对齐） ──

    /** 轮廓色（输入框边框、焦点环） */
    @ColorInt
    val colorOutline: Int = 0xFF8E8E93.toInt()

    /** 暗色模式轮廓色 */
    @ColorInt
    val colorOutlineDark: Int = 0xFF636366.toInt()

    // ── Surface Container 层级色（M3 对齐 — 用于卡片、底栏、抽屉等层级面） ──

    /** 表面暗调（最暗的表面容器） */
    @ColorInt
    val colorSurfaceDim: Int = 0xFFD8D8DD.toInt()

    /** 暗色模式表面暗调 */
    @ColorInt
    val colorSurfaceDimDark: Int = 0xFF000000.toInt()

    /** 表面亮调（最亮的表面容器） */
    @ColorInt
    val colorSurfaceBright: Int = 0xFFF2F2F7.toInt()

    /** 暗色模式表面亮调 */
    @ColorInt
    val colorSurfaceBrightDark: Int = 0xFF2C2C2E.toInt()

    /** 表面容器 — 最低层级 */
    @ColorInt
    val colorSurfaceContainerLowest: Int = 0xFFFFFFFF.toInt()

    /** 暗色模式表面容器 — 最低层级 */
    @ColorInt
    val colorSurfaceContainerLowestDark: Int = 0xFF000000.toInt()

    /** 表面容器 — 低层级 */
    @ColorInt
    val colorSurfaceContainerLow: Int = 0xFFFAFAFF.toInt()

    /** 暗色模式表面容器 — 低层级 */
    @ColorInt
    val colorSurfaceContainerLowDark: Int = 0xFF111113.toInt()

    /** 表面容器 — 中层级 */
    @ColorInt
    val colorSurfaceContainer: Int = 0xFFF2F2F7.toInt()

    /** 暗色模式表面容器 — 中层级 */
    @ColorInt
    val colorSurfaceContainerDark: Int = 0xFF1C1C1E.toInt()

    /** 表面容器 — 高层级 */
    @ColorInt
    val colorSurfaceContainerHigh: Int = 0xFFE5E5EA.toInt()

    /** 暗色模式表面容器 — 高层级 */
    @ColorInt
    val colorSurfaceContainerHighDark: Int = 0xFF2C2C2E.toInt()

    /** 表面容器 — 最高层级 */
    @ColorInt
    val colorSurfaceContainerHighest: Int = 0xFFD8D8DD.toInt()

    /** 暗色模式表面容器 — 最高层级 */
    @ColorInt
    val colorSurfaceContainerHighestDark: Int = 0xFF3A3A3C.toInt()

    // ═══════════════════════════════════════════════════════════════════════
    // 形状 Token — 圆角半径（dp 值，由各体系自行转换为 px / Dp）
    // ═══════════════════════════════════════════════════════════════════════

    /** iOS 风格按钮圆角 */
    const val cornerRadiusButtonIos = 12

    /** Pixel (M3) 风格按钮圆角 */
    const val cornerRadiusButtonPixel = 8

    /** iOS 风格卡片圆角 */
    const val cornerRadiusCardIos = 16

    /** Pixel (M3) 风格卡片圆角 */
    const val cornerRadiusCardPixel = 12

    /** iOS 风格输入框圆角 */
    const val cornerRadiusTextFieldIos = 12

    /** Pixel (M3) 风格输入框圆角 */
    const val cornerRadiusTextFieldPixel = 8

    /** 小圆角 */
    const val cornerRadiusSmall = 8

    /** 中圆角 */
    const val cornerRadiusMedium = 12

    /** 大圆角 */
    const val cornerRadiusLarge = 16

    // ═══════════════════════════════════════════════════════════════════════
    // 排版 Token — 字号（sp）与字重
    // ═══════════════════════════════════════════════════════════════════════

    /** 大标题字号 */
    const val fontSizeLargeTitle = 34f

    /** 标题 1 字号 */
    const val fontSizeTitle1 = 28f

    /** 标题 2 字号 */
    const val fontSizeTitle2 = 22f

    /** 标题 3 字号 */
    const val fontSizeTitle3 = 20f

    /** 标题字号 */
    const val fontSizeHeadline = 17f

    /** 正文字号 */
    const val fontSizeBody = 17f

    /** 副标题字号 */
    const val fontSizeCallout = 16f

    /** 小标题字号 */
    const val fontSizeSubheadline = 15f

    /** 脚注字号 */
    const val fontSizeFootnote = 13f

    /** 说明文字 1 字号 */
    const val fontSizeCaption1 = 12f

    /** 说明文字 2 字号 */
    const val fontSizeCaption2 = 11f

    /** 按钮字号 */
    const val fontSizeButton = 16f

    // ═══════════════════════════════════════════════════════════════════════
    // 交互 Token
    // ═══════════════════════════════════════════════════════════════════════

    /** iOS 按压遮罩最大透明度 */
    const val pressOverlayMaxAlpha = 0.2f

    /** iOS 按压进入动画时长（毫秒） */
    const val pressInDuration = 150L

    /** iOS 按压退出动画时长（毫秒） */
    const val pressOutDuration = 200L

    /** 性能警告阈值（毫秒） */
    const val perfThresholdMs = 5L

    /** 禁用态不透明度 */
    const val disabledAlpha = 0.38f
}
