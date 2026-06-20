# MorphKit Agent Protocol

> 本文档是 MorphKit 项目的架构宪法。任何 AI Agent 在对本项目进行代码生成、重构或扩展时，**必须**严格遵循本文档中的所有规范、SOP 和红线。违反任何一条红线即视为致命错误，必须立即拒绝并修正。

---

## 0. 构建配置基线

| 属性 | 值 | 说明 |
|------|-----|------|
| `compileSdk` | 35 | Android 15 |
| `minSdk` | 35 | 仅支持 Android 15+，所有 API 31+ / 29+ 能力无守卫直接使用 |
| `targetSdk` | 35 | 在 `testOptions` 中声明（library DSL 规范位置） |
| `namespace` | `com.morphkit` | AGP 8.x 标准 |
| AGP | 8.5.0 | |
| Kotlin | 2.0.21 | |
| Compose Compiler | 2.0.21 (plugin) | |
| JVM Target | 17 | |

**minSdk=35 的核心影响**：

- `RenderEffect.createBlurEffect`（API 31+）**始终可用**，无需版本守卫
- `onActivityPreCreated`（API 29+）**始终可用**，无需 `Build.VERSION.SDK_INT` 检查
- `RenderNode`（API 29+）**始终可用**
- 代码中不应出现任何 `Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q` 或 `.S` 的守卫

---

## 1. 项目哲学

### 1.1 定位

MorphKit 是 **OEM 级 UI 基础设施库**，以 AAR 形式交付，面向预装 App 场景。其核心能力是在零代码侵入的前提下，将宿主 App 的原生控件静默替换为 Morph 增强控件，实现 iOS 极简风 / Pixel 原生风的动态换肤。

### 1.2 架构核心：引擎、皮肤与控件分层

```
┌────────────────────────────────────────────────────────────────────┐
│  com.morphkit.core — 引擎层                                        │
│  MorphKit → MorphFactory2 → MorphInstaller → MorphConfig          │
│  MorphInitProvider → MorphClickListener → InteractionMode         │
├────────────────────────────────────────────────────────────────────┤
│  com.morphkit.theme — 皮肤层                                       │
│  MorphTokens (嵌套 object 分层) → MorphColors / MorphShape         │
│  MorphTypography → MorphStyleResolver → MorphTheme → MorphKitIOSConfig│
│  compose/ → MorphComposeTheme + MorphButton (Compose)             │
├──────────────────────────┬─────────────────────────────────────────┤
│  com.morphkit.widget     │  com.morphkit.internal                  │
│  .button  → MorphButton  │  ReflectionHelper (反射工具)            │
│           → MorphRadio...│                                         │
│  .text    → MorphText... │                                         │
│           → MorphEdit... │                                         │
│  .container→ MorphCard..│                                         │
│            → BackdropBlurHelper (internal)                         │
│  .selection→ MorphCheck.│                                         │
├──────────────────────────┴─────────────────────────────────────────┤
│     iOS Skin (InteractionMode.IOS)  │  Pixel Skin (.MATERIAL)      │
│  Theme.MorphKit.iOS                 │  Theme.MorphKit.Pixel        │
│  StateListAnimator                  │  Ripple + M3 defaults        │
└─────────────────────────────────────┴──────────────────────────────┘
```

- **引擎层**（`com.morphkit.core`）：负责 Hook 注入、控件替换、属性修改，与皮肤无关。包含 `InteractionMode` 枚举（`IOS` / `MATERIAL`）。
- **皮肤层**（`com.morphkit.theme`）：风格解析、设计 Token、Context 包装、Compose 主题。
- **控件层**（`com.morphkit.widget.*`）：按控件类型分子包（button/text/container/selection）。
- **内部层**（`com.morphkit.internal`）：不对外暴露的反射工具类等。`BackdropBlurHelper` 作为 `internal object` 存在于 `widget.container` 包中。
- **皮肤资源**（`themes.xml`, `styles.xml`, `StateListAnimator`）：通过 `morphInteractionMode` 枚举分发交互行为，引擎层不硬编码任何视觉逻辑。

### 1.3 三大核心原则

| 原则 | 含义 | 实现手段 |
|------|------|----------|
| **绝不白屏** | 任何异常不得导致宿主 Activity 白屏 | `MorphFactory2.onCreateView` 全链路 try-catch，异常时降级到 `originalFactory` |
| **极度克制** | 尊重宿主已有属性，绝不覆盖业务方显式设置的值 | `hasCustomBackground` 检测，`obtainStyledAttributes` 只读不写 |
| **系统一致** | View 体系与 Compose 体系的颜色真相源统一 | Pixel 模式下 Compose 通过 `MaterialColors.getColor(context, ...)` 读取 Theme，而非硬编码 Token |

