# MorphKit Agent Protocol

> 本文档是 MorphKit 项目的架构宪法。任何 AI Agent 在对本项目进行代码生成、重构或扩展时，**必须**严格遵循本文档中的所有规范、SOP 和红线。违反任何一条红线即视为致命错误，必须立即拒绝并修正。

---

## 1. 项目哲学

### 1.1 定位

MorphKit 是 **OEM 级 UI 基础设施库**，以 AAR 形式交付，面向预装 App 场景。其核心能力是在零代码侵入的前提下，将宿主 App 的原生控件静默替换为 Morph 增强控件，实现 iOS 极简风 / Pixel 原生风的动态换肤。

### 1.2 架构核心：引擎、皮肤与控件分层

```
┌────────────────────────────────────────────────────────────────────┐
│  com.morphkit.core — 引擎层                                        │
│  MorphKit → MorphFactory2 → MorphInstaller → MorphConfig          │
│  MorphInitProvider → MorphClickListener                            │
├────────────────────────────────────────────────────────────────────┤
│  com.morphkit.theme — 皮肤层                                       │
│  MorphStyleResolver → MorphTheme → MorphTokens → MorphKitIOSConfig│
│  compose/ → MorphComposeTheme + MorphButton (Compose)             │
├──────────────────────────┬─────────────────────────────────────────┤
│  com.morphkit.widget     │  com.morphkit.internal                  │
│  .button  → MorphButton  │  ReflectionHelper (反射工具)            │
│           → MorphRadio...│                                         │
│  .text    → MorphText... │                                         │
│           → MorphEdit... │                                         │
│  .container→ MorphCard..│                                         │
│  .selection→ MorphCheck.│                                         │
├──────────────────────────┴─────────────────────────────────────────┤
│     iOS Skin (morphInteractionMode=0)  │  Pixel Skin (mode=1)      │
│  Theme.MorphKit.iOS                    │  Theme.MorphKit.Pixel     │
│  StateListAnimator                     │  Ripple + M3 defaults     │
└────────────────────────────────────────┴───────────────────────────┘
```

- **引擎层**（`com.morphkit.core`）：负责 Hook 注入、控件替换、属性修改，与皮肤无关。
- **皮肤层**（`com.morphkit.theme`）：风格解析、设计 Token、Context 包装、Compose 主题。
- **控件层**（`com.morphkit.widget.*`）：按控件类型分子包（button/text/container/selection）。
- **内部层**（`com.morphkit.internal`）：不对外暴露的反射工具类等。
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

在 `init` 块中读取 `morphInteractionMode`，按模式分发交互行为：

```kotlin
init {
    val a = context.obtainStyledAttributes(attrs, R.styleable.MorphButton, defStyleAttr, 0)
    try {
        val interactionMode = a.getInt(R.styleable.MorphButton_morphInteractionMode, 0)
        when (interactionMode) {
            0 -> applyIOSInteraction()   // ios
            1 -> applyMaterialInteraction() // material
        }
    } finally {
        a.recycle()  // ← 必须 try-finally，防止 TypedArray 泄漏
    }
}

private fun applyIOSInteraction() {
    // 1. 剥离 Ripple
    // 2. 必须提供 StateListAnimator 补偿（包含 state_pressed 和 state_focused）
    stateListAnimator = AnimatorInflater.loadStateListAnimator(
        context, R.animator.morph_widget_seekbar_ios_state
    )
}

private fun applyMaterialInteraction() {
    // 保持默认 Ripple，不做任何修改
}
```

**关键**：iOS 模式下剥离 Ripple 后，**必须**通过 `StateListAnimator` 提供无障碍焦点补偿。`StateListAnimator` 必须同时包含：
- `state_pressed` → 按压反馈（如 alpha 变化）
- `state_focused` → 焦点补偿（如 translationZ 提升）

参考现有实现：`src/main/res/animator/morph_widget_button_ios_state.xml`

### Step 5: 防抖包装

若控件有点击行为，**必须**使用 `MorphClickListener` 包装：

