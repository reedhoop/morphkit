package com.morphkit.engine

import android.content.Context
import android.os.Build
import android.util.Log
import com.morphkit.R

/**
 * MorphKit 智能自适应风格解析器。
 *
 * 根据 [StylePolicy] 在运行时决定最终使用的 Theme ResId，
 * 实现「零配置自动适配 + 宿主强制覆盖」的双重能力。
 *
 * ## 核心职责
 *
 * 1. **AUTO 策略检测**：检查设备是否支持 Material You Dynamic Color，
 *    支持则选择 Pixel 皮肤（将色彩控制权交给系统壁纸引擎），
 *    不支持则回退到 iOS 皮肤（确定性强、不依赖系统取色）。
 * 2. **强制策略**：[StylePolicy.FORCE_IOS] / [StylePolicy.FORCE_PIXEL]
 *    无视设备环境，直接返回对应的 Theme ResId。
 *
 * ## AUTO 判定逻辑
 *
 * ```
 * DynamicColors.isDynamicColorAvailable() == true
 *   或 Build.VERSION.SDK_INT >= S (31)
 *   └─ → R.style.Theme_MorphKit_Pixel
 *
 * 否则
 *   └─ → R.style.Theme_MorphKit_iOS
 * ```
 *
 * ## 使用方式
 *
 * 本类由 [MorphKit.init] 内部自动调用，外部通常无需直接使用：
 * ```kotlin
 * val themeResId = MorphStyleResolver.resolve(context, StylePolicy.AUTO)
 * ```
 *
 * @see StylePolicy
 * @see MorphKit
 */
object MorphStyleResolver {

    private const val TAG = "MorphKit"

    /**
     * 根据策略解析最终使用的 Theme 资源 ID。
     *
     * @param context 上下文，用于检测 Dynamic Color 可用性
     * @param policy  风格策略，来自 [MorphConfig.policy]
     * @return Theme 资源 ID（[R.style.Theme_MorphKit_iOS] 或 [R.style.Theme_MorphKit_Pixel]）
     */
    fun resolve(context: Context, policy: StylePolicy): Int {
        return when (policy) {
            StylePolicy.FORCE_IOS -> {
                Log.d(TAG, "Style policy applied -> iOS (forced)")
                R.style.Theme_MorphKit_iOS
            }
            StylePolicy.FORCE_PIXEL -> {
                Log.d(TAG, "Style policy applied -> Pixel (forced)")
                R.style.Theme_MorphKit_Pixel
            }
            StylePolicy.AUTO -> resolveAuto(context)
        }
    }

    /**
     * AUTO 策略核心检测逻辑。
     *
     * 检测顺序：
     * 1. 优先使用 Material 库的 [DynamicColors.isDynamicColorAvailable]，
     *    这是最准确的检测方式（考虑了定制 ROM 可能剥夺此功能的情况）。
     * 2. 若 DynamicColors API 不可用（如 material 库版本过低），
     *    降级为检查 `Build.VERSION.SDK_INT >= S`。
     *
     * @param context 上下文
     * @return Theme 资源 ID
     */
    private fun resolveAuto(context: Context): Int {
        val dynamicColorAvailable = checkDynamicColorAvailable(context)

        return if (dynamicColorAvailable) {
            Log.d(TAG, "Style policy applied -> Pixel (Dynamic Color)")
            R.style.Theme_MorphKit_Pixel
        } else {
            Log.d(TAG, "Style policy applied -> iOS")
            R.style.Theme_MorphKit_iOS
        }
    }

    /**
     * 检测设备是否支持 Material You Dynamic Color。
     *
     * 双重检测策略：
     * 1. 优先调用 `com.google.android.material.color.DynamicColors.isDynamicColorAvailable()`
     *    — 这是 Material 库提供的官方 API，不仅检查 API 级别，
     *    还会验证系统壁纸引擎是否真正可用（部分定制 ROM 可能剥夺此功能）。
     * 2. 若反射调用失败（material 库版本过低或类不存在），
     *    降级为 `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`。
     *
     * @param context 上下文
     * @return true 表示设备支持 Dynamic Color
     */
    private fun checkDynamicColorAvailable(context: Context): Boolean {
        // 策略 1：尝试使用 Material 库的 DynamicColors API
        try {
            val clazz = Class.forName("com.google.android.material.color.DynamicColors")
            val method = clazz.getMethod("isDynamicColorAvailable", Context::class.java)
            val result = method.invoke(null, context)
            if (result is Boolean) {
                return result
            }
        } catch (e: Exception) {
            // DynamicColors 类不存在或方法签名变更，降级到 API 级别检测
            Log.d(TAG, "DynamicColors.isDynamicColorAvailable() 不可用，降级到 API 级别检测", e)
        }

        // 策略 2：降级为 API 级别检测
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}