---

## 2. 新增控件标准作业程序 (SOP)

当需要新增一个控件（例如 `MorphSeekBar`）时，Agent **必须**按以下 7 步严格执行，**缺一不可**。每一步完成后必须在提交信息中注明已完成的步骤编号。

### Step 1: 属性定义

在 `src/main/res/values/attrs.xml` 中声明：

```xml
<!-- 必须声明 morph[Widget]Style 作为 defStyleAttr 的指向目标 -->
<attr name="morphSeekBarStyle" format="reference" />

<!-- morphInteractionMode 是全局枚举，已存在，无需重复声明 -->
```

**检查点**：`morph[Widget]Style` 的 `format` 必须为 `reference`，不能是其他类型。

### Step 2: 构造函数防御

控件的第三个构造函数参数 `defStyleAttr` **必须**指向 `R.attr.morph[Widget]Style`：

```kotlin
class MorphSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphSeekBarStyle  // ← 绝不能硬编码 AppCompat 或 Material 的属性
) : View(context, attrs, defStyleAttr) {
```

**禁止**：
- `defStyleAttr = androidx.appcompat.R.attr.seekBarStyle`
- `defStyleAttr = com.google.android.material.R.attr.sliderStyle`

### Step 3: 双套 Style 铺设

在 `src/main/res/values/styles.xml` 中必须同时实现 iOS 和 Pixel 两套 Style：

```xml
<!-- iOS 风格 -->
<style name="Widget.MorphKit.SeekBar.iOS" parent="">
    <item name="morphInteractionMode">0</item>  <!-- ios=0 -->
    <!-- iOS 风格的视觉属性 -->
</style>

<!-- Pixel 风格 -->
<style name="Widget.MorphKit.SeekBar.Pixel" parent="">
    <item name="morphInteractionMode">1</item>  <!-- material=1 -->
    <!-- Pixel 风格的视觉属性 -->
</style>
```

在 `src/main/res/values/themes.xml` 中分配：

```xml
<style name="Theme.MorphKit.iOS" parent="">
    <!-- ... 已有属性 ... -->
    <item name="morphSeekBarStyle">@style/Widget.MorphKit.SeekBar.iOS</item>
</style>

<style name="Theme.MorphKit.Pixel" parent="">
    <!-- ... 已有属性 ... -->
    <item name="morphSeekBarStyle">@style/Widget.MorphKit.SeekBar.Pixel</item>
</style>
```

### Step 4: 交互模式分发

在 `init` 块中读取 `morphInteractionMode`，使用 `InteractionMode` 枚举分发交互行为：

```kotlin
private val interactionMode: InteractionMode

init {
    val a = context.obtainStyledAttributes(attrs, R.styleable.MorphSeekBar, defStyleAttr, 0)
    try {
        val modeValue = a.getInt(R.styleable.MorphSeekBar_morphInteractionMode, 0)
        interactionMode = if (modeValue == 1) InteractionMode.MATERIAL else InteractionMode.IOS
    } finally {
        a.recycle()  // ← 必须 try-finally，防止 TypedArray 泄漏
    }

    when (interactionMode) {
        InteractionMode.IOS -> applyIOSInteraction()
        InteractionMode.MATERIAL -> { /* 保留 M3 默认 */ }
    }
}

private fun applyIOSInteraction() {
    // 1. 剥离 Ripple
    // 2. 必须提供 StateListAnimator 补偿（包含 state_pressed 和 state_focused）
    stateListAnimator = AnimatorInflater.loadStateListAnimator(
        context, R.animator.morph_widget_seekbar_ios_state
    )
}
```

**关键**：iOS 模式下剥离 Ripple 后，**必须**通过 `StateListAnimator` 提供无障碍焦点补偿。`StateListAnimator` 必须同时包含：
- `state_pressed` → 按压反馈（如 alpha 变化）
- `state_focused` → 焦点补偿（如 translationZ 提升）
- **默认态** → 用 `<set>` 同时恢复 `alpha` **和** `translationZ`，防止状态切换后属性残留

参考现有实现：`src/main/res/animator/morph_widget_button_ios_state.xml`

### Step 5: 防抖包装

若控件有**离散点击行为**（如 RadioButton、CheckBox 的选中切换），**必须**使用 `MorphClickListener` 包装：

```kotlin
// 正确 — 离散点击控件
setOnClickListener(MorphClickListener { v ->
    handleClick(v)
})

// 禁止：裸 setOnClickListener
setOnClickListener { handleClick(it) }
```

`MorphClickListener` 默认冷却时间 300ms，可自定义：

```kotlin
MorphClickListener(debounceInterval = 500L) { v -> ... }
```

