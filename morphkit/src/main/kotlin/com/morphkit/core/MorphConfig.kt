package com.morphkit.core

import android.content.Context
import android.util.AttributeSet
import android.view.View
import java.util.concurrent.ConcurrentHashMap

/**
 * 统一控件前缀常量。
 *
 * 所有 MorphKit 生成的替换控件类名应以此前缀开头（如 `MorphTextView`、`MorphButton`），
 * 以便于在 Layout Inspector 与日志中快速辨识替换来源。
 * 此值同时作为 [MorphConfig.unifiedPrefix] 的数据源，以及运行时规范校验的判定依据。
 */
val unifiedPrefix = MorphConfig.DEFAULT_PREFIX

/**
 * 自适应风格策略枚举 — MorphKit 唯一的风格决策类型。
 *
 * 决定 MorphKit 在运行时选择哪套内置皮肤，由 [MorphConfig.stylePolicy] 配置。
 * View 体系（[MorphStyleResolver]）和 Compose 体系（MorphTheme composable）均使用
 * 此枚举，消除以往 `MorphStyle` / `StylePolicy` 双重概念的歧义。
 *
 * | 策略 | 行为 | 适用场景 |
 * |------|------|---------|
 * | [AUTO] | 根据设备是否支持 Dynamic Color 自动选择 | 默认策略，零配置 |
 * | [IOS] | 使用 iOS 极简风格 | 需要确定性强、不依赖系统取色的场景 |
 * | [PIXEL] | 使用 Pixel (Material You) 风格 | 需要原生 M3 体验的场景 |
 *
 * ## AUTO 策略判定逻辑
 *
 * ```
 * 设备支持 Dynamic Color (Android 12+ 且壁纸引擎可用)
 *   └─ → PIXEL (将色彩控制权交给系统壁纸引擎)
 * 设备不支持 Dynamic Color
 *   └─ → IOS (回退到确定性强、不依赖系统取色的极简风格)
 * ```
 *
 * @see MorphConfig.stylePolicy
 * @see MorphStyleResolver
 */
enum class StylePolicy {
    /** 自动检测：支持 Dynamic Color → PIXEL，否则 → IOS */
    AUTO,
    /** iOS 极简风格 */
    IOS,
    /** Pixel (Material You) 风格 */
    PIXEL;

    companion object {
        /** @suppress 向后兼容：旧代码中的 FORCE_IOS 等价于 [IOS] */
        @Deprecated("Use IOS instead", ReplaceWith("StylePolicy.IOS"))
        val FORCE_IOS: StylePolicy = IOS

        /** @suppress 向后兼容：旧代码中的 FORCE_PIXEL 等价于 [PIXEL] */
        @Deprecated("Use PIXEL instead", ReplaceWith("StylePolicy.PIXEL"))
        val FORCE_PIXEL: StylePolicy = PIXEL
    }
}

/**
 * MorphKit 控件替换规则配置类。
 *
 * 通过 DSL 方式声明控件的替换规则（[replace] / [groupReplace]）
 * 与属性修改规则（[modify]），以及自适应风格策略（[stylePolicy]）。
 * 配置完成后由 [MorphKit] 引擎在 LayoutInflater 回调中消费。
 *
 * 典型用法：
 * ```kotlin
 * MorphKit.init(application) {
 *     // 自适应策略（默认 AUTO，可省略）
 *     stylePolicy(StylePolicy.AUTO)
 *
 *     replace("TextView") { ctx, attrs ->
 *         MorphTextView(ctx, attrs)
 *     }
 *     groupReplace(listOf("Button", "androidx.appcompat.widget.AppCompatButton")) { ctx, attrs ->
 *         MorphButton(ctx, attrs)
 *     }
 *     modify("ImageView") { view ->
 *         view.alpha = 0.8f
 *     }
 * }
 * ```
 *
 * @constructor 内部构造，仅由 [MorphKit.init] 创建
 */
class MorphConfig internal constructor() {

    companion object {
        /** Default prefix for all MorphKit replacement widget class names. */
        const val DEFAULT_PREFIX = "Morph"
    }

    /** 替换规则映射：原始控件名 → 创建器（线程安全） */
    private val _replaceMap = ConcurrentHashMap<String, (Context, AttributeSet) -> View>()

    /** 属性修改规则映射：原始控件名 → 修改器（线程安全） */
    private val _modifyMap = ConcurrentHashMap<String, (View) -> Unit>()

