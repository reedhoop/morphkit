package com.morphkit.widget.button

import com.google.common.truth.Truth.assertThat
import io.mockk.mockkStatic
import android.util.Log
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * MorphButton 单元测试。
 *
 * 由于 JVM 环境无法实例化 AppCompatButton 子类，
 * 本测试聚焦于类结构验证、构造函数签名校验、内部枚举和包名合规性检查。
 */
class MorphButtonTest {

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
        val clazz = Class.forName("com.morphkit.widget.button.MorphButton")
        assertThat(clazz.`package`?.name).isEqualTo("com.morphkit.widget.button")
    }

    @Test
    fun `类名以 Morph 前缀开头`() {
        val clazz = Class.forName("com.morphkit.widget.button.MorphButton")
        assertThat(clazz.simpleName).startsWith("Morph")
    }

    @Test
    fun `继承自 AppCompatButton`() {
        val clazz = Class.forName("com.morphkit.widget.button.MorphButton")
        val superClass = clazz.superclass
        assertThat(superClass?.simpleName).isEqualTo("AppCompatButton")
    }

    @Test
    fun `具有 JvmOverloads 构造函数注解生成多个构造函数`() {
        val clazz = Class.forName("com.morphkit.widget.button.MorphButton")
        val constructors = clazz.declaredConstructors
        // JvmOverloads 会生成 3 个构造函数：全参、缺省 attrs、缺省 attrs+defStyleAttr
        assertThat(constructors.size).isAtLeast(2)
    }

    @Test
    fun `构造函数参数包含 Context 和 AttributeSet`() {
        val clazz = Class.forName("com.morphkit.widget.button.MorphButton")
        val constructor = clazz.getDeclaredConstructor(
            android.content.Context::class.java,
            android.util.AttributeSet::class.java
        )
        assertThat(constructor).isNotNull()
    }

    @Test
    fun `defStyleAttr 指向 morphButtonStyle`() {
        val rAttrClass = Class.forName("com.morphkit.R\$attr")
        val field = rAttrClass.getDeclaredField("morphButtonStyle")
        assertThat(Modifier.isPublic(field.modifiers)).isTrue()
        assertThat(field.type).isEqualTo(Int::class.javaPrimitiveType)
    }

    @Test
    fun `InteractionMode 枚举存在且包含 IOS 和 MATERIAL`() {
        val clazz = Class.forName("com.morphkit.widget.button.MorphButton")
        val innerClasses = clazz.declaredClasses
        val modeClass = innerClasses.firstOrNull { it.simpleName == "InteractionMode" }
        assertThat(modeClass).isNotNull()
        assertThat(modeClass!!.isEnum).isTrue()
        val enumConstants = modeClass.enumConstants
        assertThat(enumConstants!!.map { (it as Enum<*>).name })
            .containsExactly("IOS", "MATERIAL")
    }

    @Test
    fun `Style 枚举存在且包含 FILLED 和 PLAIN`() {
        val clazz = Class.forName("com.morphkit.widget.button.MorphButton")
        val styleClass = clazz.declaredClasses.first { it.simpleName == "Style" }
        assertThat(styleClass.isEnum).isTrue()
        val enumConstants = styleClass.enumConstants
        assertThat(enumConstants!!.map { (it as Enum<*>).name })
            .containsExactly("FILLED", "PLAIN")
    }
}