**例外**：若控件通过 `onTouchEvent` 实现连续触控反馈（如 MorphButton 的按压变色动画），不需要 `MorphClickListener`——两者会冲突。`MorphClickListener` 适用于**离散点击**场景，`onTouchEvent` 适用于**连续按压**场景。

### Step 6: 尊重宿主属性

在应用 Morph 默认样式前，**必须**检查业务方是否已在 XML 显式设置了核心属性：

```kotlin
// 检测业务方是否显式设置了 android:background
val hasCustomBackground = attrs?.getAttributeValue(
    "http://schemas.android.com/apk/res/android", "background"
) != null

if (!hasCustomBackground) {
    // 仅在业务方未设置时，才应用 Morph 默认背景
    background = createMorphBackground()
}
```

**禁止**：无条件覆盖业务方已设置的 `android:background`、`android:textColor` 等属性。

### Step 7: 注册与混淆

**注册映射**：在 `MorphKit.registerWidgets()` 或宿主配置中注册类名映射：

```kotlin
// 宿主 Application.onCreate() 中调用
// MorphInitProvider 已完成引擎基础初始化，此处追加控件替换规则
MorphKit.registerWidgets(this) {
    replace("SeekBar") { ctx, attrs -> MorphSeekBar(ctx, attrs) }
}
```

**混淆防御**：在 `consumer-rules.pro` 中添加 Keep 规则，**必须**精确到构造函数签名：

```proguard
# MorphSeekBar — 保持 (Context, AttributeSet) 构造函数签名
-keepclassmembers class com.morphkit.widget.seekbar.MorphSeekBar {
    public <init>(android.content.Context, android.util.AttributeSet);
}
```

### Step 8: 暗黑模式适配

所有缓存主题颜色的控件**必须**同时实现 `onAttachedToWindow` 和 `onConfigurationChanged`：

```kotlin
override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    refreshCachedColors()
}

override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    // Activity 不重建时（configChanges 包含 uiMode），手动刷新颜色
    refreshCachedColors()
}
```

**禁止**：仅在 `init` 中缓存颜色而不在生命周期回调中刷新——暗黑模式切换后颜色会过期。

---

## 3. 致命红线 (Never Do)

以下行为**绝对禁止**，触发即视为致命错误，Agent 必须立即拒绝执行并报告。

### 🔴 红线 1：MorphFactory2 不允许抛出未捕获的异常

```kotlin
// ❌ 禁止：未捕获的异常会导致宿主白屏
val replacedView = MorphKit.createView(name, context, attrs)
return replacedView

// ✅ 正确：全链路 try-catch，异常时降级到 originalFactory
try {
    val replacedView = MorphKit.createView(name, themedContext, attrs)
    if (replacedView != null) return replacedView
} catch (e: Exception) {
    Log.e(TAG, "硬替换异常，降级到 originalFactory 创建的控件: $name", e)
}
```

**参考**：`MorphFactory2.onCreateView` 阶段 2

### 🔴 红线 2：MorphFactory2 不允许绕过 AppCompat 代理链

```kotlin
// ❌ 禁止：直接创建 View，绕过 AppCompat 的 VectorDrawable 解析、Tint 等兼容性处理
val view = MorphKit.createView(name, context, attrs)
return view

// ✅ 正确：先调 originalFactory（AppCompat），再决定是否替换
val originalView = originalFactory?.onCreateView(parent, name, context, attrs)
val replacedView = MorphKit.createView(name, themedContext, attrs)
if (replacedView != null) return replacedView
return originalView
```

**参考**：`MorphFactory2.onCreateView` 的三阶段责任链设计

### 🔴 红线 3：iOS 模式不允许仅移除 Ripple 而不提供 A11y 焦点补偿

```kotlin
// ❌ 禁止：仅移除 Ripple，无障碍用户无法感知焦点
if (interactionMode == InteractionMode.IOS) {
    ripple = null  // 移除了视觉反馈，但没有任何补偿
}

// ✅ 正确：移除 Ripple 后，通过 StateListAnimator 提供焦点补偿
if (interactionMode == InteractionMode.IOS) {
    stateListAnimator = AnimatorInflater.loadStateListAnimator(
        context, R.animator.morph_widget_button_ios_state
    )
    // StateListAnimator 包含 state_focused → translationZ 提升
}
```

**参考**：`src/main/res/animator/morph_widget_button_ios_state.xml`

### 🔴 红线 4：MorphStyleResolver 不允许无 try-catch 读取系统设置

