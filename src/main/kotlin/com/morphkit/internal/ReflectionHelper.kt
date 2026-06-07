package com.morphkit.internal

import android.util.Log
import java.lang.reflect.Field

/**
 * MorphKit 内部反射工具类。
 *
 * 提供安全的反射字段访问与写入方法，
 * 供 [com.morphkit.core.MorphInstaller] 等内部组件使用。
 *
 * **注意**：此类属于内部实现，不对外暴露，后续版本可能随时变更。
 */
internal object ReflectionHelper {

    private const val TAG = "MorphKit"

    /**
     * 安全获取类的声明字段。
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @return 可访问的 Field，获取失败时返回 null
     */
    fun resolveField(clazz: Class<*>, fieldName: String): Field? {
        return try {
            val field = clazz.getDeclaredField(fieldName)
            if (!field.isAccessible) {
                field.isAccessible = true
            }
            field
        } catch (e: Exception) {
            Log.d(TAG, "反射获取字段 $fieldName 不可用: ${e.javaClass.simpleName}")
            null
        }
    }

    /**
     * 安全写入字段值。
     *
     * @param field  目标字段
     * @param target 目标对象
     * @param value  要设置的值
     * @throws Exception 写入失败时抛出
     */
    fun setFieldValue(field: Field, target: Any, value: Any) {
        try {
            field.set(target, value)
        } catch (e: Exception) {
            Log.d(TAG, "反射写入字段 ${field.name} 失败: ${e.javaClass.simpleName}")
            throw e
        }
    }
}
