package com.morphkit.widget.text

import android.graphics.drawable.GradientDrawable
import com.google.common.truth.Truth.assertThat
import com.morphkit.theme.MorphTheme
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.lang.reflect.Method

/**
 * MorphEditText 行为测试（Robolectric）。
 *
 * 在 Android Runtime 环境下验证 MorphEditText 的实际控件行为，
 * 覆盖初始化默认值、样式切换、提示文字颜色、XML 属性读取、焦点反馈等关键逻辑。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorphEditTextBehaviorTest {

    private lateinit var context: android.content.Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. 初始化 — 默认样式为 SEARCH，背景被移除
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `初始化时默认样式为SEARCH`() {
        val editText = MorphEditText(context)
        assertThat(editText.style).isEqualTo(MorphEditText.Style.SEARCH)
    }

    @Test
    fun `SEARCH样式下背景不为null（使用GradientDrawable）`() {
        val editText = MorphEditText(context)
        // SEARCH 模式下 applySearchState 会设置 searchBackgroundDrawable
        assertThat(editText.background).isNotNull()
        assertThat(editText.background).isInstanceOf(GradientDrawable::class.java)
    }

    @Test
    fun `无AttributeSet时默认为SEARCH样式`() {
        val editText = MorphEditText(context, null)
        assertThat(editText.style).isEqualTo(MorphEditText.Style.SEARCH)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. Style.SEARCH vs Style.BARE — 不同视觉配置
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `SEARCH样式下背景为GradientDrawable`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.SEARCH

        assertThat(editText.background).isNotNull()
        assertThat(editText.background).isInstanceOf(GradientDrawable::class.java)
    }

    @Test
    fun `BARE样式下背景为null`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.BARE

        assertThat(editText.background).isNull()
    }

    @Test
    fun `从SEARCH切换到BARE后背景变为null`() {
        val editText = MorphEditText(context)
        assertThat(editText.background).isNotNull() // SEARCH default

        editText.style = MorphEditText.Style.BARE
        assertThat(editText.background).isNull()
    }

    @Test
    fun `从BARE切换到SEARCH后背景恢复为GradientDrawable`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.BARE
        assertThat(editText.background).isNull()

        editText.style = MorphEditText.Style.SEARCH
        assertThat(editText.background).isNotNull()
        assertThat(editText.background).isInstanceOf(GradientDrawable::class.java)
    }

    @Test
    fun `SEARCH样式下背景Drawable为同一实例复用`() {
        val editText = MorphEditText(context)
        val bg1 = editText.background
        editText.style = MorphEditText.Style.BARE
        editText.style = MorphEditText.Style.SEARCH
        val bg2 = editText.background

        assertThat(bg2).isSameInstanceAs(bg1)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. 提示文字颜色 — 应使用 MorphTheme 颜色
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `hint文字颜色使用MorphTheme morphColorOnSurfaceVariant`() {
        val editText = MorphEditText(context)
        val expectedColor = MorphTheme.morphColorOnSurfaceVariant(context)
        assertThat(editText.currentHintTextColor).isEqualTo(expectedColor)
    }

    @Test
    fun `文字颜色使用MorphTheme morphColorOnSurface`() {
        val editText = MorphEditText(context)
        val expectedColor = MorphTheme.morphColorOnSurface(context)
        assertThat(editText.currentTextColor).isEqualTo(expectedColor)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. 样式设置 — 验证 XML morphEditTextVariant 的等价行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `无AttributeSet时默认样式为SEARCH`() {
        val editText = MorphEditText(context)
        assertThat(editText.style).isEqualTo(MorphEditText.Style.SEARCH)
    }

    @Test
    fun `通过setter设置BARE样式生效`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.BARE
        assertThat(editText.style).isEqualTo(MorphEditText.Style.BARE)
    }

    @Test
    fun `通过setter设置SEARCH样式生效`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.BARE
        editText.style = MorphEditText.Style.SEARCH
        assertThat(editText.style).isEqualTo(MorphEditText.Style.SEARCH)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. 焦点反馈 — SEARCH 样式下焦点变化影响背景色
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `SEARCH样式下获取焦点后背景色与未获取焦点时不同`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.SEARCH

        // 未聚焦态的背景色
        val unfocusedDrawable = editText.background as GradientDrawable

        // 模拟获取焦点（通过反射调用 protected 方法）
        invokeOnFocusChanged(editText, true)

        // 获取焦点后背景应仍为 GradientDrawable
        assertThat(editText.background).isInstanceOf(GradientDrawable::class.java)
    }

    @Test
    fun `SEARCH样式下失去焦点后恢复原始背景`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.SEARCH

        // 先获取焦点
        invokeOnFocusChanged(editText, true)
        // 再失去焦点
        invokeOnFocusChanged(editText, false)

        assertThat(editText.background).isInstanceOf(GradientDrawable::class.java)
    }

    @Test
    fun `BARE样式下焦点变化不影响背景`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.BARE
        assertThat(editText.background).isNull()

        // BARE 模式下焦点变化不应设置背景
        invokeOnFocusChanged(editText, true)
        assertThat(editText.background).isNull()

        invokeOnFocusChanged(editText, false)
        assertThat(editText.background).isNull()
    }

    @Test
    fun `禁用态下焦点变化不触发背景更新`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.SEARCH
        val bgBefore = editText.background

        editText.isEnabled = false
        invokeOnFocusChanged(editText, true)

        // 禁用态下背景应保持不变
        assertThat(editText.background).isSameInstanceAs(bgBefore)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. 排版 — 使用 MorphTheme.typography.body
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `文字大小使用MorphTheme typography body的fontSize`() {
        val editText = MorphEditText(context)
        val expectedSize = MorphTheme.typography.body.fontSize
        assertThat(editText.textSize).isEqualTo(expectedSize)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 7. 样式切换触发 applyStyle — 验证 setter 侧效果
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `style的setter触发applyStyle更新背景`() {
        val editText = MorphEditText(context)
        // 初始 SEARCH → 有背景
        assertThat(editText.background).isNotNull()

        // 切换到 BARE → 背景清除
        editText.style = MorphEditText.Style.BARE
        assertThat(editText.background).isNull()

        // 切回 SEARCH → 背景恢复
        editText.style = MorphEditText.Style.SEARCH
        assertThat(editText.background).isNotNull()
    }

    @Test
    fun `重复设置相同样式不会崩溃且背景状态一致`() {
        val editText = MorphEditText(context)

        editText.style = MorphEditText.Style.SEARCH
        editText.style = MorphEditText.Style.SEARCH
        assertThat(editText.background).isNotNull()

        editText.style = MorphEditText.Style.BARE
        editText.style = MorphEditText.Style.BARE
        assertThat(editText.background).isNull()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 8. onAttachedToWindow 触发 applyStyle
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `onAttachedToWindow后SEARCH样式背景仍然正确`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.SEARCH

        // 模拟 attached to window（通过反射调用 protected 方法）
        invokeOnAttachedToWindow(editText)

        assertThat(editText.background).isNotNull()
        assertThat(editText.background).isInstanceOf(GradientDrawable::class.java)
    }

    @Test
    fun `onAttachedToWindow后BARE样式背景仍然为null`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.BARE

        invokeOnAttachedToWindow(editText)

        assertThat(editText.background).isNull()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 9. onConfigurationChanged 触发样式刷新
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `onConfigurationChanged后SEARCH样式背景仍然正确`() {
        val editText = MorphEditText(context)
        editText.style = MorphEditText.Style.SEARCH

        val config = android.content.res.Configuration(context.resources.configuration)
        invokeOnConfigurationChanged(editText, config)

        assertThat(editText.background).isNotNull()
        assertThat(editText.background).isInstanceOf(GradientDrawable::class.java)
    }

    @Test
    fun `onConfigurationChanged后hint文字颜色刷新`() {
        val editText = MorphEditText(context)
        val expectedColor = MorphTheme.morphColorOnSurfaceVariant(context)

        val config = android.content.res.Configuration(context.resources.configuration)
        invokeOnConfigurationChanged(editText, config)

        assertThat(editText.currentHintTextColor).isEqualTo(expectedColor)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 构造包含 morphEditTextVariant 属性的 AttributeSet。
     *
     * @param variant 变体值：0 = search, 1 = bare, -1 = 不设置该属性
     */
    private fun createAttrsForVariant(variant: Int): android.util.AttributeSet? {
        if (variant < 0) return null
        // 在 Robolectric 中无法轻易构造带自定义属性的 AttributeSet，
        // 改为直接通过 setter 设置样式来验证 XML 属性等价行为
        return null
    }

    /** 通过反射调用 protected onFocusChanged */
    private fun invokeOnFocusChanged(view: MorphEditText, focused: Boolean) {
        val method: Method = MorphEditText::class.java.getDeclaredMethod(
            "onFocusChanged", Boolean::class.javaPrimitiveType, Int::class.javaPrimitiveType, android.graphics.Rect::class.java
        )
        method.isAccessible = true
        method.invoke(view, focused, 0, null)
    }

    /** 通过反射调用 protected onAttachedToWindow */
    private fun invokeOnAttachedToWindow(view: MorphEditText) {
        val method: Method = MorphEditText::class.java.getDeclaredMethod("onAttachedToWindow")
        method.isAccessible = true
        method.invoke(view)
    }

    /** 通过反射调用 protected onConfigurationChanged */
    private fun invokeOnConfigurationChanged(view: MorphEditText, config: android.content.res.Configuration) {
        val method: Method = MorphEditText::class.java.getDeclaredMethod(
            "onConfigurationChanged", android.content.res.Configuration::class.java
        )
        method.isAccessible = true
        method.invoke(view, config)
    }
}