```kotlin
// ❌ 禁止：部分定制 ROM 限制 Settings.System 读取权限
val oemStyle = Settings.System.getInt(context.contentResolver, "oem_ui_style", 0)

// ✅ 正确：必须 try-catch，异常时安全回退到默认值
val oemStyle = try {
    Settings.System.getInt(context.contentResolver, OEM_UI_STYLE_KEY, OEM_STYLE_DEFAULT)
} catch (e: Exception) {
    Log.d(TAG, "读取 OEM 系统设置异常，使用默认值", e)
    OEM_STYLE_DEFAULT
}
```

**参考**：`MorphStyleResolver.readOemStyle()`

### 🔴 红线 5：MorphInstaller 不允许在多进程环境下重复执行反射注入

```kotlin
// ❌ 禁止：多进程环境下 ContentProvider 会在每个进程触发 onCreate
// 直接执行反射注入会导致重复注册 ActivityLifecycleCallbacks

// ✅ 正确：通过进程名检测 + AtomicBoolean 防重入
private val installed = AtomicBoolean(false)

fun install(app: Application) {
    if (!installed.compareAndSet(false, true)) return  // 防重入
    if (isUnsupportedProcess(app)) return  // 多进程保护
    app.registerActivityLifecycleCallbacks(...)
}
```

**参考**：`MorphInitProvider.onCreate()` + `MorphInstaller.install()`

### 🔴 红线 6：Bitmap 回收前必须解除 Drawable 引用

```kotlin
// ❌ 禁止：Bitmap 已回收但 BitmapDrawable 仍持有引用，下一帧绘制时崩溃
(iv.drawable as? BitmapDrawable)?.bitmap?.recycle()
iv.setImageDrawable(null)  // 太晚！回收后 Drawable 仍可能触发绘制

// ✅ 正确：先解除引用，再回收
val oldBitmap = (iv.drawable as? BitmapDrawable)?.bitmap
iv.setImageDrawable(null)  // 先解除引用
oldBitmap?.recycle()       // 再安全回收
```

**参考**：`MorphCardView.removeBlurBackgroundView()` / `updateBlurBackground()`

---

## 4. 代码风格与命名约定

### 4.1 类名前缀

所有 Morph 控件类名**必须**以 `Morph` 前缀开头：

| 正确 | 错误 |
|------|------|
| `MorphButton` | `iOSButton` |
| `MorphSeekBar` | `MorphSeekBarCompat` |
| `MorphTextView` | `StyledTextView` |

### 4.2 Theme 命名规范

```
Theme.MorphKit.iOS          ← iOS 极简风主题
Theme.MorphKit.Pixel        ← Pixel 原生风主题
Widget.MorphKit.Button.iOS  ← iOS 风格控件样式
Widget.MorphKit.Button.Pixel ← Pixel 风格控件样式
```

**禁止**：`Theme.MorphKit`（无皮肤后缀）、`Widget.MorphKit.Button`（无皮肤后缀）

### 4.3 属性命名规范

```
morph[Widget]Style          ← defStyleAttr 指向目标（reference 类型）
morphInteractionMode        ← 交互模式枚举（0=ios, 1=material）
```

### 4.4 枚举使用规范

`InteractionMode` 是 `com.morphkit.core` 中的 Kotlin 枚举：

```kotlin
enum class InteractionMode {
    IOS, MATERIAL
}
```

- 控件内部使用枚举值（`InteractionMode.IOS` / `InteractionMode.MATERIAL`）进行分支判断
- XML 中使用整数（`morphInteractionMode=0` / `morphInteractionMode=1`）
- 从 XML 读取时映射规则：`getInt(..., 0)` → `if (value == 1) MATERIAL else IOS`

`StylePolicy` 是 `MorphConfig.kt` 中的嵌套枚举：

```kotlin
enum class StylePolicy { AUTO, IOS, PIXEL
    companion object {
        @Deprecated("使用 IOS 代替") val FORCE_IOS = IOS
        @Deprecated("使用 PIXEL 代替") val FORCE_PIXEL = PIXEL
    }
}
```

### 4.5 KDoc 注释要求

所有核心防御逻辑**必须**添加 KDoc 注释，说明**为何如此设计**（而非做了什么）：

```kotlin
// ❌ 禁止：仅描述行为
try {
    val replacedView = MorphKit.createView(...)
} catch (e: Exception) {
    Log.e(TAG, "异常", e)
}

// ✅ 正确：说明设计意图
try {
    val replacedView = MorphKit.createView(name, themedContext, attrs)
    if (replacedView != null) return replacedView
} catch (e: Exception) {
    // 降级保护：防止自定义控件崩溃导致宿主白屏
    // 必须吞掉异常并回退到 originalFactory 创建的安全 View
    Log.e(TAG, "硬替换异常，降级到 originalFactory 创建的控件: $name", e)
}
```

