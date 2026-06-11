package com.morphkit.theme

import android.app.Application
import android.util.Log
import android.view.View
import com.morphkit.core.MorphConfig
import com.morphkit.core.MorphInstaller
import com.morphkit.core.MorphKit
import com.morphkit.core.StylePolicy
import io.mockk.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

/**
 * MorphKitIOSConfig 单元测试。
 *
 * 验证 [Application.initIOSStyle] 扩展函数的完整行为：
 * 1. 调用 [MorphKit.init] 并传入正确的配置
 * 2. stylePolicy 被设置为 [StylePolicy.IOS]（关键修复验证）
 * 3. 默认控件替换规则已注册（replaceMap 包含预期的 key）
 * 4. RecyclerView 的 modify 规则已注册
 *
 * ## 测试策略
 *
 * - 复用 MorphKitTest 的单例重置模式（反射重置 initialized / config / installed）
 * - Mock Application 避免真实 Android 组件依赖
 * - Mock Log 静态方法避免 JVM 环境下的 UnsatisfiedLinkError
 */
class MorphKitIOSConfigTest {

    private lateinit var mockApp: Application

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUpLog() {
            mockkStatic(Log::class)
            every { Log.i(any(), any<String>()) } returns 0
            every { Log.w(any(), any<String>()) } returns 0
            every { Log.w(any(), any<String>(), any()) } returns 0
            every { Log.e(any(), any<String>()) } returns 0
            every { Log.e(any(), any<String>(), any()) } returns 0
            every { Log.d(any(), any<String>()) } returns 0
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

        mockApp = mockk<Application>(relaxed = true)
        every { mockApp.registerActivityLifecycleCallbacks(any()) } just runs
    }

    @AfterEach
    fun tearDown() {
        try { unmockkObject(MorphKit) } catch (_: Exception) {}
        resetMorphKit()
    }

    // ===================================================================
    // 1. initIOSStyle() calls MorphKit.init() with correct configuration
    // ===================================================================

    @Nested
    inner class InitIntegrationTest {

        @Test
        fun `initIOSStyle completes without exception and initializes MorphKit`() {
            mockApp.initIOSStyle()

            assertTrue(MorphKit.isInitialized(), "MorphKit should be initialized after initIOSStyle()")
        }

        @Test
        fun `initIOSStyle logs iOS Design System Applied`() {
            mockApp.initIOSStyle()

            // 验证 Log.i 被调用（包含 "iOS Design System Applied" 消息）
            // 使用 verify(atLeast = 1) 因为 MorphKit.init 内部也可能调用 Log.i
            verify(atLeast = 1) {
                Log.i("MorphKit", "iOS Design System Applied.")
            }
        }

        @Test
        fun `initIOSStyle cannot be called twice — throws IllegalStateException`() {
            mockApp.initIOSStyle()

            val exception = assertThrows<IllegalStateException> {
                mockApp.initIOSStyle()
            }
            assertTrue(exception.message?.contains("已初始化") == true)
        }
    }

    // ===================================================================
    // 2. stylePolicy is set to StylePolicy.IOS (the critical fix!)
    // ===================================================================

    @Nested
    inner class StylePolicyTest {

        @Test
        fun `initIOSStyle sets stylePolicy to IOS`() {
            mockApp.initIOSStyle()

            val config = getConfig()
            assertEquals(StylePolicy.IOS, config.policy,
                "stylePolicy must be IOS after initIOSStyle()")
        }

        @Test
        fun `initIOSStyle policy is NOT the default AUTO`() {
            mockApp.initIOSStyle()

            val config = getConfig()
            assertNotEquals(StylePolicy.AUTO, config.policy,
                "stylePolicy must NOT remain at default AUTO")
        }

        @Test
        fun `initIOSStyle policy is NOT PIXEL`() {
            mockApp.initIOSStyle()

            val config = getConfig()
            assertNotEquals(StylePolicy.PIXEL, config.policy,
                "stylePolicy must NOT be PIXEL")
        }
    }

    // ===================================================================
    // 3. Default widgets are registered (replaceMap contains expected keys)
    // ===================================================================

    @Nested
    inner class DefaultWidgetsTest {

        @Test
        fun `replaceMap contains TextView and AppCompatTextView`() {
            mockApp.initIOSStyle()

            val replaceMap = getReplaceMap()
            assertTrue(replaceMap.containsKey("TextView"),
                "TextView should be registered")
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatTextView"),
                "AppCompatTextView should be registered")
        }

