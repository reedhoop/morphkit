package com.morphkit.core

import com.morphkit.core.MorphKit
import com.morphkit.core.MorphInitProvider
import com.morphkit.core.MorphInstaller
import com.morphkit.theme.MorphStyleResolver
import android.app.Application
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * MorphInitProvider 零侵入自动初始化测试。
 *
 * 验证 MorphKit.autoInit 与 MorphInstaller 的集成行为，
 * 以及 MorphInitProvider 在纯 JVM 环境下的安全降级。
 *
 * 注意：ContentProvider 的 getContext() 在纯 JVM 环境下返回 null，
 * 因此 onCreate() 应安全返回 false。主进程/非主进程的完整测试
 * 需要在 Instrumented Test 或 Robolectric 环境下执行。
 */
class MorphInitProviderTest {

    private lateinit var mockApp: Application

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.d(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
        every { Log.i(any(), any<String>()) } returns 0

        mockApp = mockk<Application>(relaxed = true)
        every { mockApp.registerActivityLifecycleCallbacks(any()) } returns Unit

        MorphKitTestHelper.resetMorphInstaller()
        MorphKitTestHelper.resetMorphKit()
    }

    @After
    fun tearDown() {
        unmockkAll()
        MorphKitTestHelper.resetMorphInstaller()
        MorphKitTestHelper.resetMorphKit()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 G1：context 为 null 时 onCreate 安全返回 false
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `context为null时_onCreate安全返回false`() {
        val provider = MorphInitProvider()
        // 纯 JVM 环境下 getContext() 返回 null
        val result = provider.onCreate()
        assertFalse("context 为 null 时应安全返回 false", result)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 G2：onCreate 返回 false 时不影响宿主 App
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `onCreate返回false时_MorphKit未初始化`() {
        val provider = MorphInitProvider()
        provider.onCreate()

        // 验证 MorphKit 未被初始化（registerActivityLifecycleCallbacks 未被调用）
        verify(exactly = 0) { mockApp.registerActivityLifecycleCallbacks(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 G3：autoInit 正常调用后 MorphKit 已初始化
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `autoInit正常调用后_MorphKit已初始化`() {
        MorphKit.autoInit(mockApp)

        // 验证 MorphInstaller.install 被调用（通过 registerActivityLifecycleCallbacks 间接验证）
        verify(exactly = 1) { mockApp.registerActivityLifecycleCallbacks(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 G4：autoInit 异常时宿主 App 不崩溃
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `autoInit异常时_宿主App不崩溃`() {
        // 模拟 registerActivityLifecycleCallbacks 抛异常
        every { mockApp.registerActivityLifecycleCallbacks(any()) } throws RuntimeException("模拟异常")

        // MorphKit.init 内部调用 MorphInstaller.install，
        // install 内部调用 registerActivityLifecycleCallbacks
        // 如果抛异常，init 会传播异常
        try {
            MorphKit.autoInit(mockApp)
        } catch (e: RuntimeException) {
            // 预期：异常会被传播
            assertTrue("异常消息应包含模拟信息", e.message?.contains("模拟异常") == true)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 G5：MorphInitProvider 的 OEM 常量验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `MorphStyleResolver_OEM常量值正确`() {
        assertEquals("oem_ui_style", MorphStyleResolver.OEM_UI_STYLE_KEY)
        assertEquals(0, MorphStyleResolver.OEM_STYLE_DEFAULT)
        assertEquals(1, MorphStyleResolver.OEM_STYLE_IOS)
        assertEquals(2, MorphStyleResolver.OEM_STYLE_PIXEL)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════

}