### 4.6 TypedArray 生命周期

所有 `obtainStyledAttributes` 调用**必须**使用 `try-finally` 确保 `recycle()`：

```kotlin
val a = context.obtainStyledAttributes(attrs, R.styleable.MorphButton, defStyleAttr, 0)
try {
    // 读取属性
} finally {
    a.recycle()  // ← 绝不能遗漏
}
```

### 4.7 颜色获取方式

| 模式 | View 体系 | Compose 体系 |
|------|-----------|-------------|
| iOS | `MorphTheme.morphColor*(context)` → `MaterialColors.getColor()` | `MorphTokens.*` 静态常量 |
| Pixel | `MorphTheme.morphColor*(context)` → `MaterialColors.getColor()` | `pixelFromContext(context)` → `MaterialColors.getColor()` |

**禁止**：在 Pixel 模式下硬编码颜色值。Pixel 模式的颜色真相源是宿主 Theme，不是 Token。

### 4.8 线程安全

- `@Volatile` 注解**必须**放在类体属性声明上，不能放在构造函数参数上（后者不会生成 `@Volatile` 字段）
- 防抖/原子操作使用 `AtomicLong` + CAS 循环，不使用 `@Volatile` + check-then-act
- 时间源使用 `SystemClock.elapsedRealtime()`（不受系统时钟调整影响），不使用 `System.currentTimeMillis()`
- 单次初始化的缓存引用优先使用 `by lazy` 委托，天然保证线程安全且无 check-then-act 竞态（参考 `MorphStyleResolver.dynamicColorMethod`）
- 并发容器使用 `ConcurrentHashMap`，不使用 `Collections.synchronizedMap`（参考 `MorphConfig._replaceMap`）

### 4.9 内存管理

- Bitmap 对象池：`BackdropBlurHelper.bitmapPool` 缓存最多 3 个 Bitmap，`onTrimMemory` 时清空
- IntArray 双级缓存：`BackdropBlurHelper.pixelBuffer` 主/次交替，支持两种尺寸场景
- BitmapDrawable 复用：`MorphCardView.updateBlurBackground` 同尺寸直接替换像素，复用 BitmapDrawable 对象
- `onDetachedFromWindow` 必须清理 Bitmap、取消动画、置空引用

---

## 5. 关键文件索引

### 5.1 包结构映射

| 包名 | 职责 | 包含类 |
|------|------|--------|
| `com.morphkit.core` | 引擎、注入、降级、防抖、枚举 | `MorphKit`, `MorphConfig`, `MorphFactory2`, `MorphInstaller`, `MorphInitProvider`, `MorphClickListener`, `InteractionMode` |
| `com.morphkit.theme` | 风格解析、Token、Context 包装 | `MorphStyleResolver`, `MorphTheme`, `MorphTokens`（嵌套 object 分层）, `MorphColors`, `MorphShape`, `MorphTypography`, `MorphKitIOSConfig` |
| `com.morphkit.theme.compose` | Compose 主题 | `MorphComposeTheme`（含 `MorphColorPalette` 37 字段）, `MorphButton` (Compose) |
| `com.morphkit.widget.button` | 按钮类控件 | `MorphButton`, `MorphRadioButton` |
| `com.morphkit.widget.text` | 文本类控件 | `MorphTextView`, `MorphEditText` |
| `com.morphkit.widget.container` | 容器类控件 | `MorphCardView`, `BackdropBlurHelper`（`internal`） |
| `com.morphkit.widget.selection` | 选择类控件 | `MorphCheckBox` |
| `com.morphkit.internal` | 内部工具（不对外暴露） | `ReflectionHelper` |

### 5.2 文件职责

