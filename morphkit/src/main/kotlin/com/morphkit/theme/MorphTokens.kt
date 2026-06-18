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
 * | tintColor                 | colorPrimary            | [Colors.colorBlue500]          |
 * | systemBackground          | colorSurface            | [Colors.colorSurface]          |
 * | secondarySystemBackground | colorSurfaceVariant     | [Colors.colorSurfaceVariant]   |
 * | separator                 | colorOutlineVariant     | [Colors.colorOutlineVariant]   |
 * | label                     | colorOnSurface          | [Colors.colorOnSurface]        |
 * | secondaryLabel            | colorOnSurfaceVariant   | [Colors.colorOnSurfaceVariant] |
 *
 * @see MorphTheme View 体系设计系统
 * @see MorphComposeTheme Compose 体系设计系统
 */
object MorphTokens {

    // ═══════════════════════════════════════════════════════════════════════
    // 颜色 Token — 调色板基色 + M3 语义色
    // ═══════════════════════════════════════════════════════════════════════

    /** 颜色 Token 子集 — 通过 `MorphTokens.Colors.colorXxx` 访问 */
    object Colors {

        // ── 调色板基色 ──

        /** iOS 蓝 #007AFF / M3 Primary 基色 */
        @ColorInt
        val colorBlue500: Int = 0xFF007AFF.toInt()

        /** iOS 蓝色变体（浅色），暗色模式下 primary 使用 */
        @ColorInt
        val colorBlue100: Int = 0xFFD1E8FF.toInt()

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

        /** 暗色模式主色上的文字/图标色（L7 修复：从语义色别名区移到 primary 色组） */
        @ColorInt
        val colorOnPrimaryDark: Int = 0xFF001D3F.toInt()

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

        // ── 语义色别名（Light 模式 — 与 Dark 模式命名对齐） ──

        /** 错误色 — 别名 [colorRed500]，与 Dark 模式 [colorErrorDark] 对称 */
        @ColorInt
        val colorError: Int = colorRed500

        /** 成功色 — 别名 [colorGreen500]，与 Dark 模式 [colorSuccessDark] 对称 */
        @ColorInt
        val colorSuccess: Int = colorGreen500

        /** 警告色 — 别名 [colorOrange500]，与 Dark 模式 [colorWarningDark] 对称 */
        @ColorInt
        val colorWarning: Int = colorOrange500

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

        // ── Scrim + 毛玻璃色（功能色） ──

        /** Scrim 遮罩色 — 模态遮罩背景（M3 规范：黑色基底） */
        @ColorInt
        val colorScrim: Int = 0xFF000000.toInt()

        /** 毛玻璃浅色背景 — 80% 不透明度白色（#CCFFFFFF） */
        @ColorInt
        val colorGlassmorphismLightBg: Int = 0xCCFFFFFF.toInt()

        /** 毛玻璃深色背景 — 60% 不透明度黑色（#99000000） */
        @ColorInt
        val colorGlassmorphismDarkBg: Int = 0x99000000.toInt()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 形状 Token — 圆角半径（dp 值，由各体系自行转换为 px / Dp）
    // ═══════════════════════════════════════════════════════════════════════

    /** 形状 Token 子集 — 通过 `MorphTokens.Shapes.cornerRadiusXxx` 访问 */
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
        /** 焦点叠加透明度 — 输入框焦点反馈（MorphEditText 用） */
        const val focusOverlayAlpha = 0.08f
        /** 焦点动画时长（ms） — 输入框焦点过渡动画 */
        const val focusAnimationDuration = 150L
        /** 按钮焦点抬升/回落动画时长（ms） — Compose MorphButton 焦点过渡 */
        const val buttonFocusDuration = 200L
        /** PLAIN 按钮按压叠加透明度 — 透明底上的 primary 微弱底色 */
        const val plainPressOverlayAlpha = 0.12f
    }

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
        /** 按钮最小宽度（88dp）— M3 无障碍触控目标 */
        const val buttonMinWidth = 88
        /** 按钮最小高度（48dp）— M3 无障碍触控目标 */
        const val buttonMinHeight = 48
    }

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
        /** 焦点抬升阴影（4dp）— 键盘焦点无障碍反馈 */
        const val elevationFocus = 4
        /** 五级阴影（12dp）— 模态底部抽屉 */
        const val elevationLevel5 = 12
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 动效 Token — 时长（毫秒）与缓动曲线
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 动效 Token 子集。
     *
     * 缓动曲线以 CSS cubic-bezier 字符串格式给出（如 `motionEasingStandard`），
     * 仅供文档参考，无法在 Android/Compose 中直接使用。
     *
     * 在 Compose 中使用时，可通过以下方式转换为 [androidx.compose.animation.core.Easing]：
     * ```kotlin
     * val easing = Easing { t ->
     *     // 解析 cubic-bezier 控制点 (x1, y1, x2, y2) 并计算
     *     CubicBezierEasing(x1, y1, x2, y2).transform(t)
     * }
     * ```
     */
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
}
