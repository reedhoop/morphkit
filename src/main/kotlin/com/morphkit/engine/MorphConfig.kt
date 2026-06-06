package com.morphkit.engine

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * 统一控件前缀常量。
 *
 * 所有 MorphKit 生成的替换控件类名应以此前缀开头（如 `MorphTextView`、`MorphButton`），
 * 以便于在 Layout Inspector 与日志中快速辨识替换来源。
 * 此值同时作为 [MorphConfig.unifiedPrefix] 的数据源，以及运行时规范校验的判定依据。
 */
val unifiedPrefix = "Morph"

/**
 * MorphKit 控件替换规则配置类。
 *
 * 通过 DSL 方式声明控件的替换规则（[replace] / [groupReplace]）
 * 与属性修改规则（[modify]）。
 * 配置完成后由 [MorphKit] 引擎在 LayoutInflater 回调中消费。
 *
 * 典型用法：
 * ```kotlin
 * MorphKit.init(application) {
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

    /** 替换规则映射：原始控件名 → 创建器 */
    private val _replaceMap = mutableMapOf<String, (Context, AttributeSet) -> View>()

    /** 属性修改规则映射：原始控件名 → 修改器 */
    private val _modifyMap = mutableMapOf<String, (View) -> Unit>()

    /** 对外只读的替换规则映射 */
    val replaceMap: Map<String, (Context, AttributeSet) -> View> get() = _replaceMap

    /** 对外只读的属性修改规则映射 */
    val modifyMap: Map<String, (View) -> Unit> get() = _modifyMap

    /**
     * 统一前缀，委托至顶层常量 [unifiedPrefix]。
     *
     * 用于运行时类名规范校验：替换控件的 `simpleName` 应以此前缀开头，
     * 否则 [MorphKit.stampAndValidateView] 将输出规范警告。
     */
    val unifiedPrefix: String
        get() = com.morphkit.engine.unifiedPrefix

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
        _modifyMap[name] = modifier
    }
}