| 文件 | 包 | 关键模式 |
|------|-----|----------|
| `MorphFactory2.kt` | `core` | 三阶段：先 originalFactory → 再替换 → 最后软修改 |
| `MorphInstaller.kt` | `core` | `onActivityPreCreated` 注入 + `onActivityCreated` 补充 AppCompat delegate |
| `MorphKit.kt` | `core` | `createView` / `modifyView` 前置 `initialized` 检查 + try-catch |
| `MorphConfig.kt` | `core` | `StylePolicy` 枚举 + `replace()` / `groupReplace()` / `modify()` |
| `MorphClickListener.kt` | `core` | `AtomicLong` CAS 防抖 + `SystemClock.elapsedRealtime()` + 可注入时间源 |
| `InteractionMode.kt` | `core` | `IOS` / `MATERIAL` 枚举，控件交互模式分发基础 |
| `MorphInitProvider.kt` | `core` | ContentProvider 零代码初始化 + 多进程保护 |
| `MorphStyleResolver.kt` | `theme` | OEM 设置 > StylePolicy > AUTO 检测 Dynamic Color；`dynamicColorMethod` 使用 `lazy` 委托保证线程安全 |
| `MorphTheme.kt` | `theme` | 语义色方法（`morphColorPrimary` 等）+ 形状 + 排版，全部走 `MaterialColors.getColor()`；废弃方法标记 `DeprecationLevel.ERROR` |
| `MorphColors.kt` | `theme` | 颜色工具：`overlayColor` / `adjustAlpha` / `blendColor` / `createColorStateList` / `isDarkMode` |
| `MorphShape.kt` | `theme` | 形状工具：`cornerSmall` / `cornerMedium` / `cornerLarge` / `cornerFull` |
| `MorphTypography.kt` | `theme` | 排版工具：`typography` / `buttonTypography` / `captionTypography` 等 |
| `MorphTokens.kt` | `theme` | 静态设计 Token：7 个嵌套 object（Colors/Shapes/Typography/Interaction/Spacing/Elevation/Motion），外层委托属性 + `@get:ColorInt` |
| `MorphKitIOSConfig.kt` | `theme` | `Application.initIOSStyle()` 扩展函数，预置控件替换规则 |
| `MorphButton.kt` | `widget.button` | 双模式分发 + `hasCustomBackground` + `StateListAnimator` + `onTouchEvent` 按压动画 |
| `MorphRadioButton.kt` | `widget.button` | 自定义圆形指示器 + `setPadding` 防文字重叠 + `MorphClickListener` 防抖 |
| `MorphCheckBox.kt` | `widget.selection` | 自定义圆角方形勾选 + `setPadding` 防文字重叠 + `MorphClickListener` 防抖 |
| `MorphTextView.kt` | `widget.text` | textStyle 重映射（NORMAL→MEDIUM 补偿）+ 次级/三级文字色 |
| `MorphEditText.kt` | `widget.text` | `Style.BARE` / `Style.SEARCH` + 焦点背景反馈 |
| `MorphCardView.kt` | `widget.container` | 极简白卡片 / 毛玻璃模式 + `BackdropBlurHelper` + Bitmap 安全回收 |
| `BackdropBlurHelper.kt` | `widget.container` | `internal object`，`RenderEffect` GPU 高斯模糊 + 软件 Stack Blur 降级；Bitmap 对象池 + IntArray 双级缓存 |
| `ReflectionHelper.kt` | `internal` | 安全反射字段访问与写入，供 MorphInstaller 使用 |
| `consumer-rules.pro` | — | R8 混淆防御，构造函数签名级 `-keep` + LayoutInflater 字段反射 keep |
| `attrs.xml` | — | 自定义属性 `morph*Style` + `morphInteractionMode` |
| `themes.xml` | — | 主题定义，透明 overlay（`parent=""`），仅声明 `morph*Style` 属性分配 |
| `styles.xml` | — | 控件样式，iOS / Pixel 双套，通过 `morphInteractionMode` 分发 |
| `ids.xml` | — | `morph_view_tag` ID，用于 `View.setTag()` 调试打标 |

### 5.3 Animator XML 规范

| 文件 | 使用控件 | 状态 |
|------|----------|------|
| `morph_widget_button_ios_state.xml` | MorphButton | focused→translationZ 4dp, default→`<set>` 恢复 translationZ（按压反馈由 pressOverlay 接管，无 pressed 态） |
| `morph_widget_selection_ios_state.xml` | MorphRadioButton, MorphCheckBox | pressed→alpha 0.7, focused→translationZ 2dp, default→`<set>` 恢复 alpha+translationZ |

**关键**：默认态必须用 `<set>` 同时恢复 `alpha` **和** `translationZ`（selection 控件），或仅恢复 `translationZ`（button 控件，按压反馈由 pressOverlay 接管），否则从 focused 退出后 `translationZ` 会残留在非零值。

---

## 6. 测试合规

### 6.1 测试目录

所有测试代码**必须**与 `src/main` 保持完全一致的包路径：

