package com.morphkit.widget.text

import com.google.common.truth.Truth.assertThat
import io.mockk.mockkStatic
import android.util.Log
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * MorphTextView 单元测试。
 *
 * 由于 JVM 环境无法实例化 AppCompatTextView 子类，
 * 本测试聚焦于类结构验证、构造函数签名校验、内部属性和包名合规性检查。
 */
class MorphTextViewTest {

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
        val clazz = Class.forName("com.morphkit.widget.text.MorphTextView")
        assertThat(clazz.`package`?.name).isEqualTo("com.morphkit.widget.text")
    }

    @Test
    fun `类名以 Morph 前缀开头`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphTextView")
        assertThat(clazz.simpleName).startsWith("Morph")
    }

    @Test
    fun `继承自 AppCompatTextView`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphTextView")
        val superClass = clazz.superclass
        assertThat(superClass?.simpleName).isEqualTo("AppCompatTextView")
    }

    @Test
    fun `具有 JvmOverloads 构造函数注解生成多个构造函数`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphTextView")
        val constructors = clazz.declaredConstructors
        assertThat(constructors.size).isAtLeast(2)
    }

    @Test
    fun `构造函数参数包含 Context 和 AttributeSet`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphTextView")
        val constructor = clazz.getDeclaredConstructor(
            android.content.Context::class.java,
            android.util.AttributeSet::class.java
        )
        assertThat(constructor).isNotNull()
    }

    @Test
    fun `defStyleAttr 指向 morphTextViewStyle`() {
        val rAttrClass = Class.forName("com.morphkit.R\$attr")
        val field = rAttrClass.getDeclaredField("morphTextViewStyle")
        assertThat(Modifier.isPublic(field.modifiers)).isTrue()
        assertThat(field.type).isEqualTo(Int::class.javaPrimitiveType)
    }

    @Test
    fun `isSecondaryText 属性存在且类型为 Boolean`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphTextView")
        val field = clazz.getDeclaredField("isSecondaryText")
        assertThat(field.type).isEqualTo(Boolean::class.javaPrimitiveType)
    }

    @Test
    fun `isTertiaryText 属性存在且类型为 Boolean`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphTextView")
        val field = clazz.getDeclaredField("isTertiaryText")
        assertThat(field.type).isEqualTo(Boolean::class.javaPrimitiveType)
    }
}