```kotlin
// 正确
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

**注册映射**：在 `MorphKit.autoInit()` 或宿主配置中注册类名映射：

```kotlin
MorphKit.config {
    replace("SeekBar", "MorphSeekBar")
}
```

**混淆防御**：在 `consumer-rules.pro` 中添加 Keep 规则，**必须**精确到构造函数签名：

```proguard
# MorphSeekBar — 保持 (Context, AttributeSet) 构造函数签名
-keepclassmembers class com.morphkit.widget.seekbar.MorphSeekBar {
    public <init>(android.content.Context, android.util.AttributeSet);
}
```

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

**参考**：`MorphFactory2.onCreateView` 第 88-102 行

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
if (interactionMode == 0) {
    ripple = null  // 移除了视觉反馈，但没有任何补偿
}

// ✅ 正确：移除 Ripple 后，通过 StateListAnimator 提供焦点补偿
if (interactionMode == 0) {
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

### 4.4 KDoc 注释要求

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

### 4.5 TypedArray 生命周期

所有 `obtainStyledAttributes` 调用**必须**使用 `try-finally` 确保 `recycle()`：

```kotlin
val a = context.obtainStyledAttributes(attrs, R.styleable.MorphButton, defStyleAttr, 0)
try {
    // 读取属性
} finally {
    a.recycle()  // ← 绝不能遗漏
}
```

### 4.6 颜色获取方式

| 模式 | View 体系 | Compose 体系 |
|------|-----------|-------------|
| iOS | `MorphTokens.*` 静态常量 | `MorphTokens.*` 静态常量 |
| Pixel | `MaterialColors.getColor(context, R.attr.colorPrimary, default)` | `pixelFromContext(context)` → `MaterialColors.getColor()` |

**禁止**：在 Pixel 模式下硬编码颜色值。Pixel 模式的颜色真相源是宿主 Theme，不是 Token。

---

## 5. 关键文件索引

### 5.1 包结构映射

| 包名 | 职责 | 包含类 |
|------|------|--------|
| `com.morphkit.core` | 引擎、注入、降级、防抖 | `MorphKit`, `MorphConfig`, `MorphFactory2`, `MorphInstaller`, `MorphInitProvider`, `MorphClickListener` |
| `com.morphkit.theme` | 风格解析、Token、Context 包装 | `MorphStyleResolver`, `MorphTheme`, `MorphTokens`, `MorphKitIOSConfig` |
| `com.morphkit.theme.compose` | Compose 主题 | `MorphComposeTheme`, `MorphButton` (Compose) |
| `com.morphkit.widget.button` | 按钮类控件 | `MorphButton`, `MorphRadioButton` |
| `com.morphkit.widget.text` | 文本类控件 | `MorphTextView`, `MorphEditText` |
| `com.morphkit.widget.container` | 容器类控件 | `MorphCardView` |
| `com.morphkit.widget.selection` | 选择类控件 | `MorphCheckBox` |
| `com.morphkit.internal` | 内部工具（不对外暴露） | `ReflectionHelper` |

### 5.2 文件职责

| 文件 | 包 | 关键模式 |
|------|-----|----------|
| `MorphFactory2.kt` | `core` | 三阶段：先 originalFactory → 再替换 → 最后软修改 |
| `MorphInstaller.kt` | `core` | `onActivityPreCreated` 注入 + `onActivityCreated` 补充 AppCompat delegate |
| `MorphKit.kt` | `core` | `createView` try-catch + `modifyView` try-catch |
| `MorphStyleResolver.kt` | `theme` | OEM 设置 > StylePolicy > AUTO 检测 Dynamic Color |
| `MorphInitProvider.kt` | `core` | ContentProvider 零代码初始化 + 多进程保护 |
| `MorphConfig.kt` | `core` | `StylePolicy` 枚举 + `replace()` / `modify()` |
| `MorphButton.kt` | `widget.button` | 参考实现：双模式分发 + hasCustomBackground + StateListAnimator |
| `MorphClickListener.kt` | `core` | 防抖点击，时间戳冷却，默认 300ms |
| `MorphTokens.kt` | `theme` | 统一设计 Token，iOS 模式颜色/形状/排版常量 |
| `ReflectionHelper.kt` | `internal` | 安全反射字段访问与写入，供 MorphInstaller 使用 |
| `consumer-rules.pro` | — | R8 混淆防御，构造函数签名级 `-keep` + LayoutInflater 字段反射 keep |
| `attrs.xml` | — | 自定义属性 `morph*Style` + `morphInteractionMode` |
| `themes.xml` | — | 主题定义，透明 overlay，仅声明 `morph*Style` 属性分配 |
| `styles.xml` | — | 控件样式，iOS / Pixel 双套，通过 `morphInteractionMode` 分发 |

---

## 6. 测试合规

### 6.1 测试目录

所有测试代码**必须**与 `src/main` 保持完全一致的包路径：

| 测试文件 | 包路径 | 源码对应 |
|---------|--------|---------|
| `MorphKitTest.kt` | `src/test/kotlin/com/morphkit/core/` | `core.MorphKit` |
| `MorphFactory2Test.kt` | `src/test/java/com/morphkit/core/` | `core.MorphFactory2` |
| `MorphConfigTest.kt` | `src/test/java/com/morphkit/core/` | `core.MorphConfig` |
| `MorphInstallerTest.kt` | `src/test/java/com/morphkit/core/` | `core.MorphInstaller` |
| `MorphInitProviderTest.kt` | `src/test/java/com/morphkit/core/` | `core.MorphInitProvider` |
| `MorphClickListenerTest.kt` | `src/test/java/com/morphkit/core/` | `core.MorphClickListener` |
| `MorphStyleResolverTest.kt` | `src/test/java/com/morphkit/theme/` | `theme.MorphStyleResolver` |
| `MorphThemeTest.kt` | `src/test/java/com/morphkit/theme/` | `theme.MorphTheme` |

测试为本地 JVM 测试，不依赖真机或模拟器。

### 6.2 测试框架

- `JUnit 4.13.2` — 测试运行器
- `MockK 1.13.5` — Kotlin 生态 Mock 库（`mockkStatic(Log::class)` 避免 "not mocked" 错误）
- `Truth 1.1.5` — 可读性断言

### 6.3 必测场景

每个 Morph 控件**必须**覆盖以下测试场景：

| 场景 | 验证点 |
|------|--------|
| 崩溃降级 | 控件构造异常时，`MorphFactory2.onCreateView` 不抛异常，降级到 `originalFactory` |
| 责任链完整性 | 替换成功时，`originalFactory` 仍被调用（保证 AppCompat 着色） |
| 防抖有效性 | 300ms 内连续点击，业务 Listener 仅触发 1 次 |

### 6.4 CI/CD 合规

MorphKit 的合并请求（PR）**必须**通过所有 `src/test` 下的测试用例，特别是降级测试，否则**严禁**合入主分支。

---

## 7. AppCompat 共存协议

MorphKit 通过**两阶段注入**与 AppCompat 共存，这是整个系统的基础，任何修改不得破坏此协议：

```
Activity 生命周期
  │
  ├─ onActivityPreCreated()
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
compileOnly(platform("androidx.compose:compose-bom:2024.10.00"))
compileOnly("androidx.compose.material3:material3")
// ... 其他 Compose 依赖均为 compileOnly
```

- 不使用 Compose 的宿主 App 不会打包 Compose 运行时（约 2-3MB）
- 使用 Compose 的宿主 App 需自行声明 Compose 依赖
- Compose 主题通过 `CompositionLocal` 传播，与 View 体系的 `ContextThemeWrapper` 独立运行
- Pixel 模式下 Compose 的颜色**必须**从 Context Theme 读取（`MaterialColors.getColor()`），而非硬编码 Token

---

*本文档最后更新：2026-06-07 | 基于 MorphKit 分层架构重构后更新*
