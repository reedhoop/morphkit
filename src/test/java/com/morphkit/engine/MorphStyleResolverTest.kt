package com.morphkit.engine

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * MorphStyleResolver 与 Context 注入测试。
 *
 * 由于 CI 环境网络受限，Robolectric 无法下载 android-all-instrumented JAR，
 * 因此采用纯 MockK 方案模拟 Android 环境，验证主题解析逻辑。
 *
 * ## CI/CD 合规要求
 *
 * MorphKit 的合并请求（PR）必须通过所有 `src/test` 下的测试用例，
 * 特别是主题解析测试，否则严禁合入主分支。
 */
class MorphStyleResolverTest {

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: ContentResolver

    @Before
    fun setUp() {
        // Mock android.util.Log
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.d(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        // Mock Settings.System
        mockkStatic(Settings.System::class)

        // Mock Context
        mockContext = mockk(relaxed = true)
        mockContentResolver = mockk(relaxed = true)
        every { mockContext.contentResolver } returns mockContentResolver

        // 默认 OEM 设置为 0（未设置）
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B1：FORCE_IOS 策略直接返回 iOS Theme
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `FORCE_IOS策略_返回iOS主题`() {
        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.FORCE_IOS)
        assertEquals(
            "FORCE_IOS 应返回 iOS 主题",
            com.morphkit.R.style.Theme_MorphKit_iOS,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B2：FORCE_PIXEL 策略直接返回 Pixel Theme
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `FORCE_PIXEL策略_返回Pixel主题`() {
        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.FORCE_PIXEL)
        assertEquals(
            "FORCE_PIXEL 应返回 Pixel 主题",
            com.morphkit.R.style.Theme_MorphKit_Pixel,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B3：AUTO 策略在不支持 Dynamic Color 时回退到 iOS
    //
    // 在纯 JVM 测试环境中：
    // - DynamicColors 类不存在，反射调用失败，降级到 API 级别检测
    // - Build.VERSION.SDK_INT 默认为 0（returnDefaultValues=true），
    //   0 < Build.VERSION_CODES.S(31)，因此判定为不支持 Dynamic Color
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `AUTO策略_不支持DynamicColor_回退到iOS主题`() {
        // 纯 JVM 环境下 DynamicColors 反射调用必然失败，
        // 且 Build.VERSION.SDK_INT 为 0（默认值），低于 S(31)，
        // 因此 AUTO 策略应回退到 iOS 主题
        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.AUTO)
        assertEquals(
            "AUTO 策略在不支持 Dynamic Color 时应回退到 iOS 主题",
            com.morphkit.R.style.Theme_MorphKit_iOS,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B4：OEM 系统设置优先级高于 StylePolicy
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `OEM系统设置优先级高于StylePolicy`() {
        // OEM 设置为 iOS (1)，但 StylePolicy 为 FORCE_PIXEL
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } returns 1

        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.FORCE_PIXEL)
        assertEquals(
            "OEM 设置为 iOS 时应返回 iOS 主题，即使 StylePolicy 为 FORCE_PIXEL",
            com.morphkit.R.style.Theme_MorphKit_iOS,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B5：OEM 系统设置为 Pixel 时返回 Pixel 主题
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `OEM系统设置为Pixel时_返回Pixel主题`() {
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } returns 2

        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.FORCE_IOS)
        assertEquals(
            "OEM 设置为 Pixel 时应返回 Pixel 主题，即使 StylePolicy 为 FORCE_IOS",
            com.morphkit.R.style.Theme_MorphKit_Pixel,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B6：OEM 系统设置读取异常时安全回退
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `OEM系统设置读取异常时_安全回退到StylePolicy`() {
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } throws SecurityException("权限不足")

        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.FORCE_IOS)
        assertEquals(
            "OEM 设置读取异常时应安全回退到 StylePolicy",
            com.morphkit.R.style.Theme_MorphKit_iOS,
            themeResId
        )
    }
}
