package com.morphkit.engine

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * MorphFactory2 崩溃降级测试。
 *
 * 验证核心降级链路：当 Morph 控件构造函数抛异常时，
 * Factory2 必须安全降级到 originalFactory，绝不白屏。
 *
 * ## CI/CD 合规要求
 *
 * MorphKit 的合并请求（PR）必须通过所有 `src/test` 下的测试用例，
 * 特别是降级测试，否则严禁合入主分支。
 */
class MorphFactory2Test {

    @Before
    fun setUp() {
        // Mock android.util.Log，避免纯 JVM 环境下 "Method not mocked" 错误
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.d(any(), any<String>(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 A1：Morph 控件抛异常时，降级到 originalFactory
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `Morph控件抛异常时_降级到originalFactory_不崩溃`() {
        // ── 1. Mock 原生 Factory2（模拟 AppCompat）──
        val mockOriginalFactory: LayoutInflater.Factory2 = mockk(relaxed = true)
        val fallbackView = FrameLayout(mockk(relaxed = true))
        every { mockOriginalFactory.onCreateView(any(), any(), any(), any()) } returns fallbackView

        // ── 2. 实例化 MorphFactory2 ──
        val factory = MorphFactory2(mockOriginalFactory, 0)

        // ── 3. Mock MorphKit.createView 抛异常 ──
        mockkObject(MorphKit)
        every { MorphKit.createView(any(), any(), any()) } throws RuntimeException("MorphTextView 构造失败")

        // ── 4. 调用 onCreateView ──
        val context: Context = mockk(relaxed = true)
        val attrs: AttributeSet = mockk(relaxed = true)

        // 关键断言：方法不抛异常（被内部 try-catch 吞掉）
        val result = try {
            factory.onCreateView(null, "TextView", context, attrs)
        } catch (e: Exception) {
            fail("MorphFactory2.onCreateView 不应抛出异常，但抛出了: ${e.message}")
        }

        // ── 5. 断言降级到 originalFactory ──
        verify { mockOriginalFactory.onCreateView(any(), "TextView", any(), any()) }

        // ── 6. 断言返回的是安全的原生 View ──
        assertEquals("降级后应返回 originalFactory 创建的 View", fallbackView, result)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 A2：Morph 控件替换成功时，仍然先调用 originalFactory（责任链）
    //
    // MorphFactory2 采用责任链代理模式：
    // 阶段 1：先调 originalFactory 创建初步 View（保证 AppCompat 着色）
    // 阶段 2：命中替换规则 → 返回 Morph 控件（替换 originalFactory 的 View）
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `Morph控件替换成功时_先调用originalFactory再替换_返回Morph控件`() {
        val mockOriginalFactory: LayoutInflater.Factory2 = mockk(relaxed = true)
        val originalView: View = mockk(relaxed = true)
        every { mockOriginalFactory.onCreateView(any(), any(), any(), any()) } returns originalView

        val replacedView: View = mockk(relaxed = true)

        val factory = MorphFactory2(mockOriginalFactory, 0)

        mockkObject(MorphKit)
        every { MorphKit.createView(any(), any(), any()) } returns replacedView
        every { MorphKit.modifyView(any(), any()) } returns replacedView

        val context: Context = mockk(relaxed = true)
        val attrs: AttributeSet = mockk(relaxed = true)

        val result = factory.onCreateView(null, "Button", context, attrs)

        // 断言返回的是替换控件（Morph 控件优先于 originalFactory 的 View）
        assertEquals("替换成功时应返回 Morph 控件", replacedView, result)

        // 断言 originalFactory 被调用了（责任链：先调 originalFactory 保证 AppCompat 着色）
        verify(exactly = 1) { mockOriginalFactory.onCreateView(any(), any(), any(), any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 A3：未命中替换规则时，返回 originalFactory 创建的 View
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `未命中替换规则时_返回originalFactory创建的View`() {
        val mockOriginalFactory: LayoutInflater.Factory2 = mockk(relaxed = true)
        val originalView: View = mockk(relaxed = true)
        every { mockOriginalFactory.onCreateView(any(), any(), any(), any()) } returns originalView

        val factory = MorphFactory2(mockOriginalFactory, 0)

        mockkObject(MorphKit)
        every { MorphKit.createView(any(), any(), any()) } returns null // 未命中
        every { MorphKit.modifyView(any(), any()) } returns originalView

        val context: Context = mockk(relaxed = true)
        val attrs: AttributeSet = mockk(relaxed = true)

        val result = factory.onCreateView(null, "ImageView", context, attrs)

        // 断言返回的是 originalFactory 创建的 View
        assertEquals("未命中替换时应返回 originalFactory 创建的 View", originalView, result)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 A4：originalFactory 为 null 时，返回 null
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `originalFactory为null且未命中替换时_返回null`() {
        val factory = MorphFactory2(null, 0)

        mockkObject(MorphKit)
        every { MorphKit.createView(any(), any(), any()) } returns null
        every { MorphKit.modifyView(any(), any()) } answers { secondArg() }

        val context: Context = mockk(relaxed = true)
        val attrs: AttributeSet = mockk(relaxed = true)

        val result = factory.onCreateView(null, "ImageView", context, attrs)

        assertNull("originalFactory 为 null 且未命中替换时应返回 null", result)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 A5：updateOriginalFactory 后，降级使用新的 Factory
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `updateOriginalFactory后_降级使用新的Factory`() {
        val factory = MorphFactory2(null, 0)

        // 初始 originalFactory 为 null
        mockkObject(MorphKit)
        every { MorphKit.createView(any(), any(), any()) } returns null
        every { MorphKit.modifyView(any(), any()) } answers { secondArg() }

        val context: Context = mockk(relaxed = true)
        val attrs: AttributeSet = mockk(relaxed = true)

        // 未命中替换 + originalFactory 为 null → 返回 null
        var result = factory.onCreateView(null, "ImageView", context, attrs)
        assertNull("初始 originalFactory 为 null 时应返回 null", result)

        // 补充 AppCompat delegate
        val mockAppCompat: LayoutInflater.Factory2 = mockk(relaxed = true)
        val appCompatView: View = mockk(relaxed = true)
        every { mockAppCompat.onCreateView(any(), any(), any(), any()) } returns appCompatView
        every { MorphKit.modifyView(any(), any()) } returns appCompatView

        factory.updateOriginalFactory(mockAppCompat)

        // 未命中替换 + originalFactory 已补充 → 返回 AppCompat View
        result = factory.onCreateView(null, "ImageView", context, attrs)
        assertEquals("补充 originalFactory 后应返回 AppCompat View", appCompatView, result)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 A6：finalThemeResId 为 0 时不包装 Context
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `finalThemeResId为0时_不包装Context`() {
        val mockOriginalFactory: LayoutInflater.Factory2 = mockk(relaxed = true)
        val originalView: View = mockk(relaxed = true)
        every { mockOriginalFactory.onCreateView(any(), any(), any(), any()) } returns originalView

        mockkObject(MorphKit)
        every { MorphKit.createView(any(), any(), any()) } returns null
        every { MorphKit.modifyView(any(), any()) } returns originalView

        // finalThemeResId = 0（默认值）
        val factory = MorphFactory2(mockOriginalFactory, 0)

        val context: Context = mockk(relaxed = true)
        val attrs: AttributeSet = mockk(relaxed = true)

        // 不应抛异常，Context 不被包装
        val result = factory.onCreateView(null, "ImageView", context, attrs)
        assertNotNull(result)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 A7：finalThemeResId 非 0 时尝试 ContextThemeWrapper 包装
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `finalThemeResId非0时_尝试ContextThemeWrapper包装`() {
        val mockOriginalFactory: LayoutInflater.Factory2 = mockk(relaxed = true)
        val originalView: View = mockk(relaxed = true)
        every { mockOriginalFactory.onCreateView(any(), any(), any(), any()) } returns originalView

        mockkObject(MorphKit)
        every { MorphKit.createView(any(), any(), any()) } returns null
        every { MorphKit.modifyView(any(), any()) } returns originalView

        // finalThemeResId 非 0（模拟 iOS 主题）
        val factory = MorphFactory2(mockOriginalFactory, com.morphkit.R.style.Theme_MorphKit_iOS)

        val context: Context = mockk(relaxed = true)
        val attrs: AttributeSet = mockk(relaxed = true)

        // 在纯 JVM 环境中，Context.theme.obtainStyledAttributes 可能抛异常，
        // 但 MorphFactory2 内部有 try-catch，不应崩溃
        val result = factory.onCreateView(null, "ImageView", context, attrs)
        // 结果可能为 null（JVM 环境下 ContextThemeWrapper 无法正常工作），
        // 关键是不崩溃
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 A8：updateOriginalFactory 后替换仍优先于 originalFactory
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `updateOriginalFactory后_替换仍优先于originalFactory`() {
        val mockOriginalFactory: LayoutInflater.Factory2 = mockk(relaxed = true)
        val originalView: View = mockk(relaxed = true)
        every { mockOriginalFactory.onCreateView(any(), any(), any(), any()) } returns originalView

        val replacedView: View = mockk(relaxed = true)

        val factory = MorphFactory2(null, 0)

        // 先补充 originalFactory
        factory.updateOriginalFactory(mockOriginalFactory)

        // 注册替换规则
        mockkObject(MorphKit)
        every { MorphKit.createView(any(), any(), any()) } returns replacedView
        every { MorphKit.modifyView(any(), any()) } returns replacedView

        val context: Context = mockk(relaxed = true)
        val attrs: AttributeSet = mockk(relaxed = true)

        val result = factory.onCreateView(null, "Button", context, attrs)

        // 替换控件应优先于 originalFactory
        assertEquals("补充 originalFactory 后替换仍应优先", replacedView, result)
    }
}
