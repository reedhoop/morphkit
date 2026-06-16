package com.morphkit.widget.container

import com.google.common.truth.Truth.assertThat
import io.mockk.mockkStatic
import android.util.Log
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * BackdropBlurHelper 单元测试。
 *
 * 由于 Bitmap 操作需要 Android 运行时，本测试聚焦于类结构验证、
 * 方法签名校验和内部实现完整性检查。
 */
class BackdropBlurHelperTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
    }

    @After
    fun tearDown() {
        // no-op
    }

    @Test
    fun `类存在于正确的包中`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        assertThat(clazz.`package`?.name).isEqualTo("com.morphkit.widget.container")
    }

    @Test
    fun `是 Kotlin object 单例`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val instanceField = clazz.getDeclaredField("INSTANCE")
        assertThat(instanceField).isNotNull()
        assertThat(Modifier.isStatic(instanceField.modifiers)).isTrue()
    }

    @Test
    fun `提供 captureParentArea 公开方法`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val method = clazz.getDeclaredMethod("captureParentArea", android.view.View::class.java)
        assertThat(method.returnType).isEqualTo(android.graphics.Bitmap::class.java)
        assertThat(Modifier.isPublic(method.modifiers)).isTrue()
    }

    @Test
    fun `提供 blur 公开方法`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val method = clazz.getDeclaredMethod("blur", android.graphics.Bitmap::class.java, Float::class.javaPrimitiveType)
        assertThat(method.returnType).isEqualTo(android.graphics.Bitmap::class.java)
        assertThat(Modifier.isPublic(method.modifiers)).isTrue()
    }

    @Test
    fun `内部包含软件 Stack Blur 降级方法`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val methodNames = clazz.declaredMethods.map { it.name }
        // minSdk=35: RenderEffect 内联到 blur() 中，blurWithRenderEffect/blurWithRenderNode 已移除
        // 仅保留 blurSoftware 作为软件 Canvas 的极端降级路径
        assertThat(methodNames).contains("blurSoftware")
        assertThat(methodNames).contains("stackBlurHorizontal")
        assertThat(methodNames).contains("stackBlurVertical")
    }

    @Test
    fun `不包含已废弃的低版本回退方法`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val methodNames = clazz.declaredMethods.map { it.name }
        // minSdk=35 后，API 29-30 的 RenderNode 直传和 API 31 的独立 RenderEffect 方法均已移除
        assertThat(methodNames).doesNotContain("blurWithRenderEffect")
        assertThat(methodNames).doesNotContain("blurWithRenderNode")
    }

    @Test
    fun `模糊策略方法均为私有`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val privateBlurMethods = clazz.declaredMethods
            .filter { it.name.startsWith("blur") && it.name != "blur" }

        for (method in privateBlurMethods) {
            assertThat(Modifier.isPrivate(method.modifiers)).isTrue()
        }
    }

    @Test
    fun `Stack Blur 内部方法均为私有`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val stackMethods = clazz.declaredMethods
            .filter { it.name.startsWith("stack") }

        assertThat(stackMethods).isNotEmpty()
        for (method in stackMethods) {
            assertThat(Modifier.isPrivate(method.modifiers)).isTrue()
        }
    }

    @Test
    fun `blur 方法接受 Bitmap 和 Float 参数`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val method = clazz.getDeclaredMethod("blur", android.graphics.Bitmap::class.java, Float::class.javaPrimitiveType)
        val params = method.parameterTypes
        assertThat(params).hasLength(2)
        assertThat(params[0]).isEqualTo(android.graphics.Bitmap::class.java)
        assertThat(params[1]).isEqualTo(Float::class.javaPrimitiveType)
    }

    @Test
    fun `captureParentArea 方法仅接受 View 参数`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val method = clazz.getDeclaredMethod("captureParentArea", android.view.View::class.java)
        val params = method.parameterTypes
        assertThat(params).hasLength(1)
        assertThat(params[0]).isEqualTo(android.view.View::class.java)
    }

    @Test
    fun `公开方法恰好五个`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val publicMethods = clazz.declaredMethods
            .filter { Modifier.isPublic(it.modifiers) }
            .map { it.name }
            .toSet()

        assertThat(publicMethods).containsExactly("captureParentArea", "blur", "obtainBitmap", "recycleToPool", "clearPool")
    }
}
