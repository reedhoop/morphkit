package com.morphkit.engine

import android.app.Application
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.morphkit.R

/**
 * MorphKit 调试打标用的 Tag Key。
 *
 * 通过 `R.id.morph_view_tag` 引用 Android 资源系统分配的真实 ID，
 * 避免与业务层通过 [View.setTag] / [View.getTag] 设置的 Tag 产生冲突。
 * 存储内容格式：`"Morph (replaced: $originalName)"`，
 * 可在 Layout Inspector 或调试器中直接查看替换来源。
 *
 * @see com.morphkit.R.id.morph_view_tag
 */
val MORPH_TAG_KEY = R.id.morph_view_tag

/*
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║                    统一控件接入防御指南                                     ║
 * ║                  (MorphWidget Integration Defense Guide)                  ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                          ║
 * ║  本框架通过 LayoutInflater.Factory2 在布局膨胀阶段拦截控件创建，             ║
 * ║  用自定义控件替换原始控件。为确保替换后功能、样式、主题完全正常，              ║
 * ║  所有接入 MorphKit 的自定义控件 **必须** 严格遵守以下三条规范：               ║
 * ║                                                                          ║
 * ║  ┌────────────────────────────────────────────────────────────────────┐  ║
 * ║  │ ⚠️  规范 1：构造函数必须接收 attrs，严禁忽略                         │  ║
 * ║  ├────────────────────────────────────────────────────────────────────┤  ║
 * ║  │                                                                    │  ║
 * ║  │  XML 中声明的所有属性（text、textColor、textSize、background 等）    │  ║
 * ║  │  均由 AttributeSet 驱动解析。若构造函数忽略 attrs，                  │  ║
 * ║  │  XML 属性将全部静默丢失，导致控件「裸奔」——无文字、无颜色、无背景。    │  ║
 * ║  │                                                                    │  ║
 * ║  │  ✅ 正确：双参或三参构造，attrs 传递给 super                        │  ║
 * ║  │     class MorphTextView @JvmOverloads constructor(                 │  ║
 * ║  │         context: Context,                                          │  ║
 * ║  │         attrs: AttributeSet? = null,                               │  ║
 * ║  │         defStyleAttr: Int = androidx.appcompat.R.attr               │  ║
 * ║  │                           .textViewStyle                           │  ║
 * ║  │     ) : AppCompatTextView(context, attrs, defStyleAttr)            │  ║
 * ║  │                                                                    │  ║
 * ║  │  ❌ 错误：忽略 attrs，XML 属性全部丢失                              │  ║
 * ║  │     class MorphTextView(context: Context) :                        │  ║
 * ║  │         AppCompatTextView(context)  // attrs 丢失！                │  ║
 * ║  │                                                                    │  ║
 * ║  └────────────────────────────────────────────────────────────────────┘  ║
 * ║                                                                          ║
 * ║  ┌────────────────────────────────────────────────────────────────────┐  ║
 * ║  │ ⚠️  规范 2：推荐继承 AppCompat 系控件，保主题着色不断链              │  ║
 * ║  ├────────────────────────────────────────────────────────────────────┤  ║
 * ║  │                                                                    │  ║
 * ║  │  AppCompat 系控件（AppCompatTextView、AppCompatButton 等）          │  ║
 * ║  │  内置了主题着色拦截器（TintContextWrapper）、矢量图着色、             │  ║
 * ║  │  上下文包装等兼容性处理。若直接继承原生控件（TextView、Button），      │  ║
 * ║  │  以下能力将全部丢失：                                                │  ║
 * ║  │  - 主题 textColor / background tint 不生效                         │  ║
 * ║  │  - 矢量图标 (VectorDrawable) 在低版本崩溃                          │  ║
 * ║  │  - android:theme 属性失效                                          │  ║
 * ║  │                                                                    │  ║
 * ║  │  ✅ 正确：继承 AppCompat 系控件                                     │  ║
 * ║  │     class MorphTextView(...) : AppCompatTextView(...)              │  ║
 * ║  │     class MorphButton(...)  : AppCompatButton(...)                │  ║
 * ║  │     class MorphImageView(...) : AppCompatImageView(...)           │  ║
 * ║  │                                                                    │  ║
 * ║  │  ❌ 错误：继承原生控件，主题着色断链                                │  ║
 * ║  │     class MorphTextView(...) : TextView(...)    // 丢失着色！      │  ║
 * ║  │     class MorphButton(...)  : Button(...)      // 丢失着色！      │  ║
 * ║  │                                                                    │  ║
 * ║  └────────────────────────────────────────────────────────────────────┘  ║
 * ║                                                                          ║
 * ║  ┌────────────────────────────────────────────────────────────────────┐  ║
 * ║  │ ⚠️  规范 3：类名必须以 `Morph` 开头，否则运行时收到 Logcat 警告      │  ║
 * ║  ├────────────────────────────────────────────────────────────────────┤  ║
 * ║  │                                                                    │  ║
 * ║  │  框架通过 [MorphKit.stampAndValidateView] 在运行时校验替换控件       │  ║
 * ║  │  的类名前缀。不以 `Morph` 开头的控件将触发规范警告：                  │  ║
 * ║  │                                                                    │  ║
 * ║  │  W/MorphKit: 规范警告：替换控件 com.xxx.MyButton 未遵循前缀规范！   │  ║
 * ║  │              建议重命名为 MorphButton                               │  ║
 * ║  │                                                                    │  ║
 * ║  │  此约束确保在 Layout Inspector、Logcat、堆栈追踪中                  │  ║
 * ║  │  可一键识别 MorphKit 替换的控件，降低排查成本。                      │  ║
 * ║  │                                                                    │  ║
 * ║  │  ✅ 正确：MorphTextView、MorphButton、MorphRecyclerView            │  ║
 * ║  │  ❌ 错误：MyTextView、CustomButton、SuperImageView                  │  ║
 * ║  │                                                                    │  ║
 * ║  └────────────────────────────────────────────────────────────────────┘  ║
 * ║                                                                          ║
 * ║  违反上述规范不会导致崩溃，但会导致：                                      ║
 * ║  - XML 属性丢失（规范 1）                                                ║
 * ║  - 主题着色断链（规范 2）                                                ║
 * ║  - 运行时规范警告（规范 3）                                              ║
 * ║                                                                          ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * MorphKit 控件动态替换框架核心引擎。
 *
 * 作为全局单例提供统一的控件替换与属性修改入口，核心能力包括：
 *
 * 1. **规则驱动替换**：基于 [MorphConfig] 中声明的 replace / groupReplace 规则，
 *    在 LayoutInflater 回调中拦截原始控件创建，返回替换后的 View。
 * 2. **属性兜底修改**：基于 modify 规则，对未替换或已替换的 View 执行后置属性修改。
 * 3. **智能调试打标**：通过 [stampAndValidateView] 自动为替换控件添加 Tag 标记，
 *    并校验类名是否符合 `Morph` 前缀规范。
 * 4. **运行时规范校验**：替换控件的类名须以 [unifiedPrefix] 开头，
 *    否则在 Logcat 中输出 `MorphKit` 标签的规范警告。
 * 5. **全局自动注入**：通过 [MorphInstaller] 在每个 Activity 的 `onActivityPreCreated`
 *    阶段自动注入 [MorphFactory2]，无需手动集成。
 *
 * ## 快速接入
 *
 * 在 [Application.onCreate] 中一行代码完成初始化：
 * ```kotlin
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         MorphKit.init(this) {
 *             groupReplace(listOf("TextView", "androidx.appcompat.widget.AppCompatTextView")) { ctx, attrs ->
 *                 MorphTextView(ctx, attrs)
 *             }
 *             modify("RecyclerView") { rv -> rv.overScrollMode = View.OVER_SCROLL_NEVER }
 *         }
 *     }
 * }
 * ```
 *
 * ## 注意事项
 *
 * 所有自定义替换控件必须严格遵守上方《统一控件接入防御指南》中的三条规范，
 * 否则可能出现 XML 属性丢失、主题着色断链或运行时规范警告等问题。
 *
 * @see MorphConfig 替换/修改规则 DSL 配置
 * @see MorphFactory2 LayoutInflater.Factory2 代理拦截器
 * @see MorphInstaller 全局自动注入器
 */
