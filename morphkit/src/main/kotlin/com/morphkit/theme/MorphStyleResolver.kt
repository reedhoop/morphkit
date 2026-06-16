package com.morphkit.theme

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.morphkit.R
import com.morphkit.core.StylePolicy

/**
 * MorphKit 智能自适应风格解析器。
 *
 * 根据 [StylePolicy] 和 OEM 系统设置在运行时决定最终使用的 Theme ResId，
 * 实现「零配置自动适配 + OEM 全局下发 + 宿主强制覆盖」三重能力。
 *
 * ## 核心职责
 *
 * 1. **OEM 系统设置优先**：读取 `Settings.System.getInt(contentResolver, "oem_ui_style", 0)`，
 *    OEM 厂商只需在系统设置 App 中修改此值，所有接入 MorphKit 的预装 App 即可瞬间整体变装。
 * 2. **AUTO 策略检测**：当 OEM 未设置时，检查设备是否支持 Material You Dynamic Color，
 *    支持则选择 Pixel 皮肤，不支持则回退到 iOS 皮肤。
 * 3. **强制策略**：[StylePolicy.IOS] / [StylePolicy.PIXEL]
 *    无视设备环境，直接返回对应的 Theme ResId。
 *
 * ## 优先级链路
 *
 * ```
 * Settings.System "oem_ui_style"
 *   ├─ 值为 1 → iOS 极简风（OEM 指定）
 *   ├─ 值为 2 → Pixel 原生风（OEM 指定）
 *   └─ 值为 0（默认）→ 根据 StylePolicy 判定
 *        ├─ IOS → iOS
 *        ├─ PIXEL → Pixel
 *        └─ AUTO → Dynamic Color 可用 → Pixel，否则 → iOS
 * ```
 *
 * ## 基线保护声明
 *
 * 本 AAR 仅限于替换本 OEM 预装应用内的控件，不具备干涉第三方应用的能力。
 * 若需全局系统级换肤，请结合 Android RRO (Runtime Resource Overlay) 机制使用。
 *
 * @see StylePolicy
 * @see MorphKit
 */
object MorphStyleResolver {

    private const val TAG = "MorphKit"

    /**
     * OEM 系统设置键名。
     *
     * 通过 `Settings.System.getInt(contentResolver, [OEM_UI_STYLE_KEY], 0)` 读取。
     * OEM 厂商在系统设置 App 中修改此值即可全局切换所有预装 App 的风格。
     */
    const val OEM_UI_STYLE_KEY = "oem_ui_style"

    /** OEM 系统设置值：未设置，走 AUTO 逻辑 */
    const val OEM_STYLE_DEFAULT = 0

    /** OEM 系统设置值：iOS 极简风 */
    const val OEM_STYLE_IOS = 1

    /** OEM 系统设置值：Pixel 原生风 */
    const val OEM_STYLE_PIXEL = 2

    /**
     * 根据策略和 OEM 系统设置解析最终使用的 Theme 资源 ID。
     *
     * 优先级：OEM 系统设置 > StylePolicy。
     *
     * @param context 上下文，用于检测 Dynamic Color 可用性和读取系统设置
     * @param policy  风格策略，来自 [MorphConfig.policy]
     * @return Theme 资源 ID（[R.style.Theme_MorphKit_iOS] 或 [R.style.Theme_MorphKit_Pixel]）
     */
    fun resolve(context: Context, policy: StylePolicy): Int {
        // ── 优先级 1：OEM 系统设置 ──
        val oemStyle = readOemStyle(context)
        if (oemStyle != OEM_STYLE_DEFAULT) {
            return when (oemStyle) {
                OEM_STYLE_IOS -> {
                    Log.d(TAG, "Style policy applied -> iOS (OEM system setting)")
                    R.style.Theme_MorphKit_iOS
                }
                OEM_STYLE_PIXEL -> {
                    Log.d(TAG, "Style policy applied -> Pixel (OEM system setting)")
                    R.style.Theme_MorphKit_Pixel
                }
                else -> {
                    Log.w(TAG, "未知的 OEM 系统设置值: $oemStyle，回退到 StylePolicy 判定")
                    resolveByPolicy(context, policy)
                }
            }
        }

        // ── 优先级 2：StylePolicy ──
        return resolveByPolicy(context, policy)
    }

    /**
     * 根据 StylePolicy 解析 Theme。
     *
     * @param context 上下文
     * @param policy  风格策略
     * @return Theme 资源 ID
     */
    private fun resolveByPolicy(context: Context, policy: StylePolicy): Int {
        return when (policy) {
            StylePolicy.IOS -> {
                Log.d(TAG, "Style policy applied -> iOS (forced)")
                R.style.Theme_MorphKit_iOS
            }
            StylePolicy.PIXEL -> {
                Log.d(TAG, "Style policy applied -> Pixel (forced)")
                R.style.Theme_MorphKit_Pixel
            }
            StylePolicy.AUTO -> resolveAuto(context)
        }
    }

    /**
     * 读取 OEM 系统设置值。
     *
     * 通过 `Settings.System.getInt(contentResolver, "oem_ui_style", 0)` 读取。
     * 若读取异常（权限不足、设置项不存在等），安全回退到 0（默认）。
     *
     * @param context 上下文
     * @return OEM 风格值（0=默认, 1=iOS, 2=Pixel）
     */
    private fun readOemStyle(context: Context): Int {
        return try {
            Settings.System.getInt(context.contentResolver, OEM_UI_STYLE_KEY, OEM_STYLE_DEFAULT)
        } catch (e: Exception) {
            // 部分定制 ROM 可能限制 Settings.System 读取权限
            Log.d(TAG, "读取 OEM 系统设置异常，使用默认值", e)
            OEM_STYLE_DEFAULT
        }
    }

    /**
     * AUTO 策略核心检测逻辑。
     *
     * 检测顺序：
     * 1. 优先使用 Material 库的 DynamicColors.isDynamicColorAvailable，
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
     * 缓存的 DynamicColors.isDynamicColorAvailable 方法引用。
     *
     * 使用 lazy + @Synchronized 保证反射查找的原子性：
     * 首次调用时执行 Class.forName + getMethod，后续调用直接复用。
     * 首次查找失败后缓存为 null（不再重试）。
     * 相比之前的 @Volatile + boolean 标记，lazy 天然保证单次初始化且线程安全。
     */
    private val dynamicColorMethod: java.lang.reflect.Method? by lazy {
        try {
            val clazz = Class.forName("com.google.android.material.color.DynamicColors")
            clazz.getMethod("isDynamicColorAvailable", Context::class.java)
        } catch (e: Exception) {
            Log.d(TAG, "DynamicColors.isDynamicColorAvailable() 不可用，降级到 API 级别检测", e)
            null
        }
    }

    /**
     * 检测设备是否支持 Material You Dynamic Color。
     *
     * 双重检测策略：
     * 1. 优先调用 DynamicColors.isDynamicColorAvailable()（反射结果已缓存，避免重复查找）
     * 2. 若反射调用失败，降级为 `Build.VERSION.SDK_INT >= S`
     *
     * @param context 上下文
     * @return true 表示设备支持 Dynamic Color
     */
    private fun checkDynamicColorAvailable(context: Context): Boolean {
        dynamicColorMethod?.let { method ->
            try {
                val result = method.invoke(null, context)
                if (result is Boolean) return result
            } catch (e: Exception) {
                Log.d(TAG, "DynamicColors.isDynamicColorAvailable() 调用失败，降级到 API 级别检测", e)
            }
        }

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}
