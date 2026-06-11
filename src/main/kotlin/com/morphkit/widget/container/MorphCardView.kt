package com.morphkit.widget.container

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.card.MaterialCardView
import com.morphkit.R
import com.morphkit.theme.MorphTheme
import com.morphkit.theme.MorphTokens
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
 * - 通过截取父容器背景、高斯模糊后作为卡片背景图层显示，
 *   实现真正的「behind-blur」效果（不会模糊卡片内部文字和子 View）
 * - API 31+ 使用 [android.graphics.RenderEffect] GPU 加速模糊
 * - 低版本降级为软件 Stack Blur（API < 29）或无模糊直传（API 29–30）
 * - 自动裁剪子 View 到圆角范围内
 *
 * ## 使用方式
 *
 * ```xml
 * <!-- 极简白卡片（默认） -->
 * <com.morphkit.widget.container.MorphCardView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content">
 *     <!-- 子 View -->
 * </com.morphkit.widget.container.MorphCardView>
 *
 * <!-- 毛玻璃卡片 -->
 * <com.morphkit.widget.container.MorphCardView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:isGlassmorphism="true" />
 *
 * <!-- 自定义模糊半径 -->
 * <com.morphkit.widget.container.MorphCardView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:isGlassmorphism="true"
 *     app:glassmorphismBlurRadius="30" />
 * ```
 *
 * @see MorphTheme.cornerLarge 圆角大小
 * @see isGlassmorphism 毛玻璃模式开关
 * @see refreshGlassmorphismBlur 手动刷新模糊背景
 */
class MorphCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphCardStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    // ═══════════════════════════════════════════════════════════════════════
    // 内部状态
    // ═══════════════════════════════════════════════════════════════════════

    /** 模糊半径（px），可通过 XML 或 setter 修改 */
    private var blurRadius: Float = DEFAULT_BLUR_RADIUS

    /**
     * 毛玻璃模式开关。
     *
     * - `true`：背景变为半透明白色（浅色模式）/ 半透明黑色（深色模式），
     *   截取父容器背景并模糊后作为卡片背景图层，
     *   实现类似 iOS UIVisualEffectView 的毛玻璃效果。
     * - `false`（默认）：极简白卡片模式，纯色背景 + 极细边框。
     *
     * 模糊效果仅作用于卡片**背后**的内容，不会影响卡片内部的文字和子 View。
     * 模糊半径为 [blurRadius] px。
     */
    var isGlassmorphism: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                applyVisualState()
            }
        }

    /**
     * 毛玻璃模糊半径（px）。
     *
     * 推荐范围 15–35px。过小（<15px）模糊不可感知，过大（>40px）导致内容完全不可读。
     * 修改后会自动刷新模糊效果（仅在毛玻璃模式下）。
     */
    var glassmorphismBlurRadius: Float
        get() = blurRadius
        set(value) {
            if (blurRadius != value) {
                blurRadius = value
                if (isGlassmorphism && isAttachedToWindow) {
                    updateBlurBackground()
                }
            }
        }

    /** 极细边框宽度（0.5dp → px） */
    private val strokeWidthPx: Float = STROKE_WIDTH_DP.dp(context)

    /** 缓存：极简模式背景色 */
    private var cachedCardBgColor: Int = MorphTheme.morphColorSurface(context)

    /** 缓存：极简模式边框色（极浅灰，模拟 iOS 分组分割线） */
    private var cachedStrokeColor: Int = MorphTheme.morphColorOutlineVariant(context)

    /** 毛玻璃模式背景色（浅色） */
    private val glassmorphismLightBg: Int = COLOR_GLASSMORPHISM_LIGHT_BG

    /** 毛玻璃模式背景色（深色） */
    private val glassmorphismDarkBg: Int = COLOR_GLASSMORPHISM_DARK_BG

    /** 模糊背景图层 — 仅在毛玻璃模式下创建 */
    private var blurBackgroundView: ImageView? = null

    // ═══════════════════════════════════════════════════════════════════════
    // 初始化
    // ═══════════════════════════════════════════════════════════════════════

    init {
        // ── 关闭 Material 默认阴影动画 ──
        // MaterialCardView 默认带 elevation 阴影和按压态阴影动画，
        // 与 iOS 极简风格完全冲突，必须彻底清除
        cardElevation = MorphTokens.elevationNone.toFloat()
        maxCardElevation = MorphTokens.elevationNone.toFloat()

        // ── 关闭涟漪效果 ──
        // MaterialCardView 默认在可点击时显示 Ripple 涟漪，
        // iOS 卡片无涟漪反馈，需禁用
        isClickable = false
        rippleColor = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)

        // ── 关闭状态动画 ──
        // 禁用按压态阴影变化动画
        stateListAnimator = null

        // ── 圆角 ──
        radius = MorphTheme.cornerLarge(context).toFloat()

        // ── 裁剪子 View 到圆角范围内 ──
        // 毛玻璃模式下子 View 不能溢出圆角区域
        clipChildren = true
        clipToPadding = true

        // ── 读取 XML 属性 ──
        attrs?.let { readXmlAttributes(it) }

        // ── 应用视觉状态 ──
        applyVisualState()
    }

    /**
     * 从 XML AttributeSet 读取 MorphCardView 自定义属性。
     */
    private fun readXmlAttributes(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.MorphCardView)
        try {
            isGlassmorphism = ta.getBoolean(
                R.styleable.MorphCardView_isGlassmorphism, false
            )
            blurRadius = ta.getFloat(
                R.styleable.MorphCardView_glassmorphismBlurRadius, DEFAULT_BLUR_RADIUS
            )
        } finally {
            ta.recycle()
        }
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
        cachedCardBgColor = MorphTheme.morphColorSurface(context)
        cachedStrokeColor = MorphTheme.morphColorOutlineVariant(context)
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
        setCardBackgroundColor(cachedCardBgColor)

        // 极细边框 — 0.5dp，颜色为 iOS 分组分割线灰
        strokeWidth = strokeWidthPx.toInt()
        strokeColor = cachedStrokeColor

        // 移除模糊背景图层
        removeBlurBackgroundView()
    }

    /**
     * 毛玻璃模式。
     *
     * - 背景：半透明白色（浅色 `#CCFFFFFF`）/ 半透明黑色（深色 `#99000000`）
     * - 边框：无（毛玻璃模式下边框会破坏通透感）
     * - 模糊：截取父容器背景并高斯模糊，作为卡片背景图层显示
     */
    private fun applyGlassmorphismState() {
        // 确保模糊背景 ImageView 已创建并位于最底层
        ensureBlurBackgroundView()

        // 半透明背景（作为模糊图层的底色叠加）
        val bgColor = if (MorphTheme.isDarkMode(context)) glassmorphismDarkBg else glassmorphismLightBg
        setCardBackgroundColor(bgColor)

        // 毛玻璃模式无边框
        strokeWidth = 0
        strokeColor = Color.TRANSPARENT

        // 延迟到下一次 layout 后截取并模糊
        requestBlurUpdate()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 模糊背景图层管理
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 确保 [blurBackgroundView] 已创建并作为第一个子 View 插入。
     *
     * 由于 MaterialCardView 内部使用 FrameLayout 容纳子 View，
     * 将 ImageView 插入 index 0 可确保它位于所有用户子 View 之下，
     * 同时在 MaterialCardView 的 ShapeDrawable（半透明背景色）之上。
     * MaterialCardView 的 `clipToOutline` 会自动裁剪到圆角范围。
     */
    private fun ensureBlurBackgroundView() {
        if (blurBackgroundView != null) return

        val iv = ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_XY
            isClickable = false
            isFocusable = false
            // 不允许拦截触摸事件
            isEnabled = true
        }
        blurBackgroundView = iv
        addView(iv, 0, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }

    /**
     * 移除模糊背景图层。
     *
     * 在从毛玻璃模式切换回极简模式时调用。
     */
    private fun removeBlurBackgroundView() {
        blurBackgroundView?.let {
            // 先解除 BitmapDrawable 引用，再将 Bitmap 归还对象池，最后移除 View
            val bitmapDrawable = it.drawable as? BitmapDrawable
            it.setImageDrawable(null)
            bitmapDrawable?.bitmap?.let { bmp -> BackdropBlurHelper.recycleToPool(bmp) }
            removeView(it)
        }
        blurBackgroundView = null
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 模糊效果 — 截取父容器背景 + 高斯模糊
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 请求在下一个 layout pass 后更新模糊背景。
     *
     * 使用 [post] 延迟执行，确保父容器的子 View 布局已完成，
     * 此时 parent.draw() 能捕获到正确的像素。
     */
    private fun requestBlurUpdate() {
        post { updateBlurBackground() }
    }

    /**
     * 截取父容器在卡片区域的像素，模糊后设为背景图层。
     *
     * 此方法在以下时机自动调用：
     * - 毛玻璃模式启用后的下一次 layout 完成后
     * - [onLayout] 中卡片尺寸或位置变化时
     *
     * 也可通过 [refreshGlassmorphismBlur] 手动调用。
     */
    private fun updateBlurBackground() {
        if (!isGlassmorphism || isInEditMode) return
        if (width <= 0 || height <= 0) return

        val iv = blurBackgroundView ?: return
        val parentView = parent as? ViewGroup ?: return

        // 1. 截取父容器在卡片区域的像素（临时隐藏卡片自身）
        val parentCapture = BackdropBlurHelper.captureParentArea(this) ?: return

        // 2. 高斯模糊
        val blurred = BackdropBlurHelper.blur(parentCapture, blurRadius)

        // 3. 将截取 Bitmap 归还对象池
        BackdropBlurHelper.recycleToPool(parentCapture)

        if (blurred == null) return

        // 4. 先解除旧 BitmapDrawable 引用，设置新 Bitmap，再将旧 Bitmap 归还对象池
        val oldBitmapDrawable = iv.drawable as? BitmapDrawable
        iv.setImageDrawable(BitmapDrawable(resources, blurred))
        oldBitmapDrawable?.bitmap?.let { bmp -> BackdropBlurHelper.recycleToPool(bmp) }
    }

    /**
     * 手动刷新毛玻璃模糊效果。
     *
     * 当卡片背后的内容发生变化（如图片加载完成、列表滚动停止等）时，
     * 调用此方法重新截取并模糊背景。
     *
     * ```kotlin
     * // 示例：RecyclerView 滚动停止后刷新模糊
     * recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
     *     override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
     *         if (newState == RecyclerView.SCROLL_STATE_IDLE) {
     *             morphCard.refreshGlassmorphismBlur()
     *         }
     *     }
     * })
     * ```
     */
    fun refreshGlassmorphismBlur() {
        if (isGlassmorphism && isAttachedToWindow) {
            updateBlurBackground()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 生命周期回调
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 布局完成后自动刷新模糊效果。
     *
     * 当卡片在父容器中的位置或尺寸发生变化时（如屏幕旋转、列表滚动），
     * 需要重新截取对应位置的父容器像素。
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (isGlassmorphism && changed && isAttachedToWindow) {
            requestBlurUpdate()
        }
    }

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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 将模糊 Bitmap 归还对象池释放内存，并置空引用防止 Context 泄漏
        blurBackgroundView?.let { iv ->
            (iv.drawable as? BitmapDrawable)?.bitmap?.let { bmp ->
                BackdropBlurHelper.recycleToPool(bmp)
            }
            iv.setImageDrawable(null)
        }
        blurBackgroundView = null
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 常量
    // ═══════════════════════════════════════════════════════════════════════

    companion object {

        /** 极细边框宽度（dp）— 0.5dp 模拟 iOS 分组分割线 */
        private const val STROKE_WIDTH_DP = 0.5f

        /**
         * 默认模糊半径（px）。
         *
         * 25px 在 2x 密度屏上约等于 12.5dp，在 3x 屏上约等于 8.3dp，
         * 视觉上接近 iOS UIVisualEffectView 的系统级模糊强度。
         * 过小（<15px）模糊不可感知，过大（>40px）导致内容完全不可读。
         */
        private const val DEFAULT_BLUR_RADIUS = 25f

        /** 毛玻璃模式浅色背景 — 80% 不透明度白色 */
        private val COLOR_GLASSMORPHISM_LIGHT_BG = 0xCCFFFFFFL.toInt()

        /** 毛玻璃模式深色背景 — 60% 不透明度黑色 */
        private val COLOR_GLASSMORPHISM_DARK_BG = 0x99000000L.toInt()
    }
}