object MorphKit {

    /** 当前配置，初始化后可用 */
    private lateinit var config: MorphConfig

    /** 是否已初始化 */
    private var initialized = false

    /**
     * 初始化 MorphKit 引擎。
     *
     * 完成以下操作（原子性，不可部分执行）：
     * 1. 创建 [MorphConfig] 并执行 DSL 配置块，注册所有 replace / groupReplace / modify 规则
     * 2. 调用 [MorphInstaller.install] 注册 Activity 生命周期回调，在每个 Activity 启动前
     *    自动注入 [MorphFactory2]
     *
     * 应在 [Application.onCreate] 中尽早调用，
     * 以确保在 LayoutInflater 膨胀布局前完成所有规则注册与注入器安装。
     * 重复调用将抛出 [IllegalStateException]。
     *
     * @param application 应用实例，用于注册 ActivityLifecycleCallbacks
     * @param block       [MorphConfig] DSL 配置块，在其中声明 replace / groupReplace / modify 规则
     * @throws IllegalStateException 若重复初始化
     */
    fun init(application: Application, block: MorphConfig.() -> Unit) {
        check(!initialized) { "MorphKit 已初始化，禁止重复调用 init()" }
        config = MorphConfig().apply(block)
        initialized = true
        // 自动安装全局 Factory2 注入，在每个 Activity 启动前拦截 LayoutInflater
        MorphInstaller.install(application)
    }