| 测试文件 | 包路径 | 源码对应 |
|---------|--------|---------|
| `MorphKitTest.kt` | `src/test/kotlin/com/morphkit/core/` | `core.MorphKit` |
| `ConcurrencyTest.kt` | `src/test/kotlin/com/morphkit/core/` | `core.MorphKit` / `MorphClickListener` / `MorphConfig` 并发安全 |
| `MorphFactory2ChainTest.kt` | `src/test/kotlin/com/morphkit/core/` | `core.MorphFactory2` 责任链 |
| `MorphFactory2Test.kt` | `src/test/java/com/morphkit/core/` | `core.MorphFactory2` |
| `MorphConfigTest.kt` | `src/test/java/com/morphkit/core/` | `core.MorphConfig` |
| `MorphInstallerTest.kt` | `src/test/java/com/morphkit/core/` | `core.MorphInstaller` |
| `MorphInitProviderTest.kt` | `src/test/java/com/morphkit/core/` | `core.MorphInitProvider` |
| `MorphClickListenerTest.kt` | `src/test/java/com/morphkit/core/` | `core.MorphClickListener` |
| `MorphStyleResolverTest.kt` | `src/test/java/com/morphkit/theme/` | `theme.MorphStyleResolver` |
| `MorphThemeTest.kt` | `src/test/java/com/morphkit/theme/` | `theme.MorphTheme` |
| `MorphThemeDpTest.kt` | `src/test/kotlin/com/morphkit/theme/` | `theme.MorphTheme` dp 扩展 |
| `MorphColorsTest.kt` | `src/test/kotlin/com/morphkit/theme/` | `theme.MorphColors` isDarkMode |
| `MorphComposeThemeTest.kt` | `src/test/java/com/morphkit/theme/compose/` | `theme.compose.MorphComposeTheme` |
| `MorphButtonTest.kt` | `src/test/java/com/morphkit/widget/button/` | `widget.button.MorphButton` |
| `MorphRadioButtonTest.kt` | `src/test/java/com/morphkit/widget/button/` | `widget.button.MorphRadioButton` |
| `MorphTextViewTest.kt` | `src/test/java/com/morphkit/widget/text/` | `widget.text.MorphTextView` |
| `MorphEditTextTest.kt` | `src/test/java/com/morphkit/widget/text/` | `widget.text.MorphEditText` |
| `MorphCardViewTest.kt` | `src/test/java/com/morphkit/widget/container/` | `widget.container.MorphCardView` |
| `BackdropBlurHelperTest.kt` | `src/test/java/com/morphkit/widget/container/` | `widget.container.BackdropBlurHelper` |
| `MorphCheckBoxTest.kt` | `src/test/java/com/morphkit/widget/selection/` | `widget.selection.MorphCheckBox` |
| `MorphCheckBoxBehaviorTest.kt` | `src/test/java/com/morphkit/widget/selection/` | `widget.selection.MorphCheckBox` 行为 |
| `MorphButtonBehaviorTest.kt` | `src/test/java/com/morphkit/widget/button/` | `widget.button.MorphButton` 行为 |
| `MorphRadioButtonBehaviorTest.kt` | `src/test/java/com/morphkit/widget/button/` | `widget.button.MorphRadioButton` 行为 |
| `MorphTextViewBehaviorTest.kt` | `src/test/java/com/morphkit/widget/text/` | `widget.text.MorphTextView` 行为 |
| `MorphEditTextBehaviorTest.kt` | `src/test/java/com/morphkit/widget/text/` | `widget.text.MorphEditText` 行为 |
| `MorphCardViewBehaviorTest.kt` | `src/test/java/com/morphkit/widget/container/` | `widget.container.MorphCardView` 行为 |
| `BackdropBlurHelperBehaviorTest.kt` | `src/test/java/com/morphkit/widget/container/` | `widget.container.BackdropBlurHelper` 行为 |
| `MorphComposeThemeBehaviorTest.kt` | `src/test/java/com/morphkit/theme/compose/` | `theme.compose.MorphComposeTheme` 行为 |
| `MorphButtonComposeTest.kt` | `src/test/java/com/morphkit/theme/compose/` | `theme.compose.MorphButton` |
| `MorphTypographyTest.kt` | `src/test/kotlin/com/morphkit/theme/` | `theme.MorphTypography` |
| `MorphShapeTest.kt` | `src/test/kotlin/com/morphkit/theme/` | `theme.MorphShape` |
| `MorphKitIOSConfigTest.kt` | `src/test/java/com/morphkit/theme/` | `theme.MorphKitIOSConfig` 扩展 |
| `ReflectionHelperTest.kt` | `src/test/java/com/morphkit/internal/` | `internal.ReflectionHelper` |

测试为本地 JVM 测试，不依赖真机或模拟器。

### 6.2 测试框架

| 框架 | 版本 | 用途 |
|------|------|------|
| JUnit 4 | 4.13.2 | 测试运行器 |
| JUnit Jupiter | 5.10.2 | JUnit 5 引擎（`useJUnitPlatform()`） |
| JUnit Vintage | 5.10.2 | JUnit 4 兼容层 |
| MockK | 1.13.5 | Kotlin Mock 库（`mockkStatic(Log::class)` 避免 "not mocked" 错误） |
| Truth | 1.1.5 | 可读性断言 |
| Robolectric | 4.10.3 | Android 框架模拟 |
| kotlin-test-junit5 | 2.0.21 | Kotlin 测试互操作 |

