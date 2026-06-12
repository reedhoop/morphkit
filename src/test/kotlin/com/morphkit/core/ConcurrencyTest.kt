package com.morphkit.core

import android.app.Application
import android.util.Log
import android.view.View
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.Runs
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger

/**
 * MorphKit 并发安全测试。
 *
 * 验证核心组件在多线程并发场景下的行为正确性：
 * 1. MorphKit.init — 重复初始化的原子性保护
 * 2. MorphClickListener — 多线程并发点击的防抖正确性
 * 3. MorphConfig — ConcurrentHashMap 的并发读写安全性
 */
class ConcurrencyTest {

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

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 1：MorphKit.init 并发调用 — 仅一个线程成功初始化
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class InitConcurrencyTest {

        @Test
        fun `多线程并发调用init_仅一个线程成功_其余抛IllegalStateException`() {
            val threadCount = 10
            val barrier = CyclicBarrier(threadCount)
            val successCount = AtomicInteger(0)
            val failureCount = AtomicInteger(0)
            val latch = CountDownLatch(threadCount)

            val mockApp = mockk<Application>(relaxed = true)
            every { mockApp.registerActivityLifecycleCallbacks(any()) } just Runs

            val threads = (0 until threadCount).map {
                Thread {
                    barrier.await()
                    try {
                        MorphKit.init(mockApp) {
                            replace("TextView") { _, _ -> mockk<View>(relaxed = true) }
                        }
                        successCount.incrementAndGet()
                    } catch (e: IllegalStateException) {
                        if (e.message?.contains("已初始化") == true) {
                            failureCount.incrementAndGet()
                        }
                    } finally {
                        latch.countDown()
                    }
                }
            }

            threads.forEach { it.start() }
            latch.await()

            assertEquals(1, successCount.get(), "仅一个线程应成功初始化")
            assertEquals(threadCount - 1, failureCount.get(), "其余线程应收到 IllegalStateException")
        }

        @Test
        fun `init成功后_isInitialized返回true`() {
            val mockApp = mockk<Application>(relaxed = true)
            every { mockApp.registerActivityLifecycleCallbacks(any()) } just Runs

            assertFalse(MorphKit.isInitialized(), "初始化前 isInitialized 应为 false")

            MorphKit.init(mockApp) {}

            assertTrue(MorphKit.isInitialized(), "初始化后 isInitialized 应为 true")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 2：MorphClickListener 并发点击 — 防抖正确性
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class ClickListenerConcurrencyTest {

        @Test
        fun `多线程并发点击_防抖窗口内仅执行一次`() {
            val clickCount = AtomicInteger(0)
            var currentTime = 1000L

            val listener = MorphClickListener(300L, { currentTime }) {
                clickCount.incrementAndGet()
            }

            val mockView: View = mockk(relaxed = true)
            val threadCount = 10
            val barrier = CyclicBarrier(threadCount)
            val latch = CountDownLatch(threadCount)

            // 所有线程在同一时刻（冷却期内）点击
            val threads = (0 until threadCount).map {
                Thread {
                    barrier.await()
                    listener.onClick(mockView)
                    latch.countDown()
                }
            }

            threads.forEach { it.start() }
            latch.await()

            assertEquals(1, clickCount.get(), "冷却期内并发点击应仅执行一次")
        }

        @Test
        fun `冷却期边界_多线程竞争_最多执行两次`() {
            val clickCount = AtomicInteger(0)
            var currentTime = 1000L

            // 极短冷却时间
            val listener = MorphClickListener(1L, { currentTime }) {
                clickCount.incrementAndGet()
            }

            val mockView: View = mockk(relaxed = true)

            // 第一次点击
            listener.onClick(mockView)

            // 推进时间到冷却期刚过
            currentTime = 1002L

            // 多线程同时点击
            val threadCount = 5
            val barrier = CyclicBarrier(threadCount)
            val latch = CountDownLatch(threadCount)

            val threads = (0 until threadCount).map {
                Thread {
                    barrier.await()
                    listener.onClick(mockView)
                    latch.countDown()
                }
            }

            threads.forEach { it.start() }
            latch.await()

            // 第一次点击 + 冷却期后的竞争，最多 2 次
            assertTrue(clickCount.get() <= 2,
                "冷却期边界竞争最多执行 2 次，实际 ${clickCount.get()}")
            assertTrue(clickCount.get() >= 1,
                "至少应执行 1 次")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试 3：MorphConfig 并发注册 — ConcurrentHashMap 安全性
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    inner class ConfigConcurrencyTest {

        @Test
        fun `多线程并发注册replace规则_无数据丢失`() {
            val mockApp = mockk<Application>(relaxed = true)
            every { mockApp.registerActivityLifecycleCallbacks(any()) } just Runs

            val ruleCount = 50
            val latch = CountDownLatch(ruleCount)

            MorphKit.init(mockApp) {
                // 空配置
            }

            // 获取 config 引用
            val configField = MorphKit::class.java.getDeclaredField("config")
            configField.isAccessible = true
            val config = configField.get(MorphKit) as MorphConfig

            // 多线程并发注册
            val threads = (0 until ruleCount).map { i ->
                Thread {
                    config.replace("Widget$i") { _, _ -> mockk<View>(relaxed = true) }
                    latch.countDown()
                }
            }

            threads.forEach { it.start() }
            latch.await()

            // 验证所有规则都已注册
            val replaceMap = config.replaceMap
            assertEquals(ruleCount, replaceMap.size, "所有并发注册的规则应全部存在")
            for (i in 0 until ruleCount) {
                assertNotNull(replaceMap["Widget$i"], "Widget$i 应已注册")
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════

    private fun resetMorphKit() {
        try {
            // Reset initGuard (AtomicBoolean) — 防止重复初始化的守卫
            val initGuardField = MorphKit::class.java.getDeclaredField("initGuard")
            initGuardField.isAccessible = true
            (initGuardField.get(MorphKit) as java.util.concurrent.atomic.AtomicBoolean).set(false)

            // Reset initialized (@Volatile Boolean) — 初始化完成标志
            val initializedField = MorphKit::class.java.getDeclaredField("initialized")
            initializedField.isAccessible = true
            initializedField.setBoolean(MorphKit, false)

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
