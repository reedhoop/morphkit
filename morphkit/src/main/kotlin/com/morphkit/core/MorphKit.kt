package com.morphkit.core

import android.app.Application
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.morphkit.R
import com.morphkit.theme.MorphStyleResolver
import java.util.concurrent.atomic.AtomicBoolean

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

/*
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║                MorphKit 多风格扩展指南                                     ║
 * ║             (Multi-Style Extension Guide)                                 ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                          ║
 * ║  MorphKit 采用「引擎与皮肤分离」架构，内置 iOS 极简风和 Pixel 原生风        ║
 * ║  两套完整皮肤。宿主 App 可一行代码切换，也可扩展第三套自定义皮肤。           ║
 * ║                                                                          ║
 * ║  ┌────────────────────────────────────────────────────────────────────┐  ║
 * ║  │ 1. 切换内置皮肤                                                    │  ║
 * ║  ├────────────────────────────────────────────────────────────────────┤  ║
 * ║  │                                                                    │  ║
 * ║  │  在 AndroidManifest.xml 的 <application> 或 <activity> 中设置：     │  ║
 * ║  │                                                                    │  ║
 * ║  │  iOS 极简风：                                                      │  ║
 * ║  │    android:theme="@style/Theme.MorphKit.iOS"                       │  ║
 * ║  │                                                                    │  ║
 * ║  │  Pixel 原生风：                                                    │  ║
 * ║  │    android:theme="@style/Theme.MorphKit.Pixel"                     │  ║
 * ║  │                                                                    │  ║
 * ║  └────────────────────────────────────────────────────────────────────┘  ║
 * ║                                                                          ║
 * ║  ┌────────────────────────────────────────────────────────────────────┐  ║
 * ║  │ 2. 扩展第三套自定义皮肤（如公司大促皮肤）                            │  ║
 * ║  ├────────────────────────────────────────────────────────────────────┤  ║
 * ║  │                                                                    │  ║
 * ║  │  步骤 1：宿主 App 继承一个内置 Theme                               │  ║
 * ║  │                                                                    │  ║
 * ║  │    <style name="Theme.MyApp.Promotion"                             │  ║
 * ║  │        parent="Theme.MorphKit.iOS">                                │  ║
 * ║  │    </style>                                                        │  ║
 * ║  │                                                                    │  ║
 * ║  │  步骤 2：在宿主 styles.xml 中重写 morph*Style 属性                  │  ║
 * ║  │                                                                    │  ║
 * ║  │    <style name="Theme.MyApp.Promotion"                             │  ║
 * ║  │        parent="Theme.MorphKit.iOS">                                │  ║
 * ║  │        <item name="morphButtonStyle">                              │  ║
 * ║  │            @style/Widget.MyApp.Button.Promotion                    │  ║
 * ║  │        </item>                                                     │  ║
 * ║  │    </style>                                                        │  ║
 * ║  │                                                                    │  ║
 * ║  │  步骤 3：定义自定义 Style，覆写背景色、圆角、交互模式等               │  ║
 * ║  │                                                                    │  ║
 * ║  │    <style name="Widget.MyApp.Button.Promotion"                     │  ║
 * ║  │        parent="Widget.MorphKit.Button.iOS">                        │  ║
 * ║  │        <item name="morphCornerRadius">20dp</item>                  │  ║
 * ║  │        <item name="morphInteractionMode">material</item>           │  ║
 * ║  │        <item name="android:background">#FF6200EE</item>            │  ║
 * ║  │    </style>                                                        │  ║
 * ║  │                                                                    │  ║
 * ║  │  步骤 4：在 AndroidManifest.xml 中应用自定义 Theme                  │  ║
 * ║  │                                                                    │  ║
 * ║  │    android:theme="@style/Theme.MyApp.Promotion"                    │  ║
 * ║  │                                                                    │  ║
 * ║  └────────────────────────────────────────────────────────────────────┘  ║
 * ║                                                                          ║
 * ║  ┌────────────────────────────────────────────────────────────────────┐  ║
 * ║  │ 3. 可覆写的属性清单                                                │  ║
 * ║  ├────────────────────────────────────────────────────────────────────┤  ║
 * ║  │                                                                    │  ║
 * ║  │  Theme 级属性（在 Theme 中覆写，影响全局）：                        │  ║
 * ║  │  - morphButtonStyle     → 按钮默认样式                             │  ║
 * ║  │  - morphTextViewStyle   → 文本默认样式                             │  ║
 * ║  │  - morphEditTextStyle   → 输入框默认样式                           │  ║
 * ║  │  - morphCardStyle       → 卡片默认样式                             │  ║
 * ║  │                                                                    │  ║
 * ║  │  Style 级属性（在 Style 中覆写，影响单个控件类型）：                 │  ║
 * ║  │  - morphInteractionMode → ios(0) / material(1)                     │  ║
 * ║  │  - morphCornerRadius    → 圆角半径 (dimension)                     │  ║
 * ║  │                                                                    │  ║
 * ║  └────────────────────────────────────────────────────────────────────┘  ║
 * ║                                                                          ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * MorphKit 控件动态替换框架核心引擎。
 *
 * 作为全局单例提供统一的控件替换和属性修改入口，核心能力包括：
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

    private const val TAG = "MorphKit"

    /**
     * 当前配置，初始化后可用。
     *
     * M2 文档：`@Volatile` 保证跨线程可见性，但字段的安全访问依赖 [initialized] gate ——
     * 所有外部访问点（[createView]、[modifyView]）均在 `if (!initialized) return null`
     * 守卫之后才读取 [config]，确保不会读到未初始化的 lateinit 值。
     * 写入仅在 [init] 内、[initialized] 置 true 之前完成，且 [init] 由 [initGuard] CAS 保证单次执行。
     */
    @Volatile
    private lateinit var config: MorphConfig

    /**
     * 初始化守卫 — 防止重复调用 init()。
     *
     * 与 [initialized] 分离：`initGuard` 在 init() 入口 CAS 获取，
     * `initialized` 在 init() 全部完成后才设为 true。
     * 这消除了「initialized=true 但 config 未赋值」的竞态窗口。
     */
    private val initGuard = AtomicBoolean(false)

    /** 是否已完成初始化（所有配置、主题解析、注入器安装均就绪后才为 true） */
    @Volatile
    private var initialized: Boolean = false

    /** 已注册的内存压力响应者列表（线程安全） */
    private val memoryTrimmables = java.util.concurrent.CopyOnWriteArrayList<MemoryTrimmable>()

    /**
     * MorphKit 解析出的最终 Theme 资源 ID。
     *
     * 由 [MorphStyleResolver] 在 [init] 时根据 [StylePolicy] 计算得出，
     * 传递给 [MorphFactory2] 用于 [ContextThemeWrapper] 主题包装。
     *
     * - 非 0：表示需要通过 [ContextThemeWrapper] 注入此 Theme
     * - 为 0：表示宿主已完全接管，无需 MorphKit 注入主题
     *
     * @see MorphStyleResolver
     * @see MorphFactory2
     */
    @Volatile
    private var _finalThemeResId: Int = 0

    /**
     * 获取 MorphKit 解析出的最终 Theme 资源 ID。
     *
     * @return Theme 资源 ID，0 表示宿主已完全接管
     * @throws IllegalStateException 若 MorphKit 未初始化
     */
    @Throws(IllegalStateException::class)
    fun getFinalThemeResId(): Int {
        check(initialized) { "MorphKit 尚未初始化，请先调用 init() 或 autoInit()" }
        return _finalThemeResId
    }

    /**
     * 内部访问：直接获取 Theme 资源 ID，不校验初始化状态。
     * 供 [MorphInstaller] 在初始化流程中使用（此时 initialized 尚未设为 true）。
     */
    internal val finalThemeResId: Int get() = _finalThemeResId

    /**
     * 查询 MorphKit 是否已完成初始化。
     *
     * 适用于需要延迟初始化或条件性初始化的场景，
     * 避免在未初始化时调用 [createView] / [modifyView] 等方法。
     *
     * @return 已初始化返回 true，否则返回 false
     */
    fun isInitialized(): Boolean = initialized

    /**
     * 重置 MorphKit 全部状态（仅用于测试）。
     *
     * 将 initGuard、initialized、config、_finalThemeResId 恢复到初始值，
     * 允许单元测试在隔离环境中重复初始化 MorphKit。
     *
     * @throws IllegalStateException 若在非测试环境调用（internal 可见性限制为模块级）
     */
    @androidx.annotation.VisibleForTesting
    internal fun reset() {
        initialized = false
        initGuard.set(false)
        config = MorphConfig()
        _finalThemeResId = 0
        memoryTrimmables.clear()
        MorphStyleResolver.invalidateCache()
        MorphInstaller.reset()
    }

    /**
     * 注册内存压力响应者。
     *
     * 注册后，当系统发出内存压力回调时，[onTrimMemory] 会通知所有注册者。
     * widget 层通过此方法注册 Bitmap 对象池清理等逻辑，避免 core 层直接依赖 widget 层。
     *
     * @param trimmable 内存压力响应者
     */
    fun registerMemoryTrimmable(trimmable: MemoryTrimmable) {
        memoryTrimmables.addIfAbsent(trimmable)
    }

    /**
     * 注销内存压力响应者。
     *
     * @param trimmable 要注销的响应者
     */
    fun unregisterMemoryTrimmable(trimmable: MemoryTrimmable) {
        memoryTrimmables.remove(trimmable)
    }

    /**
     * 通知所有注册的内存压力响应者。
     *
     * 由 [MorphInitProvider] 在系统内存压力回调中调用。
     *
     * @param level trim level，参见 [android.content.ComponentCallbacks2] 常量
     */
    internal fun onTrimMemory(level: Int) {
        memoryTrimmables.forEach { it.onTrimMemory(level) }
    }

    /**
     * 初始化 MorphKit 引擎。
     *
     * 完成以下操作（原子性，不可部分执行）：
     * 1. 创建 [MorphConfig] 并执行 DSL 配置块，注册所有 replace / groupReplace / modify 规则
     * 2. 根据 [StylePolicy] 通过 [MorphStyleResolver] 解析最终 Theme，
     *    缓存到 [getFinalThemeResId]，并打印策略日志
     * 3. 调用 [MorphInstaller.install] 注册 Activity 生命周期回调，在每个 Activity 启动前
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
    @Throws(IllegalStateException::class)
    fun init(application: Application, block: MorphConfig.() -> Unit) {
        check(initGuard.compareAndSet(false, true)) { "MorphKit 已初始化，禁止重复调用 init()" }
        // ── 在 initGuard 保护下执行全部初始化，initialized 在最后才设为 true ──
        // 其他线程通过 initialized（Volatile）判断就绪状态，
        // 看到 initialized=true 时 config 和 _finalThemeResId 必定已赋值完毕
        try {
            val newConfig = MorphConfig().apply(block)
            config = newConfig

            // ── 根据 StylePolicy 解析最终 Theme ──
            _finalThemeResId = MorphStyleResolver.resolve(application, config.policy)

            // 自动安装全局 Factory2 注入，在每个 Activity 启动前拦截 LayoutInflater
            MorphInstaller.install(application)

            // 全部初始化完成后，才标记为已初始化（Volatile 写保证 happens-before）
            initialized = true
        } catch (t: Throwable) {
            // 初始化失败：回滚 initGuard，允许重试，避免陷入永久不可恢复状态
            initGuard.set(false)
            throw t
        }
    }

    /**
     * 自动初始化 MorphKit 引擎（使用默认配置）。
     *
     * 与 [init] 不同，此方法无需手动配置替换/修改规则，
     * 仅完成引擎的基础初始化（主题解析、注入器安装等），
     * 适用于仅需使用 MorphKit 主题能力而不需要控件替换的场景。
     *
     * [registerDefaults] 参数允许调用方注入控件注册逻辑（通常由 widget 层提供），
     * 保持 core 层对 widget 层的零依赖。
     *
     * 重复调用将抛出 [IllegalStateException]。
     *
     * @param application 应用实例，用于注册 ActivityLifecycleCallbacks
     * @param registerDefaults 可选的默认控件注册块，在 [MorphConfig] DSL 上执行
     * @throws IllegalStateException 若重复初始化
     */
    @Throws(IllegalStateException::class)
    fun autoInit(application: Application, registerDefaults: (MorphConfig.() -> Unit)? = null) {
        init(application) {
            registerDefaults?.invoke(this)
        }
    }

    /**
     * 追加控件注册规则到已初始化的配置。
     *
     * **解决双初始化冲突**：Android 系统保证 [MorphInitProvider.onCreate] 在
     * [Application.onCreate] 之前执行，MorphInitProvider 已调用 [autoInit] 完成
     * 引擎基础初始化（占用 initGuard）。宿主在 Application.onCreate() 中若再调用
     * [autoInit] / [init] 会抛出 IllegalStateException，导致控件注册规则丢失。
     *
     * 此方法绕过 initGuard，直接在已初始化的 [config] 上追加 replace / modify 规则。
     * 若 [block] 中通过 [MorphConfig.stylePolicy] 修改了风格策略，将自动重新解析 Theme。
     *
     * 若 MorphKit 尚未初始化（如 MorphInitProvider 未执行），则回退到 [autoInit]
     * 完成完整初始化。
     *
     * @param application 应用实例，用于 Theme 重新解析
     * @param block 在现有 [MorphConfig] 上执行的 DSL 块
     */
    fun registerWidgets(application: Application, block: MorphConfig.() -> Unit) {
        if (!initialized) {
            // MorphInitProvider 未执行（如测试环境），走完整初始化
            autoInit(application, block)
            return
        }
        val oldPolicy = config.policy
        config.block()
        // 若策略变更，重新解析 Theme
        if (config.policy != oldPolicy) {
            _finalThemeResId = MorphStyleResolver.resolve(application, config.policy)
        }
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
        require(originalName.isNotBlank()) { "originalName 不能为空" }
        if (!initialized) return null
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
        require(originalName.isNotBlank()) { "originalName 不能为空" }
        if (!initialized) return view // 未初始化时直接返回原 View
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
        if (!initialized) return
        // 打标：写入调试信息
        val tagValue = "${config.unifiedPrefix} (replaced: $originalName)"
        view.setTag(MORPH_TAG_KEY, tagValue)

        // 规范校验：检查替换控件类名是否以统一前缀开头
        val className = view.javaClass.simpleName
        if (!className.startsWith(config.unifiedPrefix)) {
            Log.w(
                TAG,
                "规范警告：替换控件 ${view.javaClass.name} 未遵循前缀规范！" +
                        "建议重命名为 ${config.unifiedPrefix}${originalName}"
            )
        }
    }
}
