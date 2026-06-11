package com.morphkit.widget.text

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.morphkit.R
import com.morphkit.theme.MorphTheme

/**
 * MorphKit iOS 极简风格输入框。
 *
 * 基于 [AppCompatEditText]，还原 iOS 输入框的视觉特征：
 *
 * ## 视觉特征
 * - **去除 Android 原生底线**：默认背景是透明，无下划线、无边框
 * - **搜索栏样式**：[Style.SEARCH] 模式下使用 [MorphTheme.morphColorSurfaceVariant]
 *   浅灰圆角背景，还原 iOS 搜索栏的极简观感
 * - **光标颜色**：跟随 [MorphTheme.morphColorPrimary]（iOS 系统蓝），与整体设计系统一致
 *
 * ## 交互特征
 * - 获取焦点时背景色透明度微调，提供 iOS 风格的「轻微变亮/变暗」反馈
 * - 失去焦点时恢复原始背景色
 * - 禁用态不响应焦点动画
 *
 * ## 使用方式
 *
 * ```kotlin
 * MorphKit.init(this) {
 *     groupReplace(listOf("EditText", "AppCompatEditText")) { ctx, attrs ->
 *         MorphEditText(ctx, attrs).apply { style = MorphEditText.Style.SEARCH }
 *     }
 * }
 * ```
 *
 * @see MorphTheme 颜色/形状/排版设计系统
 * @see Style 预设样式枚举
 */
class MorphEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphEditTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    /**
     * iOS 风格输入框预设样式。
     *
     * | 样式 | 背景 | 焦点反馈 | iOS 对应 |
     * |------|------|----------|----------|
     * | [BARE] | 透明 | 无 | 无背景输入框 |
     * | [SEARCH] | morphColorSurfaceVariant | 透明度微调 | UISearchBar 内部输入框 |
     */
    enum class Style {

        /**
         * 裸输入样式 — 无背景。
         *
         * - 背景：完全透明
         * - 焦点反馈：无
         * - 适用于自定义布局中的内嵌输入框
         */
        BARE,

        /**
         * 搜索栏样式 — iOS 极简搜索框。
         *
         * - 背景：[MorphTheme.morphColorSurfaceVariant] 浅灰圆角
         * - 焦点反馈：获取焦点时背景色轻微变化
         * - 无下划线、无边框
         *
         * 对应 iOS UISearchBar 内部的 UITextField。
         */
        SEARCH
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 内部状态
    // ═══════════════════════════════════════════════════════════════════════

    /** 当前输入框样式 */
    var style: Style = Style.SEARCH
        set(value) {
            field = value
            applyStyle()
        }

    /** 搜索栏背景 Drawable（复用同一实例） */
    private val searchBackgroundDrawable = GradientDrawable().apply {
        cornerRadius = MorphTheme.cornerMedium(context).toFloat()
    }

    /** 缓存：搜索栏原始背景色 */
    private var searchBackgroundColor: Int = MorphTheme.morphColorSurfaceVariant(context)

    /** 焦点态背景色（基于原始色微调透明度） */
    private var searchBackgroundFocusedColor: Int = 0

    /** 缓存：tintColor 用于光标着色 */
    private var tintColor: Int = MorphTheme.morphColorPrimary(context)

    // ═══════════════════════════════════════════════════════════════════════
    // 初始化
    // ═══════════════════════════════════════════════════════════════════════

    init {
        // ── 去除 Android 原生底线 ──
        // AppCompatEditText 默认带一条底部横线（EditText 底线），
        // iOS 输入框无此线条，必须彻底清除
        background = null

        // ── 应用排版 ──
        val typo = MorphTheme.typography.body
        textSize = typo.fontSize
        typeface = typo.weight.toTypeface()

        // ── 提示文字颜色 ──
        setHintTextColor(MorphTheme.morphColorOnSurfaceVariant(context))

        // ── 默认文字颜色 ──
        setTextColor(MorphTheme.morphColorOnSurface(context))

        // ── 应用样式 ──
        applyStyle()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 样式应用
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 根据当前 [style] 应用完整的视觉配置。
     */
    private fun applyStyle() {
        refreshColors()

        when (style) {
            Style.BARE -> applyBareState()
            Style.SEARCH -> applySearchState(hasFocus())
        }
    }

    /**
     * 刷新与暗黑模式相关的颜色缓存。
     */
    private fun refreshColors() {
        searchBackgroundColor = MorphTheme.morphColorSurfaceVariant(context)
        tintColor = MorphTheme.morphColorPrimary(context)

        // 焦点态背景色：在原始色基础上微调透明度
        // 浅色模式：略加深（叠加少量黑色，在浅灰背景上更明显）
        // 深色模式：略提亮（叠加少量白色）
        searchBackgroundFocusedColor = MorphTheme.blendColor(
            searchBackgroundColor,
            if (MorphTheme.isDarkMode(context)) Color.WHITE else Color.BLACK,
            FOCUS_OVERLAY_ALPHA
        )

        // 刷新提示文字颜色（暗黑模式切换后）
        setHintTextColor(MorphTheme.morphColorOnSurfaceVariant(context))
        setTextColor(MorphTheme.morphColorOnSurface(context))
    }

    /** 应用裸输入样式 */
    private fun applyBareState() {
        background = null
    }

    /**
     * 应用搜索栏样式。
     *
     * @param focused 是否处于焦点态
     */
    private fun applySearchState(focused: Boolean) {
        val bgColor = if (focused) searchBackgroundFocusedColor else searchBackgroundColor
        searchBackgroundDrawable.setColor(bgColor)
        background = searchBackgroundDrawable
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 焦点反馈
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 焦点变化回调。
     *
     * 在 SEARCH 样式下，获取焦点时背景色轻微变化，提供 iOS 风格的微弱反馈：
     * - 获取焦点：背景色叠加少量白色遮罩 → 略微提亮
     * - 失去焦点：恢复原始背景色
     *
     * BARE 样式下不做任何视觉反馈。
     *
     * @param focused 当前是否获得焦点
     * @param direction 焦点来源方向
     * @param previouslyFocusedRect 前一个焦点 View 的矩形
     */
    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (!isEnabled) return

        if (style == Style.SEARCH) {
            applySearchState(focused)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 生命周期
    // ═══════════════════════════════════════════════════════════════════════

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyStyle()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Activity 不重建时（configChanges 包含 uiMode），手动刷新颜色
        applyStyle()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 常量
    // ═══════════════════════════════════════════════════════════════════════

    companion object {

        /** 焦点态遮罩透明度 — 极轻微的 8% 变化，iOS 风格的微妙反馈 */
        private const val FOCUS_OVERLAY_ALPHA = 0.08f
    }
}
