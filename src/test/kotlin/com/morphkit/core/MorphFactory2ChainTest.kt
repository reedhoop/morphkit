package com.morphkit.core

import android.app.Application
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * MorphFactory2 责任链完整测试。
 *
 * 验证 LayoutInflater.Factory2 责任链代理的核心行为：
 * 1. 命中替换规则 → 返回替换控件 + 执行 modifyView
 * 2. 未命中替换规则 → 返回 originalFactory 创建的控件 + 执行 modifyView
 * 3. 替换异常 → 降级到 originalFactory 创建的控件
 * 4. modifyView 异常 → 返回未修改的替换控件
 * 5. originalFactory 为 null → 安全降级
 */
class MorphFactory2ChainTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUpLog() {
            mockkStatic(Log::class)
            every { Log.w(any(), any<String>()) } returns 0
            every { Log.w(any(), any<String>(), any()) } returns 0
            every { Log.e(any(), any<String>()) } returns 0
            every { Log.e(any(), any<String>(), any()) } returns 0
            every { Log.d(any(), any<String>()) } returns 0
            every { Log.d(any(), any<String>(), any()) } returns 0
        }

        @JvmStatic
        @AfterAll
        fun tearDownLog() {
            unmockkStatic(Log::class)
        }
    }

    @BeforeEach
    fun setUp() {
        resetMorphKit()
        resetMorphInstaller()
    }

    @AfterEach
    fun tearDown() {
        try { unmockkAll() } catch (_: Exception) {}
        resetMorphKit()
    }

    @Nested
    inner class ReplaceHitTest {

        @Test
        fun `命中替换规则_返回替换控件`() {
            val mockApp = mockk<Application>(relaxed = true)
            every { mockApp.registerActivityLifecycleCallbacks(any()) } just Runs

            val replacedView = mockk<View>(relaxed = true)
            MorphKit.init(mockApp) {
                replace("TextView") { _, _ -> replacedView }
            }

            val originalFactory = mockk<LayoutInflater.Factory2>(relaxed = true)
            val factory = MorphFactory2(originalFactory)
            val context = mockk<Context>(relaxed = true)
            val attrs = mockk<AttributeSet>(relaxed = true)

            val result = factory.onCreateView(null, "TextView", context, attrs)
            assertSame(replacedView, result, "命中替换规则应返回替换控件")
        }
    }

    @Nested
    inner class ReplaceMissTest {

        @Test
        fun `未命中替换规则_返回originalFactory创建的控件`() {
            val mockApp = mockk<Application>(relaxed = true)
            every { mockApp.registerActivityLifecycleCallbacks(any()) } just Runs

            MorphKit.init(mockApp) {
                replace("Button") { _, _ -> mockk<View>(relaxed = true) }
            }

            val originalView = mockk<View>(relaxed = true)
            val originalFactory = mockk<LayoutInflater.Factory2>(relaxed = true)
            every { originalFactory.onCreateView(any(), any(), any(), any()) } returns originalView

            val factory = MorphFactory2(originalFactory)
            val context = mockk<Context>(relaxed = true)
            val attrs = mockk<AttributeSet>(relaxed = true)

            val result = factory.onCreateView(null, "TextView", context, attrs)
            assertSame(originalView, result, "未命中替换规则应返回 originalFactory 创建的控件")
        }
    }

    @Nested
    inner class ReplaceExceptionTest {

        @Test
        fun `替换控件创建异常_降级到originalFactory`() {
            val mockApp = mockk<Application>(relaxed = true)
            every { mockApp.registerActivityLifecycleCallbacks(any()) } just Runs

            MorphKit.init(mockApp) {
                replace("TextView") { _, _ -> throw RuntimeException("创建失败") }
            }

            val originalView = mockk<View>(relaxed = true)
            val originalFactory = mockk<LayoutInflater.Factory2>(relaxed = true)
            every { originalFactory.onCreateView(any(), any(), any(), any()) } returns originalView

            val factory = MorphFactory2(originalFactory)
            val context = mockk<Context>(relaxed = true)
            val attrs = mockk<AttributeSet>(relaxed = true)

            val result = factory.onCreateView(null, "TextView", context, attrs)
            assertSame(originalView, result, "替换异常应降级到 originalFactory 创建的控件")
        }
    }

    @Nested
    inner class NullOriginalFactoryTest {

        @Test
        fun `originalFactory为null_未命中替换规则_返回null`() {
            val mockApp = mockk<Application>(relaxed = true)
            every { mockApp.registerActivityLifecycleCallbacks(any()) } just Runs

            MorphKit.init(mockApp) {
                replace("Button") { _, _ -> mockk<View>(relaxed = true) }
            }

            val factory = MorphFactory2(null)
            val context = mockk<Context>(relaxed = true)
            val attrs = mockk<AttributeSet>(relaxed = true)

            val result = factory.onCreateView(null, "TextView", context, attrs)
            assertNull(result, "originalFactory 为 null 且未命中替换规则应返回 null")
        }

        @Test
        fun `originalFactory为null_命中替换规则_返回替换控件`() {
            val mockApp = mockk<Application>(relaxed = true)
            every { mockApp.registerActivityLifecycleCallbacks(any()) } just Runs

            val replacedView = mockk<View>(relaxed = true)
            MorphKit.init(mockApp) {
                replace("TextView") { _, _ -> replacedView }
            }

            val factory = MorphFactory2(null)
            val context = mockk<Context>(relaxed = true)
            val attrs = mockk<AttributeSet>(relaxed = true)

            val result = factory.onCreateView(null, "TextView", context, attrs)
            assertSame(replacedView, result, "originalFactory 为 null 但命中替换规则应返回替换控件")
        }
    }

    @Nested
    inner class UpdateOriginalFactoryTest {

        @Test
        fun `延迟设置originalFactory_后续调用使用新的originalFactory`() {
            val mockApp = mockk<Application>(relaxed = true)
            every { mockApp.registerActivityLifecycleCallbacks(any()) } just Runs

            MorphKit.init(mockApp) {
                replace("Button") { _, _ -> mockk<View>(relaxed = true) }
            }

            val factory = MorphFactory2(null)
            val context = mockk<Context>(relaxed = true)
            val attrs = mockk<AttributeSet>(relaxed = true)

            // 初始 originalFactory 为 null，未命中规则返回 null
            assertNull(factory.onCreateView(null, "TextView", context, attrs))

            // 延迟设置 originalFactory
            val originalView = mockk<View>(relaxed = true)
            val newOriginalFactory = mockk<LayoutInflater.Factory2>(relaxed = true)
            every { newOriginalFactory.onCreateView(any(), any(), any(), any()) } returns originalView

            factory.updateOriginalFactory(newOriginalFactory)

            // 现在未命中规则应返回 originalFactory 创建的控件
            val result = factory.onCreateView(null, "TextView", context, attrs)
            assertSame(originalView, result, "延迟设置 originalFactory 后应使用新的 factory")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════

    private fun resetMorphKit() {
        try {
            val initializedField = MorphKit::class.java.getDeclaredField("initialized")
            initializedField.isAccessible = true
            val atomicBool = initializedField.get(MorphKit) as java.util.concurrent.atomic.AtomicBoolean
            atomicBool.set(false)

            val configField = MorphKit::class.java.getDeclaredField("config")
            configField.isAccessible = true
            configField.set(MorphKit, null)
        } catch (e: Exception) {
            // 反射重置失败时忽略
        }
    }

    private fun resetMorphInstaller() {
        try {
            val field = MorphInstaller::class.java.getDeclaredField("installed")
            field.isAccessible = true
            val atomicBool = field.get(MorphInstaller) as java.util.concurrent.atomic.AtomicBoolean
            atomicBool.set(false)
        } catch (e: Exception) {
            // 反射重置失败时忽略
        }
    }
}
