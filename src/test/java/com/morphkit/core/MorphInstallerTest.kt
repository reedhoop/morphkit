package com.morphkit.core

import com.morphkit.core.MorphKit
import com.morphkit.core.MorphInstaller
import com.morphkit.core.MorphConfig
import com.morphkit.core.MorphFactory2
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
 * MorphInstaller 防重入与注入逻辑测试。
 *
 * 验证 install() 的 AtomicBoolean 防重入、
 * 以及与 MorphKit.init 的集成行为。
 */
class MorphInstallerTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.d(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        // 重置 MorphInstaller 的 installed 标志
        resetMorphInstaller()
        // 重置 MorphKit 单例
        resetMorphKit()
    }

    @After
    fun tearDown() {
        unmockkAll()
        resetMorphInstaller()
        resetMorphKit()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 F1：install() 首次调用注册 ActivityLifecycleCallbacks
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `install首次调用_注册ActivityLifecycleCallbacks`() {
        val mockApp = mockk<Application>(relaxed = true)
        every { mockApp.registerActivityLifecycleCallbacks(any()) } returns Unit

        MorphInstaller.install(mockApp)

        verify(exactly = 1) { mockApp.registerActivityLifecycleCallbacks(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 F2：install() 重复调用跳过注册
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `install重复调用_跳过注册`() {
        val mockApp = mockk<Application>(relaxed = true)
        every { mockApp.registerActivityLifecycleCallbacks(any()) } returns Unit

        MorphInstaller.install(mockApp)
        MorphInstaller.install(mockApp)

        // 只注册一次，第二次 install 应跳过
        verify(exactly = 1) { mockApp.registerActivityLifecycleCallbacks(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 F3：install 重复调用输出跳过日志
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `install重复调用_输出跳过日志`() {
        val mockApp = mockk<Application>(relaxed = true)
        every { mockApp.registerActivityLifecycleCallbacks(any()) } returns Unit

        MorphInstaller.install(mockApp)
        MorphInstaller.install(mockApp)

        verify(atLeast = 1) { Log.d("MorphKit", match { it.contains("已执行过") }) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 F4：MorphKit.init 内部调用 MorphInstaller.install
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `MorphKit_init内部调用MorphInstaller_install`() {
        val mockApp = mockk<Application>(relaxed = true)
        every { mockApp.registerActivityLifecycleCallbacks(any()) } returns Unit

        MorphKit.init(mockApp) {
            replace("TextView") { ctx, attrs ->
                mockk(relaxed = true)
            }
        }

        verify(exactly = 1) { mockApp.registerActivityLifecycleCallbacks(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 F5：MorphKit.autoInit 内部调用 MorphInstaller.install
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `MorphKit_autoInit内部调用MorphInstaller_install`() {
        val mockApp = mockk<Application>(relaxed = true)
        every { mockApp.registerActivityLifecycleCallbacks(any()) } returns Unit

        MorphKit.autoInit(mockApp)

        verify(exactly = 1) { mockApp.registerActivityLifecycleCallbacks(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════

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

    private fun resetMorphKit() {
        try {
            val initializedField = MorphKit::class.java.getDeclaredField("initialized")
            initializedField.isAccessible = true
            initializedField.set(MorphKit, false)

            val configField = MorphKit::class.java.getDeclaredField("config")
            configField.isAccessible = true
            configField.set(MorphKit, null)
        } catch (e: Exception) {
            // 反射重置失败时忽略
        }
    }
}
