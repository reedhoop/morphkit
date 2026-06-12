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

    /** 颜色 Token 子集 — 可通过 `MorphTokens.Colors` 或直接 `MorphTokens.colorXxx` 访问 */
    object Colors {

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

        /** 暗色模式主色容器色 */
        @ColorInt
        val colorPrimaryContainerDark: Int = 0xFF0055CC.toInt()

        /** 主色上的文字/图标色 */
        @ColorInt
        val colorOnPrimary: Int = 0xFFFFFFFF.toInt()

        /** 主色容器上的文字/图标色 */
        @ColorInt
        val colorOnPrimaryContainer: Int = 0xFF001D3F.toInt()

        /** 暗色模式主色容器上的文字/图标色 */
        @ColorInt
        val colorOnPrimaryContainerDark: Int = 0xFFD1E8FF.toInt()

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

        /** 暗色模式错误色 */
        @ColorInt
        val colorErrorDark: Int = 0xFFFF6961.toInt()

        /** 暗色模式成功色 */
        @ColorInt
        val colorSuccessDark: Int = 0xFF30D158.toInt()

        /** 暗色模式警告色 */
        @ColorInt
        val colorWarningDark: Int = 0xFFFFB340.toInt()

        /** 暗色模式主色上的文字/图标色 */
        @ColorInt
        val colorOnPrimaryDark: Int = 0xFF001D3F.toInt()

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
    }

    // ── 颜色扁平化委托：MorphTokens.colorXxx 等价于 MorphTokens.Colors.colorXxx ──
    // 使用 @get:ColorInt 在 getter 上标注注解，委托属性无 backing field 但仍可标注

    /** iOS 蓝 #007AFF / M3 Primary 基色 */
    @get:ColorInt val colorBlue500: Int get() = Colors.colorBlue500
    /** iOS 蓝色变体（浅色），暗色模式下 primary 使用 */
    @get:ColorInt val colorBlue100: Int get() = Colors.colorBlue100
    /** iOS 蓝色变体（深色），暗色模式下 primaryContainer 使用 */
    @get:ColorInt val colorBlue700: Int get() = Colors.colorBlue700
    /** iOS 系统红色（destructive 操作 / error） */
    @get:ColorInt val colorRed500: Int get() = Colors.colorRed500
    /** iOS 系统绿色（成功状态） */
    @get:ColorInt val colorGreen500: Int get() = Colors.colorGreen500
    /** iOS 系统橙色（警告） */
    @get:ColorInt val colorOrange500: Int get() = Colors.colorOrange500
    /** 主色容器色 */
    @get:ColorInt val colorPrimaryContainer: Int get() = Colors.colorPrimaryContainer
    /** 暗色模式主色容器色 */
    @get:ColorInt val colorPrimaryContainerDark: Int get() = Colors.colorPrimaryContainerDark
    /** 主色上的文字/图标色 */
    @get:ColorInt val colorOnPrimary: Int get() = Colors.colorOnPrimary
    /** 主色容器上的文字/图标色 */
    @get:ColorInt val colorOnPrimaryContainer: Int get() = Colors.colorOnPrimaryContainer
    /** 暗色模式主色容器上的文字/图标色 */
    @get:ColorInt val colorOnPrimaryContainerDark: Int get() = Colors.colorOnPrimaryContainerDark
    /** 表面色（页面背景） */
    @get:ColorInt val colorSurface: Int get() = Colors.colorSurface
    /** 暗色模式表面色 */
    @get:ColorInt val colorSurfaceDark: Int get() = Colors.colorSurfaceDark
    /** 表面变体色（分组背景、卡片） */
    @get:ColorInt val colorSurfaceVariant: Int get() = Colors.colorSurfaceVariant
    /** 暗色模式表面变体色 */
    @get:ColorInt val colorSurfaceVariantDark: Int get() = Colors.colorSurfaceVariantDark
    /** 表面上的文字/图标色 */
    @get:ColorInt val colorOnSurface: Int get() = Colors.colorOnSurface
    /** 暗色模式表面上的文字/图标色 */
    @get:ColorInt val colorOnSurfaceDark: Int get() = Colors.colorOnSurfaceDark
    /** 表面变体上的文字/图标色 */
    @get:ColorInt val colorOnSurfaceVariant: Int get() = Colors.colorOnSurfaceVariant
    /** 暗色模式表面变体上的文字/图标色 */
    @get:ColorInt val colorOnSurfaceVariantDark: Int get() = Colors.colorOnSurfaceVariantDark
    /** 轮廓变体色（分割线、边框） */
    @get:ColorInt val colorOutlineVariant: Int get() = Colors.colorOutlineVariant
    /** 暗色模式轮廓变体色 */
    @get:ColorInt val colorOutlineVariantDark: Int get() = Colors.colorOutlineVariantDark
    /** 背景色 */
    @get:ColorInt val colorBackground: Int get() = Colors.colorBackground
    /** 暗色模式背景色 */
    @get:ColorInt val colorBackgroundDark: Int get() = Colors.colorBackgroundDark
    /** 次要色 */
    @get:ColorInt val colorSecondary: Int get() = Colors.colorSecondary
    /** 暗色模式次要色 */
    @get:ColorInt val colorSecondaryDark: Int get() = Colors.colorSecondaryDark
    /** 次要色上的文字/图标色 */
    @get:ColorInt val colorOnSecondary: Int get() = Colors.colorOnSecondary
    /** 暗色模式次要色上的文字/图标色 */
    @get:ColorInt val colorOnSecondaryDark: Int get() = Colors.colorOnSecondaryDark
    /** 次要色容器色 */
    @get:ColorInt val colorSecondaryContainer: Int get() = Colors.colorSecondaryContainer
    /** 暗色模式次要色容器色 */
    @get:ColorInt val colorSecondaryContainerDark: Int get() = Colors.colorSecondaryContainerDark
    /** 次要色容器上的文字/图标色 */
    @get:ColorInt val colorOnSecondaryContainer: Int get() = Colors.colorOnSecondaryContainer
    /** 暗色模式次要色容器上的文字/图标色 */
    @get:ColorInt val colorOnSecondaryContainerDark: Int get() = Colors.colorOnSecondaryContainerDark
    /** 第三色 */
    @get:ColorInt val colorTertiary: Int get() = Colors.colorTertiary
    /** 暗色模式第三色 */
    @get:ColorInt val colorTertiaryDark: Int get() = Colors.colorTertiaryDark
    /** 第三色上的文字/图标色 */
    @get:ColorInt val colorOnTertiary: Int get() = Colors.colorOnTertiary
    /** 暗色模式第三色上的文字/图标色 */
    @get:ColorInt val colorOnTertiaryDark: Int get() = Colors.colorOnTertiaryDark
    /** 第三色容器色 */
    @get:ColorInt val colorTertiaryContainer: Int get() = Colors.colorTertiaryContainer
    /** 暗色模式第三色容器色 */
    @get:ColorInt val colorTertiaryContainerDark: Int get() = Colors.colorTertiaryContainerDark
    /** 第三色容器上的文字/图标色 */
    @get:ColorInt val colorOnTertiaryContainer: Int get() = Colors.colorOnTertiaryContainer
    /** 暗色模式第三色容器上的文字/图标色 */
    @get:ColorInt val colorOnTertiaryContainerDark: Int get() = Colors.colorOnTertiaryContainerDark
    /** 错误色上的文字/图标色 */
    @get:ColorInt val colorOnError: Int get() = Colors.colorOnError
    /** 暗色模式错误色上的文字/图标色 */
    @get:ColorInt val colorOnErrorDark: Int get() = Colors.colorOnErrorDark
    /** 错误色容器色 */
    @get:ColorInt val colorErrorContainer: Int get() = Colors.colorErrorContainer
    /** 暗色模式错误色容器色 */
    @get:ColorInt val colorErrorContainerDark: Int get() = Colors.colorErrorContainerDark
    /** 错误色容器上的文字/图标色 */
    @get:ColorInt val colorOnErrorContainer: Int get() = Colors.colorOnErrorContainer
    /** 暗色模式错误色容器上的文字/图标色 */
    @get:ColorInt val colorOnErrorContainerDark: Int get() = Colors.colorOnErrorContainerDark
    /** 暗色模式错误色 */
    @get:ColorInt val colorErrorDark: Int get() = Colors.colorErrorDark
    /** 暗色模式成功色 */
    @get:ColorInt val colorSuccessDark: Int get() = Colors.colorSuccessDark
    /** 暗色模式警告色 */
    @get:ColorInt val colorWarningDark: Int get() = Colors.colorWarningDark
    /** 暗色模式主色上的文字/图标色 */
    @get:ColorInt val colorOnPrimaryDark: Int get() = Colors.colorOnPrimaryDark
    /** 轮廓色（输入框边框、焦点环） */
    @get:ColorInt val colorOutline: Int get() = Colors.colorOutline
    /** 暗色模式轮廓色 */
    @get:ColorInt val colorOutlineDark: Int get() = Colors.colorOutlineDark
    /** 表面暗调（最暗的表面容器） */
    @get:ColorInt val colorSurfaceDim: Int get() = Colors.colorSurfaceDim
    /** 暗色模式表面暗调 */
    @get:ColorInt val colorSurfaceDimDark: Int get() = Colors.colorSurfaceDimDark
    /** 表面亮调（最亮的表面容器） */
    @get:ColorInt val colorSurfaceBright: Int get() = Colors.colorSurfaceBright
    /** 暗色模式表面亮调 */
    @get:ColorInt val colorSurfaceBrightDark: Int get() = Colors.colorSurfaceBrightDark
    /** 表面容器 — 最低层级 */
    @get:ColorInt val colorSurfaceContainerLowest: Int get() = Colors.colorSurfaceContainerLowest
    /** 暗色模式表面容器 — 最低层级 */
    @get:ColorInt val colorSurfaceContainerLowestDark: Int get() = Colors.colorSurfaceContainerLowestDark
    /** 表面容器 — 低层级 */
    @get:ColorInt val colorSurfaceContainerLow: Int get() = Colors.colorSurfaceContainerLow
    /** 暗色模式表面容器 — 低层级 */
    @get:ColorInt val colorSurfaceContainerLowDark: Int get() = Colors.colorSurfaceContainerLowDark
    /** 表面容器 — 中层级 */
    @get:ColorInt val colorSurfaceContainer: Int get() = Colors.colorSurfaceContainer
    /** 暗色模式表面容器 — 中层级 */
    @get:ColorInt val colorSurfaceContainerDark: Int get() = Colors.colorSurfaceContainerDark
    /** 表面容器 — 高层级 */
    @get:ColorInt val colorSurfaceContainerHigh: Int get() = Colors.colorSurfaceContainerHigh
    /** 暗色模式表面容器 — 高层级 */
    @get:ColorInt val colorSurfaceContainerHighDark: Int get() = Colors.colorSurfaceContainerHighDark
    /** 表面容器 — 最高层级 */
    @get:ColorInt val colorSurfaceContainerHighest: Int get() = Colors.colorSurfaceContainerHighest
    /** 暗色模式表面容器 — 最高层级 */
    @get:ColorInt val colorSurfaceContainerHighestDark: Int get() = Colors.colorSurfaceContainerHighestDark

    // ═══════════════════════════════════════════════════════════════════════
    // 形状 Token — 圆角半径（dp 值，由各体系自行转换为 px / Dp）
    // ═══════════════════════════════════════════════════════════════════════

    /** 形状 Token 子集 — 可通过 `MorphTokens.Shapes` 或直接 `MorphTokens.cornerRadiusXxx` 访问 */
    object Shapes {
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
    }

    /** iOS 风格按钮圆角 */
    const val cornerRadiusButtonIos = Shapes.cornerRadiusButtonIos
    /** Pixel (M3) 风格按钮圆角 */
    const val cornerRadiusButtonPixel = Shapes.cornerRadiusButtonPixel
    /** iOS 风格卡片圆角 */
    const val cornerRadiusCardIos = Shapes.cornerRadiusCardIos
    /** Pixel (M3) 风格卡片圆角 */
    const val cornerRadiusCardPixel = Shapes.cornerRadiusCardPixel
    /** iOS 风格输入框圆角 */
    const val cornerRadiusTextFieldIos = Shapes.cornerRadiusTextFieldIos
    /** Pixel (M3) 风格输入框圆角 */
    const val cornerRadiusTextFieldPixel = Shapes.cornerRadiusTextFieldPixel
    /** 小圆角 */
    const val cornerRadiusSmall = Shapes.cornerRadiusSmall
    /** 中圆角 */
    const val cornerRadiusMedium = Shapes.cornerRadiusMedium
    /** 大圆角 */
    const val cornerRadiusLarge = Shapes.cornerRadiusLarge

    // ═══════════════════════════════════════════════════════════════════════
    // 排版 Token — 字号（sp）与字重
    // ═══════════════════════════════════════════════════════════════════════

    /** 排版 Token 子集 */
    object Typography {
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
    }

    /** 大标题字号 */
    const val fontSizeLargeTitle = Typography.fontSizeLargeTitle
    /** 标题 1 字号 */
    const val fontSizeTitle1 = Typography.fontSizeTitle1
    /** 标题 2 字号 */
    const val fontSizeTitle2 = Typography.fontSizeTitle2
    /** 标题 3 字号 */
    const val fontSizeTitle3 = Typography.fontSizeTitle3
    /** 标题字号 */
    const val fontSizeHeadline = Typography.fontSizeHeadline
    /** 正文字号 */
    const val fontSizeBody = Typography.fontSizeBody
    /** 副标题字号 */
    const val fontSizeCallout = Typography.fontSizeCallout
    /** 小标题字号 */
    const val fontSizeSubheadline = Typography.fontSizeSubheadline
    /** 脚注字号 */
    const val fontSizeFootnote = Typography.fontSizeFootnote
    /** 说明文字 1 字号 */
    const val fontSizeCaption1 = Typography.fontSizeCaption1
    /** 说明文字 2 字号 */
    const val fontSizeCaption2 = Typography.fontSizeCaption2
    /** 按钮字号 */
    const val fontSizeButton = Typography.fontSizeButton

    // ═══════════════════════════════════════════════════════════════════════
    // 交互 Token
    // ═══════════════════════════════════════════════════════════════════════

    /** 交互 Token 子集 */
    object Interaction {
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
        /** 三级文字透明度（iOS tertiaryLabelColor 近似，55% 不透明度） */
        const val tertiaryTextAlpha = 0.55f
    }

    /** iOS 按压遮罩最大透明度 */
    const val pressOverlayMaxAlpha = Interaction.pressOverlayMaxAlpha
    /** iOS 按压进入动画时长（毫秒） */
    const val pressInDuration = Interaction.pressInDuration
    /** iOS 按压退出动画时长（毫秒） */
    const val pressOutDuration = Interaction.pressOutDuration
    /** 性能警告阈值（毫秒） */
    const val perfThresholdMs = Interaction.perfThresholdMs
    /** 禁用态不透明度 */
    const val disabledAlpha = Interaction.disabledAlpha
    /** 三级文字透明度（iOS tertiaryLabelColor 近似，55% 不透明度） */
    const val tertiaryTextAlpha = Interaction.tertiaryTextAlpha

    // ═══════════════════════════════════════════════════════════════════════
    // 间距 Token — dp 值
    // ═══════════════════════════════════════════════════════════════════════

    /** 间距 Token 子集 */
    object Spacing {
        /** 极小间距（4dp）— 图标与文字间距、紧凑列表项内边距 */
        const val spacingXs = 4
        /** 小间距（8dp）— 同组元素间距、列表项水平内边距 */
        const val spacingSm = 8
        /** 中间距（12dp）— 卡片内边距、表单字段间距 */
        const val spacingMd = 12
        /** 标准间距（16dp）— 页面水平边距、卡片间距 */
        const val spacingBase = 16
        /** 大间距（20dp）— 区块间距、详情页段落间距 */
        const val spacingLg = 20
        /** 超大间距（24dp）— 页面顶部/底部安全区域、大区块间距 */
        const val spacingXl = 24
        /** 极大间距（32dp）— 页面标题与内容区间距 */
        const val spacingXxl = 32
    }

    /** 极小间距（4dp） */
    const val spacingXs = Spacing.spacingXs
    /** 小间距（8dp） */
    const val spacingSm = Spacing.spacingSm
    /** 中间距（12dp） */
    const val spacingMd = Spacing.spacingMd
    /** 标准间距（16dp） */
    const val spacingBase = Spacing.spacingBase
    /** 大间距（20dp） */
    const val spacingLg = Spacing.spacingLg
    /** 超大间距（24dp） */
    const val spacingXl = Spacing.spacingXl
    /** 极大间距（32dp） */
    const val spacingXxl = Spacing.spacingXxl

    // ═══════════════════════════════════════════════════════════════════════
    // 阴影层级 Token — dp 值
    // ═══════════════════════════════════════════════════════════════════════

    /** 阴影 Token 子集 */
    object Elevation {
        /** 无阴影 — iOS 风格默认 */
        const val elevationNone = 0
        /** 一级阴影（1dp）— 微弱浮起感，如搜索栏 */
        const val elevationLevel1 = 1
        /** 二级阴影（3dp）— 轻浮卡片、Snackbar */
        const val elevationLevel2 = 3
        /** 三级阴影（6dp）— 浮动按钮、弹窗 */
        const val elevationLevel3 = 6
        /** 四级阴影（8dp）— 对话框、底部抽屉 */
        const val elevationLevel4 = 8
        /** 五级阴影（12dp）— 模态底部抽屉 */
        const val elevationLevel5 = 12
    }

    /** 无阴影 — iOS 风格默认 */
    const val elevationNone = Elevation.elevationNone
    /** 一级阴影（1dp） */
    const val elevationLevel1 = Elevation.elevationLevel1
    /** 二级阴影（3dp） */
    const val elevationLevel2 = Elevation.elevationLevel2
    /** 三级阴影（6dp） */
    const val elevationLevel3 = Elevation.elevationLevel3
    /** 四级阴影（8dp） */
    const val elevationLevel4 = Elevation.elevationLevel4
    /** 五级阴影（12dp） */
    const val elevationLevel5 = Elevation.elevationLevel5

    // ═══════════════════════════════════════════════════════════════════════
    // 动效 Token — 时长（毫秒）与缓动曲线
    // ═══════════════════════════════════════════════════════════════════════

    /** 动效 Token 子集 */
    object Motion {
        /** 极短动画时长 — 微交互反馈（涟漪、开关） */
        const val motionDurationXs = 100L
        /** 短动画时长 — 小范围状态切换（选中、展开折叠） */
        const val motionDurationSm = 150L
        /** 中等动画时长 — 页面转场、控件出现 */
        const val motionDurationMd = 250L
        /** 长动画时长 — 复杂转场、弹窗出现 */
        const val motionDurationLg = 350L
        /** 超长动画时长 — 全屏转场、大区域展开 */
        const val motionDurationXl = 500L
        /** 标准缓动曲线 — 大多数 UI 动画 */
        const val motionEasingStandard = "cubic-bezier(0.2, 0.0, 0, 1.0)"
        /** 减速缓动曲线 — 元素进入屏幕 */
        const val motionEasingDecelerate = "cubic-bezier(0.0, 0.0, 0, 1.0)"
        /** 加速缓动曲线 — 元素离开屏幕 */
        const val motionEasingAccelerate = "cubic-bezier(0.3, 0.0, 1.0, 1.0)"
        /** 线性缓动曲线 — 持续动画（进度条、旋转） */
        const val motionEasingLinear = "cubic-bezier(0.0, 0.0, 1.0, 1.0)"
    }

    /** 极短动画时长 */
    const val motionDurationXs = Motion.motionDurationXs
    /** 短动画时长 */
    const val motionDurationSm = Motion.motionDurationSm
    /** 中等动画时长 */
    const val motionDurationMd = Motion.motionDurationMd
    /** 长动画时长 */
    const val motionDurationLg = Motion.motionDurationLg
    /** 超长动画时长 */
    const val motionDurationXl = Motion.motionDurationXl
    /** 标准缓动曲线 */
    const val motionEasingStandard = Motion.motionEasingStandard
    /** 减速缓动曲线 */
    const val motionEasingDecelerate = Motion.motionEasingDecelerate
    /** 加速缓动曲线 */
    const val motionEasingAccelerate = Motion.motionEasingAccelerate
    /** 线性缓动曲线 */
    const val motionEasingLinear = Motion.motionEasingLinear
}