**测试中 MorphClickListener 时间源**：`MorphClickListener` 提供 `internal constructor(debounceInterval, timeSource, block)` 用于测试注入，避免 `SystemClock.elapsedRealtime()` 在 JVM 测试中返回 0 的问题。

### 6.3 必测场景

每个 Morph 控件**必须**覆盖以下测试场景：

| 场景 | 验证点 |
|------|--------|
| 崩溃降级 | 控件构造异常时，`MorphFactory2.onCreateView` 不抛异常，降级到 `originalFactory` |
| 责任链完整性 | 替换成功时，`originalFactory` 仍被调用（保证 AppCompat 着色） |
| 防抖有效性 | 300ms 内连续点击，业务 Listener 仅触发 1 次 |
| 暗黑模式刷新 | `onConfigurationChanged` 后颜色正确更新 |
| Bitmap 安全回收 | 毛玻璃模式切换时不产生已回收 Bitmap 的绘制崩溃 |

### 6.4 CI/CD 合规

MorphKit 的合并请求（PR）**必须**通过所有 `src/test` 下的测试用例，特别是降级测试，否则**严禁**合入主分支。

---

## 7. AppCompat 共存协议

MorphKit 通过**两阶段注入**与 AppCompat 共存，这是整个系统的基础，任何修改不得破坏此协议：

```
Activity 生命周期
  │
  ├─ onActivityPreCreated()       ← minSdk=35 保证此回调始终可用
  │    └─ 注入 MorphFactory2（此时 AppCompat delegate 尚未安装 Factory2）
  │         MorphFactory2.originalFactory = null
  │
  └─ onActivityCreated()
       └─ 补充 AppCompat delegate 作为 originalFactory
            MorphFactory2.updateOriginalFactory(appCompatDelegate)
            → MorphFactory2.originalFactory = AppCompat delegate
```

**关键**：`MorphFactory2` 在 `onActivityPreCreated` 时 `originalFactory` 为 null，此时创建的 View 可能缺少 AppCompat 着色。这是**有意为之**的妥协——在 `onActivityCreated` 补充 AppCompat delegate 后，后续所有 View 创建都会先经过 AppCompat 处理。

**禁止**：在 `onActivityPreCreated` 中尝试获取 AppCompat delegate（此时它尚未安装 Factory2）。

---

## 8. Compose 隔离协议

MorphKit 的 Compose 模块对宿主是**可选依赖**：

```kotlin
// build.gradle.kts
compileOnly(platform("androidx.compose:compose-bom:2025.05.01"))
compileOnly("androidx.compose.material3:material3")
// ... 其他 Compose 依赖均为 compileOnly
```

- 不使用 Compose 的宿主 App 不会打包 Compose 运行时（约 2-3MB）
- 使用 Compose 的宿主 App 需自行声明 Compose 依赖
- Compose 主题通过 `CompositionLocal` 传播，与 View 体系的 `ContextThemeWrapper` 独立运行
- Pixel 模式下 Compose 的颜色**必须**从 Context Theme 读取（`MaterialColors.getColor()`），而非硬编码 Token
- `MorphColorPalette` 是 `@Immutable data class`，包含 37 个语义色字段，覆盖完整 M3 ColorScheme 角色 + `success` / `warning` 扩展色
- `LocalMorphInteractionMode` 默认 `InteractionMode.IOS`，`LocalMorphStylePolicy` 默认 `StylePolicy.AUTO`

---

## 9. 模糊策略（BackdropBlurHelper）

`BackdropBlurHelper` 是 `internal object`，提供毛玻璃卡片的 behind-blur 能力：

| 场景 | 技术 | 质量 |
|------|------|------|
| 默认（硬件加速 Canvas） | `RenderEffect.createBlurEffect` + `RenderNode` | GPU 高斯 |
| 降级（软件 Canvas） | Stack Blur（水平+垂直两遍均值滤波） | 近似高斯 |

- minSdk=35 保证 `RenderEffect`（API 31+）始终可用，无需版本分支
- `blurWithRenderNode` 方法已移除（API 29-30 专属，minSdk=35 后不再需要）
- `captureParentArea()` 临时隐藏目标 View 后截取父容器像素，确保模糊内容不包含卡片自身
- Bitmap 生命周期严格遵守**红线 6**：先解除 Drawable 引用，再回收
- **内存优化**：`bitmapPool` 缓存最多 3 个 Bitmap，`pixelBuffer` 双级缓存（主/次交替）复用 IntArray，`onTrimMemory` 时清空 Bitmap 池

---

*本文档最后更新：2026-06-12 | 基于 A+ 级优化全量更新*
