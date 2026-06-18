package com.morphkit.theme

import com.morphkit.core.StylePolicy
import com.morphkit.theme.MorphStyleResolver
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

        // 重置 OEM 缓存，确保每个测试用例独立
        resetOemCache()
    }

    /**
     * 通过反射重置 cachedOemStyle 为 null（未缓存），
     * 确保每个测试用例的 OEM 设置读取独立。
     */
    private fun resetOemCache() {
        val field = MorphStyleResolver::class.java.getDeclaredField("cachedOemStyle")
        field.isAccessible = true
        field.set(MorphStyleResolver, null)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B1：IOS 策略直接返回 iOS Theme
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS策略_返回iOS主题`() {
        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.IOS)
        assertEquals(
            "IOS 应返回 iOS 主题",
            com.morphkit.R.style.Theme_MorphKit_iOS,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B2：PIXEL 策略直接返回 Pixel Theme
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `PIXEL策略_返回Pixel主题`() {
        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.PIXEL)
        assertEquals(
            "PIXEL 应返回 Pixel 主题",
            com.morphkit.R.style.Theme_MorphKit_Pixel,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B3：AUTO 策略在 DynamicColors 反射不可用时回退到 Pixel
    //
    // 在纯 JVM 测试环境中：
    // - DynamicColors 类不存在，反射调用失败
    // - minSdk=35 (API 31+) 保证 Dynamic Color 硬件能力必然可用，
    //   无需 SDK_INT 守卫，直接返回 true（C1 修复）
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `AUTO策略_DynamicColors反射不可用_minSdk35保证返回Pixel主题`() {
        // 纯 JVM 环境下 DynamicColors 反射调用必然失败，
        // 但 minSdk=35 保证 API 31+ 始终可用，Dynamic Color 硬件能力必然存在，
        // 因此 AUTO 策略应返回 Pixel 主题
        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.AUTO)
        assertEquals(
            "AUTO 策略在 DynamicColors 反射不可用时，minSdk=35 保证应返回 Pixel 主题",
            com.morphkit.R.style.Theme_MorphKit_Pixel,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B4：OEM 系统设置优先级高于 StylePolicy
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `OEM系统设置优先级高于StylePolicy`() {
        // OEM 设置为 iOS (1)，但 StylePolicy 为 PIXEL
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } returns 1

        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.PIXEL)
        assertEquals(
            "OEM 设置为 iOS 时应返回 iOS 主题，即使 StylePolicy 为 PIXEL",
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

        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.IOS)
        assertEquals(
            "OEM 设置为 Pixel 时应返回 Pixel 主题，即使 StylePolicy 为 IOS",
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

        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.IOS)
        assertEquals(
            "OEM 设置读取异常时应安全回退到 StylePolicy",
            com.morphkit.R.style.Theme_MorphKit_iOS,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B6b：OEM 系统设置读取抛 IllegalArgumentException 时安全回退（M20 修复）
    // Red Line 4 要求宽泛异常捕获，部分定制 ROM 可能抛非 SecurityException
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `OEM系统设置读取IllegalArgumentException时_安全回退到StylePolicy`() {
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } throws IllegalArgumentException("参数异常")

        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.PIXEL)
        assertEquals(
            "OEM 设置读取 IllegalArgumentException 时应安全回退到 StylePolicy",
            com.morphkit.R.style.Theme_MorphKit_Pixel,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B6c：OEM 系统设置读取抛 NullPointerException 时安全回退（M20 修复）
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `OEM系统设置读取NullPointerException时_安全回退到StylePolicy`() {
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } throws NullPointerException("contentResolver 为 null")

        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.IOS)
        assertEquals(
            "OEM 设置读取 NullPointerException 时应安全回退到 StylePolicy",
            com.morphkit.R.style.Theme_MorphKit_iOS,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B7：OEM 未知值回退到 StylePolicy 判定
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `OEM未知值时_回退到StylePolicy判定`() {
        // OEM 设置为 99（未知值），应回退到 StylePolicy
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } returns 99

        val themeResId = MorphStyleResolver.resolve(mockContext, StylePolicy.PIXEL)
        assertEquals(
            "OEM 未知值时应回退到 StylePolicy",
            com.morphkit.R.style.Theme_MorphKit_Pixel,
            themeResId
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B8：OEM_UI_STYLE_KEY 常量值正确
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `OEM_UI_STYLE_KEY常量值正确`() {
        assertEquals("oem_ui_style", MorphStyleResolver.OEM_UI_STYLE_KEY)
        assertEquals(0, MorphStyleResolver.OEM_STYLE_DEFAULT)
        assertEquals(1, MorphStyleResolver.OEM_STYLE_IOS)
        assertEquals(2, MorphStyleResolver.OEM_STYLE_PIXEL)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 B9：OEM 设置缓存 — 避免每次 resolve 触发 Settings.System IPC
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `OEM设置首次读取后缓存_后续调用不触发IPC`() {
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } returns 1

        // 首次调用：应读取 Settings.System 并缓存
        val firstResult = MorphStyleResolver.resolve(mockContext, StylePolicy.PIXEL)
        assertEquals(com.morphkit.R.style.Theme_MorphKit_iOS, firstResult)

        // 修改 mock 返回值为 2（Pixel），但缓存应仍为 1（iOS）
        every { Settings.System.getInt(any(), "oem_ui_style", any()) } returns 2

        // 第二次调用：应使用缓存值（1=iOS），不受新 mock 值影响
        val secondResult = MorphStyleResolver.resolve(mockContext, StylePolicy.PIXEL)
        assertEquals(
            "OEM 设置应被缓存，第二次调用仍返回首次读取的值",
            com.morphkit.R.style.Theme_MorphKit_iOS,
            secondResult
        )
    }
}
