package com.morphkit.internal

import android.util.Log
import android.view.LayoutInflater
import java.lang.reflect.Field

/**
 * MorphKit 内部反射工具类。
 *
 * 提供安全的反射字段访问与写入方法，
 * 供 [com.morphkit.core.MorphInstaller] 等内部组件使用。
 *
 * ## Android 14+ (API 34+) 反射限制应对策略
 *
 * 从 Android 14 开始，Google 逐步限制对框架类私有字段的反射访问。
 * MorphKit 的策略是**优先使用公开 API**，仅在必要时才尝试反射降级：
 *
 * | 字段 | non-SDK 分类 | API 35 可访问性 | 应对策略 |
 * |------|-------------|---------------|---------|
 * | mFactory2 | greylist (unsupported) | 仍可反射访问 | 优先反射直接设置 |
 * | mFactory | greylist (unsupported) | 仍可反射访问 | 优先反射直接设置 |
 * | mFactorySet | greylist-max-p (blocked for targetSdk≥28) | **不可访问** | 降级到公开 API |
 *
 * **注意**：此类属于内部实现，不对外暴露，后续版本可能随时变更。
 */
internal object ReflectionHelper {

    private const val TAG = "MorphKit"

    /**
     * 判断当前运行环境是否受到 Android 14+ 反射限制。
     *
     * minSdk=35 意味着始终受限制（API >= 34），此方法始终返回 true。
     * 保留方法签名以保持调用方代码的可读性和未来兼容性。
     *
     * @return 始终返回 true（minSdk=35 >= 34）
     */
    fun isReflectionRestricted(): Boolean = true

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
     * 设置 LayoutInflater 的 Factory2 — 优先反射直接写入，降级到公开 API。
     *
     * ## 策略优先级（minSdk=35 实际执行路径）
     *
     * ```
     * 1. 反射获取 mFactory2 / mFactory 字段
     *    ├─ 成功 → 直接反射写入（绕过 mFactorySet 检查，最可靠）
     *    └─ 失败 → 降级到步骤 2
     *
     * 2. 尝试重置 mFactorySet 标志位 + 公开 API setFactory2
     *    ├─ mFactorySet 反射成功 → 重置为 false → 调用 inflater.factory2 = factory
     *    └─ mFactorySet 反射失败 → 直接调用 inflater.factory2 = factory
     *         ├─ 成功（AppCompat 尚未安装）→ 返回 true
     *         └─ IllegalStateException（AppCompat 已设置 Factory2）→ 返回 false
     * ```
     *
     * ## 为什么反射 mFactory2/mFactory 仍可能成功？
     *
     * 截至 Android 15（API 35），`mFactory2` 和 `mFactory` 仍属于 greylist (unsupported)
     * 而非 blacklist，反射访问会输出警告日志但不会抛异常。
     * 仅 `mFactorySet` 属于 greylist-max-p（targetSdk≥28 时被阻止）。
     *
     * @param inflater 目标 LayoutInflater
     * @param factory  要设置的 Factory2 实例
     * @return 设置成功返回 true，失败返回 false
     */
    fun safeSetFactory2(inflater: LayoutInflater, factory: LayoutInflater.Factory2): Boolean {
        // ── 策略 1：反射直接写入 mFactory2 + mFactory（最可靠路径）──
        val factory2Field = resolveField(LayoutInflater::class.java, "mFactory2")
        val factoryField = resolveField(LayoutInflater::class.java, "mFactory")

        if (factory2Field != null) {
            try {
                setFieldValue(factory2Field, inflater, factory)
                if (factoryField != null) {
                    try {
                        setFieldValue(factoryField, inflater, factory)
                    } catch (e: Exception) {
                        Log.d(TAG, "safeSetFactory2: mFactory 写入失败（mFactory2 已设置成功，不影响核心功能）: ${e.javaClass.simpleName}")
                    }
                }
                Log.d(TAG, "safeSetFactory2: 反射直接写入 mFactory2 成功")
                return true
            } catch (e: Exception) {
                Log.w(TAG, "safeSetFactory2: 反射写入 mFactory2 失败，降级到公开 API", e)
            }
        }

        // ── 策略 2：重置 mFactorySet + 公开 API setFactory2 ──
        val factorySetField = resolveField(LayoutInflater::class.java, "mFactorySet")
        if (factorySetField != null) {
            try {
                factorySetField.setBoolean(inflater, false)
            } catch (e: Exception) {
                Log.d(TAG, "重置 mFactorySet 失败，尝试直接 setFactory2", e)
            }
        } else {
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
