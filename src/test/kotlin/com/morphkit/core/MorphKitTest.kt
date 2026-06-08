package com.morphkit.core

import com.morphkit.core.MorphKit
import com.morphkit.core.MORPH_TAG_KEY
import com.morphkit.core.MorphConfig
import com.morphkit.core.MorphFactory2
import com.morphkit.core.MorphInstaller
import com.morphkit.core.StylePolicy
import android.app.Application
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * MorphKit 框架单元测试。
 *
 * 覆盖四大核心场景：
 * 1. 别名分组测试 — groupReplace 别名映射正确性
 * 2. 异常降级测试 — 硬替换失败时无缝降级到 originalFactory
 * 3. 打标与校验测试 — Tag 写入正确性与前缀规范警告
 * 4. 软修改测试 — 未命中 replace 但命中 modify 时属性修改
 *
 * ## 测试策略
 *
 * - **MorphKit 单例重置**：每个测试前通过反射重置 `initialized` 与 `config`，
 *   避免 `IllegalStateException: MorphKit 已初始化` 影响测试隔离。
 * - **Android API Mock**：`android.util.Log`、`View`、`Context`、`AttributeSet`
 *   等通过 MockK 进行 mock，不依赖 Android 设备。
 * - **MorphInstaller 隔离**：`MorphKit.init` 内部调用 `MorphInstaller.install`，
 *   需 mock `Application` 以避免 `ActivityLifecycleCallbacks` 注册异常。
 */
class MorphKitTest {

    /** Mock Application，用于 MorphKit.init */
    private lateinit var mockApp: Application

    /** Mock Context，传入 createView / onCreateView */
    private lateinit var mockContext: Context

    /** Mock AttributeSet，传入 createView / onCreateView */
    private lateinit var mockAttrs: AttributeSet

