package com.morphkit.internal

import android.util.Log
import android.view.LayoutInflater
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

/**
 * ReflectionHelper 反射工具类单元测试。
 *
 * 覆盖四大方法：
 * - [ReflectionHelper.isReflectionRestricted]：Android 14+ 反射限制判断
 * - [ReflectionHelper.resolveField]：安全字段获取
 * - [ReflectionHelper.setFieldValue]：安全字段写入
 * - [ReflectionHelper.safeSetFactory2]：Factory2 注入（双策略降级）
 *
 * 注意：纯 JVM 环境下 Build.VERSION.SDK_INT 为 0，
 * isReflectionRestricted() 始终返回 false。
 * 线上 minSdk=35 时该方法始终返回 true，此行为通过 mockkObject 间接覆盖。
 */
class ReflectionHelperTest {

    /** 用于反射测试的简单类 */
    class TestTarget {
        @Suppress("unused")
        var name: String = ""
        @Suppress("unused")
        var count: Int = 0
    }

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
    // 一、isReflectionRestricted() 测试
    //
    // 纯 JVM 环境下 Build.VERSION.SDK_INT 为 0，始终返回 false。
    // 线上 minSdk=35 时 SDK_INT >= 34 恒成立，始终返回 true。
    // SDK_INT 为 final 静态字段，纯 JVM 下无法 Mock，此处仅验证 JVM 行为。
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `纯JVM环境下_SDK_INT为0_返回true`() {
        // minSdk=35，方法始终返回 true（不再依赖 Build.VERSION.SDK_INT）
        assertTrue(ReflectionHelper.isReflectionRestricted())
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 二、resolveField() 测试
    //
    // 成功获取字段、获取失败返回 null、不同 SDK 版本的日志输出。
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `resolveField_存在的字段_返回可访问的Field`() {
        val field = ReflectionHelper.resolveField(TestTarget::class.java, "name")

        assertNotNull("应成功获取已存在的字段", field)
        assertEquals("name", field!!.name)
        assertTrue("字段应被设置为可访问", field.isAccessible)
    }

    @Test
    fun `resolveField_不存在的字段_返回null`() {
        val field = ReflectionHelper.resolveField(TestTarget::class.java, "nonExistentField")

        assertNull("不存在的字段应返回 null", field)
    }

    @Test
    fun `resolveField_反射受限时_输出警告日志`() {
        // 通过 mockkObject 使 isReflectionRestricted() 返回 true → Log.w
        mockkObject(ReflectionHelper)
        every { ReflectionHelper.isReflectionRestricted() } returns true
        every { ReflectionHelper.resolveField(any(), any()) } answers { callOriginal() }

        ReflectionHelper.resolveField(TestTarget::class.java, "nonExistentField")

        verify { Log.w("MorphKit", match<String> { it.contains("Android 14+") && it.contains("nonExistentField") }) }
    }

    @Test
    fun `resolveField_反射未受限时_输出调试日志`() {
        // 通过 mockkObject 使 isReflectionRestricted() 返回 false → Log.d
        mockkObject(ReflectionHelper)
        every { ReflectionHelper.isReflectionRestricted() } returns false
        every { ReflectionHelper.resolveField(any(), any()) } answers { callOriginal() }

        ReflectionHelper.resolveField(TestTarget::class.java, "nonExistentField")

        verify { Log.d("MorphKit", match<String> { it.contains("nonExistentField") }) }
    }

    @Test
    fun `resolveField_Int类型字段_也能正常获取`() {
        val field = ReflectionHelper.resolveField(TestTarget::class.java, "count")

        assertNotNull("Int 类型字段应成功获取", field)
        assertEquals(Int::class.javaPrimitiveType, field!!.type)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 三、setFieldValue() 测试
    //
    // 成功写入字段、写入失败时异常被重新抛出。
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `setFieldValue_正常写入_值被正确设置`() {
        val target = TestTarget()
        val field = TestTarget::class.java.getDeclaredField("name")
        field.isAccessible = true

        ReflectionHelper.setFieldValue(field, target, "Hello")

        assertEquals("Hello", target.name)
    }

    @Test
    fun `setFieldValue_Int字段_正常写入`() {
        val target = TestTarget()
        val field = TestTarget::class.java.getDeclaredField("count")
        field.isAccessible = true

        ReflectionHelper.setFieldValue(field, target, 42)

        assertEquals(42, target.count)
    }

    @Test
    fun `setFieldValue_写入失败_异常被重新抛出`() {
        // 使用 mockk<Field> 模拟 Field.set 抛异常，验证异常被重新抛出
        val mockField = mockk<Field>(relaxed = true)
        every { mockField.name } returns "testField"
        every { mockField.set(any(), any()) } throws IllegalAccessException("mocked access error")

        val target = TestTarget()

        try {
            ReflectionHelper.setFieldValue(mockField, target, "value")
            fail("应抛出异常")
        } catch (e: IllegalAccessException) {
            assertEquals("mocked access error", e.message)
        }

        // 验证失败时输出了调试日志
        verify { Log.d("MorphKit", match<String> { it.contains("反射写入字段") && it.contains("testField") }) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 四、safeSetFactory2() 测试
    //
    // 策略 1：反射直接写入 mFactory2（最可靠路径）
    // 策略 2：重置 mFactorySet + 公开 API setFactory2（降级路径）
    // ═══════════════════════════════════════════════════════════════════════

    // ── 策略 1 成功 ──

    @Test
    fun `safeSetFactory2_策略1成功_mFactory2字段找到并写入成功_返回true`() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val factory = mockk<LayoutInflater.Factory2>(relaxed = true)
        val mockFactory2Field = mockk<Field>(relaxed = true)
        val mockFactoryField = mockk<Field>(relaxed = true)

        mockkObject(ReflectionHelper)
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory2")
        } returns mockFactory2Field
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory")
        } returns mockFactoryField
        every {
            ReflectionHelper.setFieldValue(any(), any(), any())
        } returns Unit

        val result = ReflectionHelper.safeSetFactory2(inflater, factory)

        assertTrue("策略 1 成功时应返回 true", result)
        verify { ReflectionHelper.setFieldValue(mockFactory2Field, inflater, factory) }
        verify { ReflectionHelper.setFieldValue(mockFactoryField, inflater, factory) }
        verify { Log.d("MorphKit", match<String> { it.contains("反射直接写入 mFactory2 成功") }) }
    }

    @Test
    fun `safeSetFactory2_策略1_mFactory2写入成功但mFactory写入失败_仍返回true`() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val factory = mockk<LayoutInflater.Factory2>(relaxed = true)
        val mockFactory2Field = mockk<Field>(relaxed = true)
        val mockFactoryField = mockk<Field>(relaxed = true)

        mockkObject(ReflectionHelper)
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory2")
        } returns mockFactory2Field
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory")
        } returns mockFactoryField

        // mFactory2 写入成功，mFactory 写入失败
        every {
            ReflectionHelper.setFieldValue(mockFactory2Field, inflater, factory)
        } returns Unit
        every {
            ReflectionHelper.setFieldValue(mockFactoryField, inflater, factory)
        } throws RuntimeException("mFactory write failed")

        val result = ReflectionHelper.safeSetFactory2(inflater, factory)

        assertTrue("mFactory2 写入成功时即使 mFactory 失败也应返回 true", result)
        verify {
            Log.d(
                "MorphKit",
                match<String> { it.contains("mFactory") && it.contains("写入失败") && it.contains("不影响核心功能") }
            )
        }
    }

    @Test
    fun `safeSetFactory2_策略1_mFactory2找到但mFactory字段不存在_仍返回true`() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val factory = mockk<LayoutInflater.Factory2>(relaxed = true)
        val mockFactory2Field = mockk<Field>(relaxed = true)

        mockkObject(ReflectionHelper)
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory2")
        } returns mockFactory2Field
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory")
        } returns null
        every {
            ReflectionHelper.setFieldValue(any(), any(), any())
        } returns Unit

        val result = ReflectionHelper.safeSetFactory2(inflater, factory)

        assertTrue("mFactory2 写入成功时应返回 true（mFactory 为 null 跳过）", result)
        // setFieldValue 仅被调用一次（mFactory2），mFactory 为 null 不进入写入分支
        verify(exactly = 1) { ReflectionHelper.setFieldValue(any(), any(), any()) }
    }

    // ── 策略 1 失败 → 策略 2 降级 ──

    @Test
    fun `safeSetFactory2_策略1失败_策略2通过公开API设置成功_返回true`() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val factory = mockk<LayoutInflater.Factory2>(relaxed = true)

        mockkObject(ReflectionHelper)
        // 策略 1：mFactory2 不可用
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory2")
        } returns null
        // 策略 2：mFactorySet 也不可用（Android 14+ 典型场景）
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactorySet")
        } returns null
        // factory 字段（策略 1 中的 mFactory）
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory")
        } returns null

        // inflater.factory2 = factory 成功（relaxed mock 不抛异常）
        every { inflater.factory2 = factory } returns Unit

        val result = ReflectionHelper.safeSetFactory2(inflater, factory)

        assertTrue("策略 2 通过公开 API 设置成功时应返回 true", result)
        verify { inflater.factory2 = factory }
        verify { Log.d("MorphKit", match<String> { it.contains("通过公开 API 设置 Factory2 成功") }) }
    }

    @Test
    fun `safeSetFactory2_策略1失败_策略2重置mFactorySet后公开API成功_返回true`() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val factory = mockk<LayoutInflater.Factory2>(relaxed = true)
        val mockFactorySetField = mockk<Field>(relaxed = true)

        mockkObject(ReflectionHelper)
        // 策略 1：mFactory2 不可用
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory2")
        } returns null
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory")
        } returns null
        // 策略 2：mFactorySet 可用
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactorySet")
        } returns mockFactorySetField

        // setBoolean 成功（重置标志位）
        every { mockFactorySetField.setBoolean(inflater, false) } returns Unit
        // 公开 API 设置成功
        every { inflater.factory2 = factory } returns Unit

        val result = ReflectionHelper.safeSetFactory2(inflater, factory)

        assertTrue("重置 mFactorySet 后公开 API 成功时应返回 true", result)
        verify { mockFactorySetField.setBoolean(inflater, false) }
        verify { inflater.factory2 = factory }
    }

    // ── 完全失败 ──

    @Test
    fun `safeSetFactory2_完全失败_IllegalStateException_返回false`() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val factory = mockk<LayoutInflater.Factory2>(relaxed = true)

        mockkObject(ReflectionHelper)
        // 策略 1：mFactory2 不可用
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory2")
        } returns null
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory")
        } returns null
        // 策略 2：mFactorySet 也不可用
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactorySet")
        } returns null

        // 公开 API 抛 IllegalStateException（AppCompat 已设置 Factory2）
        every { inflater.factory2 = factory } throws IllegalStateException("Factory already set")

        val result = ReflectionHelper.safeSetFactory2(inflater, factory)

        assertFalse("完全失败时应返回 false", result)
        verify {
            Log.w(
                "MorphKit",
                match<String> { it.contains("safeSetFactory2 失败") && it.contains("MorphKit 注入无法生效") },
                any()
            )
        }
    }

    @Test
    fun `safeSetFactory2_完全失败_其他异常_返回false`() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val factory = mockk<LayoutInflater.Factory2>(relaxed = true)

        mockkObject(ReflectionHelper)
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory2")
        } returns null
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory")
        } returns null
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactorySet")
        } returns null

        // 公开 API 抛非 IllegalStateException 的异常
        every { inflater.factory2 = factory } throws RuntimeException("unexpected error")

        val result = ReflectionHelper.safeSetFactory2(inflater, factory)

        assertFalse("非 IllegalStateException 异常也应返回 false", result)
        verify { Log.w("MorphKit", match<String> { it.contains("safeSetFactory2 异常") }, any()) }
    }

    @Test
    fun `safeSetFactory2_mFactorySet重置失败_但公开API成功_返回true`() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val factory = mockk<LayoutInflater.Factory2>(relaxed = true)
        val mockFactorySetField = mockk<Field>(relaxed = true)

        mockkObject(ReflectionHelper)
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory2")
        } returns null
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory")
        } returns null
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactorySet")
        } returns mockFactorySetField

        // mFactorySet 重置失败
        every { mockFactorySetField.setBoolean(inflater, false) } throws IllegalAccessException("blocked")
        // 但公开 API 仍然成功（AppCompat 尚未安装）
        every { inflater.factory2 = factory } returns Unit

        val result = ReflectionHelper.safeSetFactory2(inflater, factory)

        assertTrue("mFactorySet 重置失败但公开 API 成功时应返回 true", result)
        verify { Log.d("MorphKit", match<String> { it.contains("重置 mFactorySet 失败") }, any()) }
    }

    // ── 策略 1 写入异常降级到策略 2 ──

    @Test
    fun `safeSetFactory2_策略1写入mFactory2抛异常_降级到策略2_成功返回true`() {
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val factory = mockk<LayoutInflater.Factory2>(relaxed = true)
        val mockFactory2Field = mockk<Field>(relaxed = true)

        mockkObject(ReflectionHelper)
        // 策略 1：mFactory2 字段找到
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory2")
        } returns mockFactory2Field
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactory")
        } returns null
        // 但写入抛异常
        every {
            ReflectionHelper.setFieldValue(mockFactory2Field, inflater, factory)
        } throws IllegalAccessException("write blocked")

        // 策略 2 降级
        every {
            ReflectionHelper.resolveField(LayoutInflater::class.java, "mFactorySet")
        } returns null
        every { inflater.factory2 = factory } returns Unit

        val result = ReflectionHelper.safeSetFactory2(inflater, factory)

        assertTrue("策略 1 写入失败降级到策略 2 成功时应返回 true", result)
        verify {
            Log.w(
                "MorphKit",
                match<String> { it.contains("反射写入 mFactory2 失败") && it.contains("降级到公开 API") },
                any()
            )
        }
        verify { inflater.factory2 = factory }
    }
}
