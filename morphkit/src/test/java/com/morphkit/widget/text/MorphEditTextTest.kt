package com.morphkit.widget.text

import com.google.common.truth.Truth.assertThat
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import android.util.Log
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * MorphEditText 单元测试。
 *
 * 由于 JVM 环境无法实例化 AppCompatEditText 子类，
 * 本测试聚焦于类结构验证、构造函数签名校验、内部枚举和包名合规性检查。
 */
class MorphEditTextTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `类存在于正确的包中`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphEditText")
        assertThat(clazz.`package`?.name).isEqualTo("com.morphkit.widget.text")
    }

    @Test
    fun `类名以 Morph 前缀开头`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphEditText")
        assertThat(clazz.simpleName).startsWith("Morph")
    }

    @Test
    fun `继承自 AppCompatEditText`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphEditText")
        val superClass = clazz.superclass
        assertThat(superClass?.simpleName).isEqualTo("AppCompatEditText")
    }

    @Test
    fun `具有 JvmOverloads 构造函数注解生成多个构造函数`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphEditText")
        val constructors = clazz.declaredConstructors
        assertThat(constructors.size).isAtLeast(2)
    }

    @Test
    fun `构造函数参数包含 Context 和 AttributeSet`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphEditText")
        val constructor = clazz.getDeclaredConstructor(
            android.content.Context::class.java,
            android.util.AttributeSet::class.java
        )
        assertThat(constructor).isNotNull()
    }

    @Test
    fun `defStyleAttr 指向 morphEditTextStyle`() {
        val rAttrClass = Class.forName("com.morphkit.R\$attr")
        val field = rAttrClass.getDeclaredField("morphEditTextStyle")
        assertThat(Modifier.isPublic(field.modifiers)).isTrue()
        assertThat(field.type).isEqualTo(Int::class.javaPrimitiveType)
    }

    @Test
    fun `Style 枚举存在且包含 BARE 和 SEARCH`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphEditText")
        val styleClass = clazz.declaredClasses.first { it.simpleName == "Style" }
        assertThat(styleClass.isEnum).isTrue()
        val enumConstants = styleClass.enumConstants
        assertThat(enumConstants!!.map { (it as Enum<*>).name })
            .containsExactly("BARE", "SEARCH")
    }

    @Test
    fun `style 属性存在且类型为 Style 枚举`() {
        val clazz = Class.forName("com.morphkit.widget.text.MorphEditText")
        val styleClass = clazz.declaredClasses.first { it.simpleName == "Style" }
        val field = clazz.getDeclaredField("style")
        assertThat(field.type).isEqualTo(styleClass)
    }
}
