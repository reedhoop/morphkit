package com.morphkit.widget.container

import com.google.common.truth.Truth.assertThat
import io.mockk.mockkStatic
import android.util.Log
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * MorphCardView 单元测试。
 *
 * 由于 JVM 环境无法实例化 MaterialCardView 子类，
 * 本测试聚焦于类结构验证、构造函数签名校验、内部属性和包名合规性检查。
 */
class MorphCardViewTest {

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
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        assertThat(clazz.`package`?.name).isEqualTo("com.morphkit.widget.container")
    }

    @Test
    fun `类名以 Morph 前缀开头`() {
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        assertThat(clazz.simpleName).startsWith("Morph")
    }

    @Test
    fun `继承自 MaterialCardView`() {
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        val superClass = clazz.superclass
        assertThat(superClass?.simpleName).isEqualTo("MaterialCardView")
    }

    @Test
    fun `具有 JvmOverloads 构造函数注解生成多个构造函数`() {
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        val constructors = clazz.declaredConstructors
        assertThat(constructors.size).isAtLeast(2)
    }

    @Test
    fun `构造函数参数包含 Context 和 AttributeSet`() {
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        val constructor = clazz.getDeclaredConstructor(
            android.content.Context::class.java,
            android.util.AttributeSet::class.java
        )
        assertThat(constructor).isNotNull()
    }

    @Test
    fun `defStyleAttr 指向 morphCardStyle`() {
        val rAttrClass = Class.forName("com.morphkit.R\$attr")
        val field = rAttrClass.getDeclaredField("morphCardStyle")
        assertThat(Modifier.isPublic(field.modifiers)).isTrue()
        assertThat(field.type).isEqualTo(Int::class.javaPrimitiveType)
    }

    @Test
    fun `isGlassmorphism 属性存在且类型为 Boolean`() {
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        val field = clazz.getDeclaredField("isGlassmorphism")
        assertThat(field.type).isEqualTo(Boolean::class.javaPrimitiveType)
    }

    // ── S4 新增测试：毛玻璃模糊改进 ──

    @Test
    fun `glassmorphismBlurRadius 属性存在且类型为 Float`() {
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        val getter = clazz.getDeclaredMethod("getGlassmorphismBlurRadius")
        val setter = clazz.getDeclaredMethod("setGlassmorphismBlurRadius", Float::class.javaPrimitiveType)
        assertThat(getter.returnType).isEqualTo(Float::class.javaPrimitiveType)
        assertThat(setter).isNotNull()
    }

    @Test
    fun `refreshGlassmorphismBlur 公开方法存在`() {
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        val method = clazz.getDeclaredMethod("refreshGlassmorphismBlur")
        assertThat(Modifier.isPublic(method.modifiers)).isTrue()
        assertThat(method.returnType).isEqualTo(Void.TYPE)
    }

    @Test
    fun `不再使用 View setRenderEffect 做模糊`() {
        // S4 修复验证：MorphCardView 不应直接调用 setRenderEffect 做模糊
        // （模糊由 BackdropBlurHelper 在独立 Bitmap 上完成）
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        val methods = clazz.declaredMethods.map { it.name }
        // 旧的 applyBlurEffect 和 clearBlurEffect 方法应已移除
        assertThat(methods).doesNotContain("applyBlurEffect")
        assertThat(methods).doesNotContain("clearBlurEffect")
    }

    @Test
    fun `BackdropBlurHelper 工具类存在`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        // Kotlin object 编译后是单例，应有 INSTANCE 字段
        val instanceField = clazz.getDeclaredField("INSTANCE")
        assertThat(instanceField).isNotNull()
    }

    @Test
    fun `BackdropBlurHelper 提供 captureParentArea 方法`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val method = clazz.getDeclaredMethod("captureParentArea", android.view.View::class.java)
        assertThat(method.returnType).isEqualTo(android.graphics.Bitmap::class.java)
    }

    @Test
    fun `BackdropBlurHelper 提供 blur 方法`() {
        val clazz = Class.forName("com.morphkit.widget.container.BackdropBlurHelper")
        val method = clazz.getDeclaredMethod("blur", android.graphics.Bitmap::class.java, Float::class.javaPrimitiveType)
        assertThat(method.returnType).isEqualTo(android.graphics.Bitmap::class.java)
    }

    @Test
    fun `Companion 对象存在`() {
        val clazz = Class.forName("com.morphkit.widget.container.MorphCardView")
        val companion = clazz.declaredClasses.firstOrNull { it.simpleName == "Companion" }
        assertThat(companion).isNotNull()
    }

    @Test
    fun `XML 属性 isGlassmorphism 已声明`() {
        val rStyleableClass = Class.forName("com.morphkit.R\$styleable")
        val field = rStyleableClass.getDeclaredField("MorphCardView_isGlassmorphism")
        assertThat(field).isNotNull()
        assertThat(field.type).isEqualTo(Int::class.javaPrimitiveType)
    }

    @Test
    fun `XML 属性 glassmorphismBlurRadius 已声明`() {
        val rStyleableClass = Class.forName("com.morphkit.R\$styleable")
        val field = rStyleableClass.getDeclaredField("MorphCardView_glassmorphismBlurRadius")
        assertThat(field).isNotNull()
        assertThat(field.type).isEqualTo(Int::class.javaPrimitiveType)
    }
}