    /** 对外只读的替换规则映射 */
    val replaceMap: Map<String, (Context, AttributeSet) -> View> get() = _replaceMap

    /** 对外只读的属性修改规则映射 */
    val modifyMap: Map<String, (View) -> Unit> get() = _modifyMap

    /**
     * 自适应风格策略，默认 [StylePolicy.AUTO]。
     *
     * 由 [stylePolicy] DSL 方法设置，在 [MorphKit.init] 完成后
     * 由 [MorphStyleResolver] 消费，计算出最终的 Theme ResId。
     *
     * @see StylePolicy
     * @see MorphStyleResolver
     */
    var policy: StylePolicy = StylePolicy.AUTO
        private set

    /**
     * 统一前缀，委托至顶层常量 [unifiedPrefix]。
     *
     * 用于运行时类名规范校验：替换控件的 `simpleName` 应以此前缀开头，
     * 否则 [MorphKit.stampAndValidateView] 将输出规范警告。
     */
    val unifiedPrefix: String
        get() = com.morphkit.core.unifiedPrefix

    /**
     * 设置自适应风格策略。
     *
     * 决定 MorphKit 在运行时选择 iOS 极简风还是 Pixel 原生风：
     * - [StylePolicy.AUTO]（默认）：根据设备是否支持 Dynamic Color 自动选择
     * - [StylePolicy.IOS]：iOS 极简风格
     * - [StylePolicy.PIXEL]：Pixel (Material You) 风格
     *
     * 示例：
     * ```kotlin
     * MorphKit.init(application) {
     *     stylePolicy(StylePolicy.IOS)  // iOS 风格
     *     // ...
     * }
     * ```
     *
     * @param policy 风格策略，默认 [StylePolicy.AUTO]
     * @see StylePolicy
     */
    fun stylePolicy(policy: StylePolicy) {
        this.policy = policy
    }

    /**
     * 注册单个控件替换规则。
     *
     * 当 [MorphKit.createView] 匹配到 [name] 时，
     * 将使用 [creator] 创建新的 View 实例替代原始控件，
     * 并自动调用 [MorphKit.stampAndValidateView] 进行调试打标。
     *
     * @param name    原始控件的简名或全限定名（如 `"TextView"`）
     * @param creator 替换控件的工厂方法，接收 [Context] 与 [AttributeSet]
     */
    fun replace(name: String, creator: (Context, AttributeSet) -> View) {
        require(name.isNotBlank()) { "replace: name 不能为空" }
        _replaceMap[name] = creator
    }

    /**
     * 注册别名分组替换规则。
     *
     * 解决 AppCompat 别名问题：同一逻辑控件在布局膨胀时可能以不同名称出现，
     * 例如 `TextView` 与 `AppCompatTextView` 实际指向同一个替换逻辑。
     * [groupReplace] 将 [names] 中每个名称均映射到同一个 [creator]，
     * 避免重复声明，减少遗漏风险。
     *
     * 内部实现等价于对 [names] 逐项调用 [replace]，但语义更清晰：
     * ```kotlin
     * // 以下两种写法等效，groupReplace 更简洁且意图明确
     * groupReplace(listOf("Button", "AppCompatButton"), ::MorphButton)
     * // vs
     * replace("Button", ::MorphButton)
     * replace("AppCompatButton", ::MorphButton)
     * ```
     *
     * @param names   需要统一替换的原始控件名称列表
     * @param creator 共用的替换控件工厂方法
     */
    fun groupReplace(names: List<String>, creator: (Context, AttributeSet) -> View) {
        require(names.isNotEmpty()) { "groupReplace: names 不能为空列表" }
        names.forEach { name ->
            _replaceMap[name] = creator
        }
    }

    /**
     * 注册后置属性修改规则。
     *
     * 当 [MorphKit.modifyView] 匹配到 [name] 时，
     * 将对已创建（或已替换）的 View 执行 [modifier] 进行属性兜底修改，
     * 例如统一色调、全局字体、圆角等。
     *
     * modify 与 replace 的区别：
     * - **replace**：整体替换控件类型（如 TextView → MorphTextView）
     * - **modify**：保留原控件类型，仅后置修改属性，作为兜底方案
     *
     * @param name     原始控件名称
     * @param modifier 属性修改器，接收已创建的 [View]
     */
    fun modify(name: String, modifier: (View) -> Unit) {
        require(name.isNotBlank()) { "modify: name 不能为空" }
        _modifyMap[name] = modifier
    }

}