    companion object {
        /**
         * 全局一次性初始化：mock android.util.Log 静态方法，
         * 避免 Android 框架类在纯 JVM 环境下抛 UnsatisfiedLinkError。
         */
        @JvmStatic
        @BeforeAll
        fun setUpLog() {
            mockkStatic(Log::class)
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
        // 重置 MorphKit 单例状态，确保每个测试从干净状态开始
        resetMorphKit()
        // 重置 MorphInstaller 防重入标志
        resetMorphInstaller()

        // Mock Application：拦截 registerActivityLifecycleCallbacks（MorphInstaller.install 需要）
        mockApp = mockk<Application>(relaxed = true)
        every { mockApp.registerActivityLifecycleCallbacks(any()) } just runs

        mockContext = mockk(relaxed = true)
        mockAttrs = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        // 清理 mockkObject(MorphKit) 的 mock，避免影响其他测试
        try { unmockkObject(MorphKit) } catch (_: Exception) {}
        resetMorphKit()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 1：别名分组测试
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class GroupReplaceTest {

        @Test
        fun `groupReplace — 别名列表中的所有名称均能返回同一个自定义 View`() {
            // ── Arrange ──
            val morphButton = mockk<View>(relaxed = true)

            val sharedCreator: (Context, AttributeSet) -> View = { _, _ -> morphButton }

            MorphKit.init(mockApp) {
                groupReplace(
                    listOf("Button", "AppCompatButton", "androidx.appcompat.widget.AppCompatButton"),
                    sharedCreator
                )
            }

            // ── Act & Assert ──
            // 别名列表中的每个名称都应命中同一个 creator，返回同一个 View 实例
            val result1 = MorphKit.createView("Button", mockContext, mockAttrs)
            val result2 = MorphKit.createView("AppCompatButton", mockContext, mockAttrs)
            val result3 = MorphKit.createView("androidx.appcompat.widget.AppCompatButton", mockContext, mockAttrs)

            assertNotNull(result1)
            assertNotNull(result2)
            assertNotNull(result3)
            assertSame(result1, result2, "Button 与 AppCompatButton 应返回同一 View 实例")
            assertSame(result2, result3, "AppCompatButton 与全限定名应返回同一 View 实例")
        }

        @Test
        fun `groupReplace — 未注册的名称返回 null`() {
            val morphTextView = mockk<View>(relaxed = true)

            MorphKit.init(mockApp) {
                groupReplace(listOf("TextView", "AppCompatTextView")) { _, _ -> morphTextView }
            }

            val result = MorphKit.createView("EditText", mockContext, mockAttrs)
            assertNull(result, "未注册的名称应返回 null")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 2：异常降级测试（核心）
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class ExceptionFallbackTest {

        @Test
        fun `硬替换抛异常 — 异常被捕获，降级到 originalFactory 创建原生 View`() {
            // ── Arrange ──
            // 注册一个会抛 RuntimeException 的 replace 规则
            val crashingCreator: (Context, AttributeSet) -> View = { _, _ ->
                throw RuntimeException("模拟控件创建崩溃")
            }

            MorphKit.init(mockApp) {
                replace("Button", crashingCreator)
            }

            // Mock originalFactory，降级时应调用它
            val nativeButton = mockk<View>(relaxed = true)
            val originalFactory = mockk<LayoutInflater.Factory2>()
            every {
                originalFactory.onCreateView(any(), eq("Button"), any(), any())
            } returns nativeButton

            val factory2 = MorphFactory2(originalFactory)

            // ── Act ──
            val result = factory2.onCreateView(null, "Button", mockContext, mockAttrs)

            // ── Assert ──
            // 1. 异常未向上抛出，方法正常返回
            assertNotNull(result)
            // 2. 降级逻辑触发，originalFactory 被调用创建了原生 View
            verify(exactly = 1) {
                originalFactory.onCreateView(null, "Button", mockContext, mockAttrs)
            }
            // 3. 返回的是 originalFactory 创建的原生 View
            assertSame(nativeButton, result, "降级后应返回 originalFactory 创建的原生 View")
        }

        @Test
        fun `硬替换抛异常 — 降级后对原生 View 执行软修改兜底`() {
            // ── Arrange ──
            val crashingCreator: (Context, AttributeSet) -> View = { _, _ ->
                throw RuntimeException("模拟崩溃")
            }
            var modifyCalled = false

            MorphKit.init(mockApp) {
                replace("ImageView", crashingCreator)
                modify("ImageView") { modifyCalled = true }
            }

            val nativeImageView = mockk<View>(relaxed = true)
            val originalFactory = mockk<LayoutInflater.Factory2>()
            every {
                originalFactory.onCreateView(any(), eq("ImageView"), any(), any())
            } returns nativeImageView

            val factory2 = MorphFactory2(originalFactory)

            // ── Act ──
            val result = factory2.onCreateView(null, "ImageView", mockContext, mockAttrs)

            // ── Assert ──
            assertSame(nativeImageView, result)
            assertTrue(modifyCalled, "降级后应对原生 View 执行 modify 兜底")
        }

        @Test
        fun `硬替换异常 — originalFactory 为 null 时返回 null 交由系统处理`() {
            val crashingCreator: (Context, AttributeSet) -> View = { _, _ ->
                throw RuntimeException("模拟崩溃")
            }

            MorphKit.init(mockApp) {
                replace("TextView", crashingCreator)
            }

            // originalFactory 为 null（非 AppCompat 场景）
            val factory2 = MorphFactory2(null)

            val result = factory2.onCreateView(null, "TextView", mockContext, mockAttrs)
            assertNull(result, "originalFactory 为 null 时应返回 null 交由系统处理")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 3：打标与校验测试
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class StampAndValidateTest {

        @Test
        fun `符合规范的 MorphButton — Tag 被正确设置为 Morph replaced Button`() {
            // ── Arrange ──
            val morphButton = mockk<View>(relaxed = true)

            MorphKit.init(mockApp) {
                replace("Button") { _, _ -> morphButton }
            }

            // ── Act ──
            val result = MorphKit.createView("Button", mockContext, mockAttrs)

            // ── Assert ──
            assertNotNull(result)
            verify(exactly = 1) {
                morphButton.setTag(MORPH_TAG_KEY, "Morph (replaced: Button)")
            }
        }

        @Test
        fun `不符合规范的控件_输出规范警告Log`() {
            // ── Arrange ──
            val badButton = mockk<View>(relaxed = true)

            MorphKit.init(mockApp) {
                replace("Button") { _, _ -> badButton }
            }

            // ── Act ──
            val result = MorphKit.createView("Button", mockContext, mockAttrs)

            // ── Assert ──
            assertNotNull(result)
            // 打标仍然执行
            verify(exactly = 1) {
                badButton.setTag(MORPH_TAG_KEY, "Morph (replaced: Button)")
            }
            // 规范警告日志已输出（mock View 的类名不以 Morph 开头，必然触发警告）
            verify(atLeast = 1) {
                Log.w(
                    "MorphKit",
                    match<String> { it.contains("未遵循前缀规范") }
                )
            }
        }

        @Test
        fun `规范警告包含原始控件名`() {
            // ── Arrange ──
            val someView = mockk<View>(relaxed = true)

            MorphKit.init(mockApp) {
                replace("EditText") { _, _ -> someView }
            }

            // ── Act ──
            MorphKit.createView("EditText", mockContext, mockAttrs)

            // ── Assert ──
            // 警告日志应包含原始控件名（建议重命名信息）
            verify(atLeast = 1) {
                Log.w(
                    "MorphKit",
                    match<String> { it.contains("MorphEditText") }
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 4：软修改测试
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class ModifyTest {

        @Test
        fun `未命中 replace 但命中 modify — 原View类型未变但属性被修改`() {
            // ── Arrange ──
            var alphaModified = false

            MorphKit.init(mockApp) {
                // 只注册 modify，不注册 replace
                modify("ImageView") { view ->
                    view.alpha = 0.5f
                    alphaModified = true
                }
            }

            val originalImageView = mockk<View>(relaxed = true)

            // ── Act ──
            // createView 未命中，返回 null
            val createdView = MorphKit.createView("ImageView", mockContext, mockAttrs)
            assertNull(createdView, "未注册 replace 规则，createView 应返回 null")

            // modifyView 命中，修改原 View 属性
            val result = MorphKit.modifyView("ImageView", originalImageView)

            // ── Assert ──
            assertSame(originalImageView, result, "modifyView 应返回同一 View 实例")
            assertTrue(alphaModified, "modify 规则应被执行")
            verify(exactly = 1) { originalImageView.alpha = 0.5f }
        }

        @Test
        fun `命中 replace 且命中 modify — 替换控件被创建且属性被修改`() {
            // ── Arrange ──
            val morphTextView = mockk<View>(relaxed = true)

            var modifyExecuted = false

            MorphKit.init(mockApp) {
                replace("TextView") { _, _ -> morphTextView }
                modify("TextView") { view ->
                    view.alpha = 0.8f
                    modifyExecuted = true
                }
            }

            // ── Act ──
            val created = MorphKit.createView("TextView", mockContext, mockAttrs)
            assertNotNull(created)

            val modified = MorphKit.modifyView("TextView", created!!)
            assertSame(created, modified)
            assertTrue(modifyExecuted, "replace 命中后 modify 也应执行")
            verify { morphTextView.alpha = 0.8f }
        }

        @Test
        fun `未命中任何规则 — View 原样返回`() {
            MorphKit.init(mockApp) {
                // 空配置
            }

            val someView = mockk<View>(relaxed = true)

            val created = MorphKit.createView("Unknown", mockContext, mockAttrs)
            assertNull(created)

            val modified = MorphKit.modifyView("Unknown", someView)
            assertSame(someView, modified, "未命中任何规则应原样返回")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试：MorphFactory2 完整流程
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class MorphFactory2IntegrationTest {

        @Test
        fun `onCreateView 无 parent 重载 — 委托给带 parent 版本`() {
            val morphButton = mockk<View>(relaxed = true)

            MorphKit.init(mockApp) {
                replace("Button") { _, _ -> morphButton }
            }

            val originalFactory = mockk<LayoutInflater.Factory2>(relaxed = true)
            val factory2 = MorphFactory2(originalFactory)

            // 调用不带 parent 的重载
            val result = factory2.onCreateView("Button", mockContext, mockAttrs)

            assertNotNull(result)
            assertSame(morphButton, result)
        }

        @Test
        fun `硬替换成功且 modify 异常 — 仍返回替换控件（属性修改丢失但不白屏）`() {
            // ── Arrange ──
            val morphTextView = mockk<View>(relaxed = true)

            MorphKit.init(mockApp) {
                replace("TextView") { _, _ -> morphTextView }
                modify("TextView") { _ ->
                    throw RuntimeException("模拟 modify 异常")
                }
            }

            val originalFactory = mockk<LayoutInflater.Factory2>(relaxed = true)
            val factory2 = MorphFactory2(originalFactory)

            // ── Act ──
            val result = factory2.onCreateView(null, "TextView", mockContext, mockAttrs)

            // ── Assert ──
            // modify 抛异常但替换控件仍被返回，绝不白屏
            assertNotNull(result)
            assertSame(morphTextView, result, "modify 异常时仍应返回替换控件")
        }

        @Test
        fun `软修改兜底异常 — 静默捕获，返回原生控件`() {
            // ── Arrange ──
            MorphKit.init(mockApp) {
                modify("ImageView") { _ ->
                    throw RuntimeException("模拟 modify 兜底异常")
                }
            }

            val nativeImageView = mockk<View>(relaxed = true)
            val originalFactory = mockk<LayoutInflater.Factory2>()
            every {
                originalFactory.onCreateView(any(), eq("ImageView"), any(), any())
            } returns nativeImageView

            val factory2 = MorphFactory2(originalFactory)

            // ── Act ──
            val result = factory2.onCreateView(null, "ImageView", mockContext, mockAttrs)

            // ── Assert ──
            assertSame(nativeImageView, result, "软修改异常时应静默忽略，返回原生控件")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 5：init() 重复调用防御
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class InitReentryTest {

        @Test
        fun `init重复调用_抛出IllegalStateException`() {
            MorphKit.init(mockApp) {
                replace("TextView") { _, _ -> mockk(relaxed = true) }
            }

            val exception = assertThrows<IllegalStateException> {
                MorphKit.init(mockApp) {
                    replace("Button") { _, _ -> mockk(relaxed = true) }
                }
            }
            assertTrue(exception.message?.contains("已初始化") == true)
        }

        @Test
        fun `autoInit重复调用_抛出IllegalStateException`() {
            MorphKit.autoInit(mockApp)

            val exception = assertThrows<IllegalStateException> {
                MorphKit.autoInit(mockApp)
            }
            assertTrue(exception.message?.contains("已初始化") == true)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 6：autoInit() 默认映射完整性
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class AutoInitMappingTest {

        private fun getConfigReplaceMap(): Map<String, *> {
            val configField = MorphKit::class.java.getDeclaredField("config")
            configField.isAccessible = true
            val config = configField.get(MorphKit) as MorphConfig
            return config.replaceMap
        }

        @Test
        fun `autoInit注册TextView别名映射`() {
            MorphKit.autoInit(mockApp)

            val replaceMap = getConfigReplaceMap()
            assertTrue(replaceMap.containsKey("TextView"), "autoInit 应注册 TextView")
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatTextView"), "autoInit 应注册 AppCompatTextView")
        }

        @Test
        fun `autoInit注册Button别名映射`() {
            MorphKit.autoInit(mockApp)

            val replaceMap = getConfigReplaceMap()
            assertTrue(replaceMap.containsKey("Button"), "autoInit 应注册 Button")
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatButton"), "autoInit 应注册 AppCompatButton")
        }

        @Test
        fun `autoInit注册RadioButton别名映射`() {
            MorphKit.autoInit(mockApp)

            val replaceMap = getConfigReplaceMap()
            assertTrue(replaceMap.containsKey("RadioButton"), "autoInit 应注册 RadioButton")
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatRadioButton"), "autoInit 应注册 AppCompatRadioButton")
        }

        @Test
        fun `autoInit注册CheckBox别名映射`() {
            MorphKit.autoInit(mockApp)

            val replaceMap = getConfigReplaceMap()
            assertTrue(replaceMap.containsKey("CheckBox"), "autoInit 应注册 CheckBox")
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatCheckBox"), "autoInit 应注册 AppCompatCheckBox")
        }

        @Test
        fun `autoInit注册EditText和CardView映射`() {
            MorphKit.autoInit(mockApp)

            val replaceMap = getConfigReplaceMap()
            assertTrue(replaceMap.containsKey("androidx.appcompat.widget.AppCompatEditText"), "autoInit 应注册 AppCompatEditText")
            assertTrue(replaceMap.containsKey("com.google.android.material.card.MaterialCardView"), "autoInit 应注册 MaterialCardView")
        }

        @Test
        fun `autoInit未注册的控件不在replaceMap中`() {
            MorphKit.autoInit(mockApp)

            val replaceMap = getConfigReplaceMap()
            assertFalse(replaceMap.containsKey("ImageView"), "ImageView 不应在 replaceMap 中")
            assertFalse(replaceMap.containsKey("RecyclerView"), "RecyclerView 不应在 replaceMap 中")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 7：finalThemeResId 赋值正确性
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class FinalThemeResIdTest {

        @Test
        fun `init后finalThemeResId被赋值`() {
            MorphKit.init(mockApp) {
                stylePolicy(StylePolicy.FORCE_IOS)
            }

            assertNotEquals(0, MorphKit.finalThemeResId, "init 后 finalThemeResId 应被赋值")
        }

        @Test
        fun `FORCE_IOS策略_finalThemeResId指向iOS主题`() {
            MorphKit.init(mockApp) {
                stylePolicy(StylePolicy.FORCE_IOS)
            }

            assertEquals(
                com.morphkit.R.style.Theme_MorphKit_iOS,
                MorphKit.finalThemeResId
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 通过反射重置 MorphKit 单例的内部状态。
     *
     * 由于 [MorphKit] 是 Kotlin `object` 单例，其 `initialized` 和 `config`
     * 字段在测试间需要重置，否则第二次 `init` 会抛 `IllegalStateException`。
     * 此方法通过反射将 `initialized` AtomicBoolean 置为 false、`config` 置为未初始化状态。
     */
    private fun resetMorphKit() {
        try {
            val initializedField = MorphKit::class.java.getDeclaredField("initialized")
            initializedField.isAccessible = true
            val atomicBool = initializedField.get(MorphKit) as java.util.concurrent.atomic.AtomicBoolean
            atomicBool.set(false)

            val configField = MorphKit::class.java.getDeclaredField("config")
            configField.isAccessible = true
            // 将 lateinit config 重置为未初始化状态
            configField.set(MorphKit, null)
        } catch (e: Exception) {
            // 反射重置失败时忽略，测试可能因此失败并给出明确错误信息
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
