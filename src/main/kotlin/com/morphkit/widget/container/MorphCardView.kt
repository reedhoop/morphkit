package com.morphkit.widget.container

import android.content.Context
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.card.MaterialCardView
import com.morphkit.R
import com.morphkit.theme.MorphTheme
import com.morphkit.theme.dp

/**
 * MorphKit iOS 17 风格卡片容器。
 *
 * 基于 [MaterialCardView]，利用其现成的 Shape 模型和圆角裁剪能力，
 * 还原 iOS 17 卡片的两种视觉模式：
 *
 * ## 极简白卡片模式（默认）
 * - 零阴影、极细边框、纯色背景
 * - 边框颜色为极浅灰，模拟 iOS 分组分割线的「存在但克制」质感
 * - 圆角 [MorphTheme.cornerLarge]（16dp）
 *
 * ## 毛玻璃模式（Glassmorphism）
 * - 半透明背景 + 背后内容模糊
 * - API 31+ 使用 [RenderEffect.createBlurEffect] 实现真实高斯模糊
 * - 低版本降级为仅半透明遮罩，不崩溃
 * - 自动裁剪子 View 到圆角范围内
 *
 * ## 使用方式
 *
 * ```xml
 * <!-- 极简白卡片（默认） -->
 * <com.morphkit.engine.MorphCardView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content">
 *     <!-- 子 View -->
 * </com.morphkit.engine.MorphCardView>
 *
 * <!-- 毛玻璃卡片 -->
 * <com.morphkit.engine.MorphCardView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:isGlassmorphism="true" />
 * ```
 *
 * @see MorphTheme.cornerRadiusLarge 圆角大小
 * @see isGlassmorphism 毛玻璃模式开关
 */
class MorphCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphCardStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    // ═══════════════════════════════════════════════════════════════════════
    // 内部状态
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 毛玻璃模式开关。
     *
     * - `true`：背景变为半透明白色（浅色模式）/ 半透明黑色（深色模式），
     *   API 31+ 自动启用 [RenderEffect.createBlurEffect] 对背后内容做高斯模糊。
     * - `false`（默认）：极简白卡片模式，纯色背景 + 极细边框。
     *
     * **注意**：毛玻璃模式下的模糊效果仅作用于卡片背后**同级或更底层**的 View，
     * 对于与卡片无遮挡关系的 View 不产生模糊。模糊半径为 [BLUR_RADIUS]px。
     */
    var isGlassmorphism: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                applyVisualState()
            }
        }

    /** 极细边框宽度（0.5dp → px） */
    private val strokeWidthPx: Float = STROKE_WIDTH_DP.dp

    /** 缓存：极简模式背景色 */
    private var cardBackgroundColor: Int = MorphTheme.morphColorSurface(context)

    /** 缓存：极简模式边框色（极浅灰，模拟 iOS 分组分割线） */
    private var strokeColor: Int = MorphTheme.morphColorOutlineVariant(context)

    /** 缓存：毛玻璃模式背景色（浅色） */
    private var glassmorphismLightBg: Int = COLOR_GLASSMORPHISM_LIGHT_BG

    /** 缓存：毛玻璃模式背景色（深色） */
    private var glassmorphismDarkBg: Int = COLOR_GLASSMORPHISM_DARK_BG

    // ═══════════════════════════════════════════════════════════════════════
    // 初始化
    // ═══════════════════════════════════════════════════════════════════════

    init {
        // ── 关闭 Material 默认阴影动画 ──
        // MaterialCardView 默认带 elevation 阴影和按压态阴影动画，
        // 与 iOS 极简风格完全冲突，必须彻底清除
        cardElevation = 0f
        maxCardElevation = 0f

        // ── 关闭涟漪效果 ──
        // MaterialCardView 默认在可点击时显示 Ripple 涟漪，
        // iOS 卡片无涟漪反馈，需禁用
        isClickable = false
        rippleColor = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)

        // ── 关闭状态动画 ──
        // 禁用按压态阴影变化动画
        stateListAnimator = null

        // ── 圆角 ──
        radius = MorphTheme.cornerLarge.toFloat()

        // ── 裁剪子 View 到圆角范围 ──
        // 毛玻璃模式下子 View 不能溢出圆角区域
        clipChildren = true
        clipToPadding = true

        // ── 应用视觉状态 ──
        applyVisualState()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 视觉状态管理
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 根据当前模式（极简/毛玻璃）与暗黑模式，应用完整的视觉配置。
     *
     * 此方法在以下时机调用：
     * - [isGlassmorphism] 被赋值时
     * - [onAttachedToWindow] 时（适配暗黑模式切换）
     */
    private fun applyVisualState() {
        refreshColors()

        if (isGlassmorphism) {
            applyGlassmorphismState()
        } else {
            applyCleanCardState()
        }
    }

    /**
     * 刷新与暗黑模式相关的颜色缓存。
     */
    private fun refreshColors() {
        cardBackgroundColor = MorphTheme.morphColorSurface(context)
        strokeColor = MorphTheme.morphColorOutlineVariant(context)
    }

    /**
     * 极简白卡片模式。
     *
     * - 背景：纯白（浅色）/ 纯黑（深色）
     * - 边框：0.5dp 极浅灰，模拟 iOS 分组分割线
     * - 阴影：零
     * - 模糊：无
     */
    private fun applyCleanCardState() {
        // 背景色
        setCardBackgroundColor(cardBackgroundColor)

        // 极细边框 — 0.5dp，颜色为 iOS 分组分割线灰
        strokeWidth = strokeWidthPx.toInt()
        strokeColor = this.strokeColor

        // 移除模糊效果
        clearBlurEffect()
    }

    /**
     * 毛玻璃模式。
     *
     * - 背景：半透明白色（浅色 `#CCFFFFFF`）/ 半透明黑色（深色 `#99000000`）
     * - 边框：无（毛玻璃模式下边框会破坏通透感）
     * - 模糊：API 31+ 启用 [RenderEffect.createBlurEffect]，
     *   低版本降级为仅半透明遮罩
     */
    private fun applyGlassmorphismState() {
        // 半透明背景
        val bgColor = if (MorphTheme.isDarkMode(context)) glassmorphismDarkBg else glassmorphismLightBg
        setCardBackgroundColor(bgColor)

        // 毛玻璃模式无边框
        strokeWidth = 0
        strokeColor = Color.TRANSPARENT

        // 尝试启用模糊效果
        applyBlurEffect()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 模糊效果 — API 31+ RenderEffect / 低版本降级
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 应用模糊效果。
     *
     * ### API 31+ (Android 12+)
     *
     * 使用 [RenderEffect.createBlurEffect] 对卡片背后内容做高斯模糊：
     * ```kotlin
     * val blurEffect = RenderEffect.createBlurEffect(
     *     BLUR_RADIUS, BLUR_RADIUS, Shader.TileMode.CLAMP
     * )
     * setRenderEffect(blurEffect)
     * ```
     *
     * **原理**：`View.setRenderEffect` 将 RenderEffect 应用于 View 的渲染管线，
     * 在绘制该 View 时对**已绘制的像素**（包括背后内容透过半透明背景可见的部分）
     * 进行后处理模糊。由于卡片背景是半透明的，背后内容会先被绘制，
     * 然后整个区域被模糊处理，产生类似 iOS UIVisualEffectView 的毛玻璃效果。
     *
     * ### API < 31
     *
     * [RenderEffect] 不可用，降级为仅显示半透明遮罩，
     * 不做任何模糊处理。用户看到的是半透明背景 + 清晰的背后内容，
     * 视觉上虽非毛玻璃但仍保持层次感，且绝不崩溃。
     *
     * **为什么不用 RenderScript / Toolkit 方案？**
     * - RenderScript 在 API 31+ 已废弃
     * - 第三方模糊库（RealtimeBlurView 等）需额外依赖且性能不可控
     * - 降级为纯半透明遮罩是最安全的兜底方案
     */
    private fun applyBlurEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val blurEffect = RenderEffect.createBlurEffect(
                    BLUR_RADIUS,
                    BLUR_RADIUS,
                    Shader.TileMode.CLAMP
                )
                setRenderEffect(blurEffect)
            } catch (e: Exception) {
                // RenderEffect 在极端场景下可能抛异常（如硬件加速关闭），
                // 降级为无模糊
                clearBlurEffect()
            }
        }
        // API < 31: 降级为仅半透明遮罩，无需额外操作
    }

    /**
     * 清除模糊效果。
     *
     * 在从毛玻璃模式切换回极简模式时调用，
     * 将 [RenderEffect] 设为 null 恢复正常渲染。
     */
    private fun clearBlurEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                setRenderEffect(null)
            } catch (_: Exception) {
                // 忽略清除失败
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 暗黑模式适配
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 配置变更回调。
     *
     * 当暗黑模式切换时，系统会触发 [android.content.res.Configuration] 变更，
     * 此时需要刷新颜色缓存并重新应用视觉状态。
     */
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration?) {
        super.onConfigurationChanged(newConfig)
        applyVisualState()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyVisualState()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 常量
    // ═══════════════════════════════════════════════════════════════════════

    companion object {

        /** 极细边框宽度（dp）— 0.5dp 模拟 iOS 分组分割线 */
        private const val STROKE_WIDTH_DP = 0.5f

        /**
         * 模糊半径（px）。
         *
         * 25px 在 2x 密度屏上约等于 12.5dp，在 3x 屏上约等于 8.3dp，
         * 视觉上接近 iOS UIVisualEffectView 的系统级模糊强度。
         * 过小（<15px）模糊不可感知，过大（>40px）导致内容完全不可读。
         */
        private const val BLUR_RADIUS = 25f

        /** 毛玻璃模式浅色背景 — 80% 不透明度白色 */
        private val COLOR_GLASSMORPHISM_LIGHT_BG = 0xCCFFFFFFL.toInt()

        /** 毛玻璃模式深色背景 — 60% 不透明度黑色 */
        private val COLOR_GLASSMORPHISM_DARK_BG = 0x99000000L.toInt()
    }
}
