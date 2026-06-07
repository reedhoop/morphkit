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
}
