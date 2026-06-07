package com.morphkit.internal

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
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
     * 判断当前运行环境是否受到 Android 14+ 反射限制。
     *
     * 从 Android 14（API 34）开始，Google 限制了对框架类私有字段的反射访问，
     * 例如 [LayoutInflater.mFactory2] 可能变得不可访问。
     *
     * @return 当前 API level >= 34 时返回 true
     */
    fun isReflectionRestricted(): Boolean {
        return Build.VERSION.SDK_INT >= 34
    }

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
            if (isReflectionRestricted()) {
                Log.w(TAG, "反射获取字段 $fieldName 失败: Android 14+ 限制了框架类私有字段的反射访问 (${e.javaClass.simpleName})")
            } else {
                Log.d(TAG, "反射获取字段 $fieldName 不可用: ${e.javaClass.simpleName}")
            }
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

    /**
     * 使用公开 API [LayoutInflater.setFactory2] 设置 Factory2 的安全降级方案。
     *
     * 在 Android 14+ 上，反射访问 [LayoutInflater.mFactory2] 可能受限，
     * 此方法作为降级回退：先尝试重置 `mFactorySet` 标志位，
     * 再通过公开的 [LayoutInflater.setFactory2] 完成设置。
     *
     * @param inflater 目标 LayoutInflater
     * @param factory  要设置的 Factory2 实例
     * @return 设置成功返回 true，失败返回 false
     */
    fun safeSetFactory2(inflater: LayoutInflater, factory: LayoutInflater.Factory2): Boolean {
        // 尝试重置 mFactorySet，使 setFactory2 不抛 IllegalStateException
        val factorySetField = resolveField(LayoutInflater::class.java, "mFactorySet")
        if (factorySetField != null) {
            try {
                factorySetField.setBoolean(inflater, false)
            } catch (e: Exception) {
                Log.d(TAG, "重置 mFactorySet 失败，尝试直接 setFactory2", e)
            }
        } else if (isReflectionRestricted()) {
            Log.w(TAG, "Android 14+ 无法反射访问 mFactorySet，尝试直接 setFactory2")
        }

        return try {
            inflater.factory2 = factory
            Log.d(TAG, "safeSetFactory2: 通过公开 API 设置 Factory2 成功")
            true
        } catch (e: IllegalStateException) {
            Log.w(TAG, "safeSetFactory2 失败（mFactorySet 重置无效），MorphKit 注入无法生效", e)
            false
        } catch (e: Exception) {
            Log.w(TAG, "safeSetFactory2 异常", e)
            false
        }
    }
}