    /**
     * 创建替换控件。
     *
     * 查找 [originalName] 是否命中 [MorphConfig.replaceMap] 中的替换规则：
     * - **命中**：调用对应的 creator 创建新 View，并执行 [stampAndValidateView] 打标与规范校验。
     * - **未命中**：返回 `null`，由调用方回退到原始控件创建流程。
     *
     * 该方法是 [MorphFactory2] 硬替换阶段的核心入口，
     * 由 [MorphFactory2.onCreateView] 自动调用，外部通常无需直接使用。
     *
     * @param originalName 原始控件名称（简名或全限定名，须与 [MorphConfig.replace] 注册时的 key 一致）
     * @param context      上下文
     * @param attrs        属性集（**严禁忽略**，见《统一控件接入防御指南》规范 1）
     * @return 替换后的 View，若未命中规则则返回 `null`
     */
    fun createView(originalName: String, context: Context, attrs: AttributeSet): View? {
        val creator = config.replaceMap[originalName] ?: return null
        val view = creator(context, attrs)
        stampAndValidateView(view, originalName)
        return view
    }

    /**
     * 后置属性修改。
     *
     * 查找 [originalName] 是否命中 [MorphConfig.modifyMap] 中的修改规则：
     * - **命中**：对 [view] 执行 modifier 进行属性兜底修改，返回同一实例。
     * - **未命中**：直接返回 [view] 本身，不做任何修改。
     *
     * 该方法由 [MorphFactory2] 在以下两个阶段调用：
     * 1. 硬替换成功后，对替换控件执行后置属性修改
     * 2. 降级创建原生控件后，对原生控件执行软修改兜底
     *
     * @param originalName 原始控件名称
     * @param view         待修改的 View 实例
     * @return 修改后的 View（同一实例）
     */
    fun modifyView(originalName: String, view: View): View {
        val modifier = config.modifyMap[originalName] ?: return view
        modifier(view)
        return view
    }

    /**
     * 智能调试打标与运行时规范校验。
     *
     * 执行两个核心操作：
     *
     * **1. 调试打标**
     * 自动为 [view] 设置 MorphKit 替换标记，格式为 `"Morph (replaced: $originalName)"`，
     * 存储于 [MORPH_TAG_KEY] 对应的 Tag 中。
     * 在 Layout Inspector、调试断点或 `view.getTag(MORPH_TAG_KEY)` 中可直接查看替换来源。
     *
     * **2. 类名前缀规范校验**（见《统一控件接入防御指南》规范 3）
     * 检查 [view] 的 [Class.getSimpleName] 是否以 [MorphConfig.unifiedPrefix]（即 `"Morph"`）开头：
     * - 符合规范：静默通过
     * - 不符合规范：通过 [Log.w] 输出 `MorphKit` 标签的规范警告，
     *   建议将控件类重命名为 `Morph${originalName}`
     *
     * 该方法由 [createView] 自动调用，外部通常无需直接使用。
     *
     * @param view         替换后的控件实例
     * @param originalName 被替换的原始控件名称
     */
    internal fun stampAndValidateView(view: View, originalName: String) {
        // 打标：写入调试信息
        val tagValue = "${config.unifiedPrefix} (replaced: $originalName)"
        view.setTag(MORPH_TAG_KEY, tagValue)

        // 规范校验：检查替换控件类名是否以统一前缀开头
        val className = view.javaClass.simpleName
        if (!className.startsWith(config.unifiedPrefix)) {
            Log.w(
                "MorphKit",
                "规范警告：替换控件 ${view.javaClass.name} 未遵循前缀规范！" +
                        "建议重命名为 ${config.unifiedPrefix}${originalName}"
            )
        }
    }
}