        @Test
        fun `replaceMap contains Button and AppCompatButton`() {
            mockApp.initIOSStyle()

            val replaceMap = getReplaceMap()
            assertTrue(replaceMap.containsKey("Button"),
                "Button should be registered")
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatButton"),
                "AppCompatButton should be registered")
        }

        @Test
        fun `replaceMap contains EditText and AppCompatEditText`() {
            mockApp.initIOSStyle()

            val replaceMap = getReplaceMap()
            assertTrue(replaceMap.containsKey("EditText"),
                "EditText should be registered")
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatEditText"),
                "AppCompatEditText should be registered")
        }

        @Test
        fun `replaceMap contains CardView`() {
            mockApp.initIOSStyle()

            val replaceMap = getReplaceMap()
            assertTrue(replaceMap.containsKey("androidx.cardview.widget.CardView"),
                "CardView should be registered")
        }

        @Test
        fun `replaceMap contains MaterialCardView`() {
            mockApp.initIOSStyle()

            val replaceMap = getReplaceMap()
            assertTrue(replaceMap.containsKey("com.google.android.material.card.MaterialCardView"),
                "MaterialCardView should be registered")
        }

        @Test
        fun `replaceMap contains RadioButton and AppCompatRadioButton`() {
            mockApp.initIOSStyle()

            val replaceMap = getReplaceMap()
            assertTrue(replaceMap.containsKey("RadioButton"),
                "RadioButton should be registered")
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatRadioButton"),
                "AppCompatRadioButton should be registered")
        }

        @Test
        fun `replaceMap contains CheckBox and AppCompatCheckBox`() {
            mockApp.initIOSStyle()

            val replaceMap = getReplaceMap()
            assertTrue(replaceMap.containsKey("CheckBox"),
                "CheckBox should be registered")
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatCheckBox"),
                "AppCompatCheckBox should be registered")
        }

        @Test
        fun `replaceMap contains all 12 expected entries`() {
            mockApp.initIOSStyle()

            val replaceMap = getReplaceMap()
            // 2 (TextView) + 2 (Button) + 2 (EditText) + 1 (CardView)
            // + 1 (MaterialCardView) + 2 (RadioButton) + 2 (CheckBox) = 12
            assertEquals(12, replaceMap.size,
                "replaceMap should contain exactly 12 entries from registerDefaultWidgets()")
        }
    }

    // ===================================================================
    // 4. RecyclerView modify rule is registered
    // ===================================================================

    @Nested
    inner class RecyclerViewModifyTest {

        @Test
        fun `modifyMap contains RecyclerView rule`() {
            mockApp.initIOSStyle()

            val modifyMap = getModifyMap()
            assertTrue(modifyMap.containsKey("androidx.recyclerview.widget.RecyclerView"),
                "RecyclerView modify rule should be registered")
        }

        @Test
        fun `RecyclerView modify rule sets overScrollMode to OVER_SCROLL_NEVER`() {
            mockApp.initIOSStyle()

            val modifyMap = getModifyMap()
            val modifier = modifyMap["androidx.recyclerview.widget.RecyclerView"]
            assertNotNull(modifier, "RecyclerView modifier should not be null")

            val mockRecyclerView = mockk<View>(relaxed = true)
            modifier!!(mockRecyclerView)

            verify(exactly = 1) {
                mockRecyclerView.overScrollMode = View.OVER_SCROLL_NEVER
            }
        }

        @Test
        fun `modifyMap contains exactly 1 entry (only RecyclerView)`() {
            mockApp.initIOSStyle()

            val modifyMap = getModifyMap()
            assertEquals(1, modifyMap.size,
                "modifyMap should contain exactly 1 entry (RecyclerView)")
        }
    }

    // ===================================================================
    // Helper methods — reflection-based access to MorphKit internals
    // ===================================================================

    /**
     * Retrieve the internal [MorphConfig] from the [MorphKit] singleton via reflection.
     */
    private fun getConfig(): MorphConfig {
        val configField = MorphKit::class.java.getDeclaredField("config")
        configField.isAccessible = true
        return configField.get(MorphKit) as MorphConfig
    }

    private fun getReplaceMap(): Map<String, *> {
        return getConfig().replaceMap
    }

    private fun getModifyMap(): Map<String, (View) -> Unit> {
        return getConfig().modifyMap
    }

    /**
     * Reset MorphKit singleton state via reflection.
     *
     * Sets [initialized] AtomicBoolean to false and clears the lateinit [config] field,
     * ensuring each test starts from a clean state.
     */
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
            // Reflection reset failure — test will fail with a clear error
        }
    }

    /**
     * Reset MorphInstaller singleton state via reflection.
     *
     * [MorphKit.init] internally calls [MorphInstaller.install], which uses an
     * AtomicBoolean guard to prevent double-registration. This must be reset
     * between tests.
     */
    private fun resetMorphInstaller() {
        try {
            val field = MorphInstaller::class.java.getDeclaredField("installed")
            field.isAccessible = true
            val atomicBool = field.get(MorphInstaller) as java.util.concurrent.atomic.AtomicBoolean
            atomicBool.set(false)
        } catch (e: Exception) {
            // Reflection reset failure — test will fail with a clear error
        }
    }
}
