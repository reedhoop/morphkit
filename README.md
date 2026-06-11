# MorphKit

**Android 双皮肤控件替换框架**

MorphKit 是一个 OEM 级 UI 基础设施库，以 AAR 形式交付。它通过 `LayoutInflater.Factory2` 在布局膨胀阶段拦截控件创建，将宿主 App 的原生控件静默替换为 Morph 增强控件，实现 **iOS 极简风** 与 **Pixel (Material You) 原生风** 的动态换肤 -- 宿主 App 无需修改任何业务代码。

---

## 核心特性

- **零代码接入** -- 通过 `ContentProvider` 自动初始化 + `ActivityLifecycleCallbacks` 全局注入，无需手动集成
- **iOS / Material 双皮肤** -- 内置两套完整皮肤，支持 `StylePolicy.AUTO` 自适应切换（根据设备 Dynamic Color 能力自动选择）
- **Compose 支持** -- 提供 `MorphTheme` 包裹器和 Compose 版 `MorphButton`，Compose 依赖为 `compileOnly`，不使用 Compose 的宿主不会增加包体积
- **毛玻璃效果** -- `MorphCardView` 支持 backdrop blur，基于 `RenderEffect` GPU 高斯模糊，软件 Canvas 自动降级为 Stack Blur
- **防御式架构** -- 全链路 try-catch 降级，替换异常时回退到原始控件，绝不导致宿主白屏
- **尊重宿主属性** -- 仅补充未设置的属性，绝不覆盖业务方显式声明的 `android:background`、`android:textColor` 等值

---

## 快速开始

### 1. 添加依赖

```kotlin
// build.gradle.kts (app module)
dependencies {
    implementation("com.morphkit:morphkit:1.0.0")
}
```

### 2. 一行代码接入（推荐）

在 `Application.onCreate` 中调用即可：

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initIOSStyle()   // 全局启用 iOS 极简风
    }
}
```

`initIOSStyle()` 会注册所有 iOS 风格的控件替换规则并注入全局 `MorphFactory2` 拦截链。

### 3. 手动配置

如需精细控制替换行为或使用 Pixel 风格：

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MorphKit.init(this) {
            // 风格策略：AUTO / IOS / PIXEL
            stylePolicy(StylePolicy.AUTO)

            // 按需注册替换规则
            groupReplace(listOf("Button", "androidx.appcompat.widget.AppCompatButton")) { ctx, attrs ->
                MorphButton(ctx, attrs)
            }
            replace("TextView") { ctx, attrs ->
                MorphTextView(ctx, attrs)
            }

            // 软修改兜底
            modify("androidx.recyclerview.widget.RecyclerView") { rv ->
                rv.overScrollMode = View.OVER_SCROLL_NEVER
            }
        }
    }
}
```

也可以在 XML 中切换内置皮肤：

```xml
<!-- iOS 极简风 -->
<application android:theme="@style/Theme.MorphKit.iOS" ... >

<!-- Pixel 原生风 -->
<application android:theme="@style/Theme.MorphKit.Pixel" ... >
```

---

## 架构概览

MorphKit 采用 **引擎 - 皮肤 - 控件** 三层分离架构：

```
┌─────────────────────────────────────────────────┐
│  引擎层 (com.morphkit.core)                      │
│  MorphKit / MorphFactory2 / MorphInstaller       │
│  MorphConfig (DSL) / MorphInitProvider           │
├─────────────────────────────────────────────────┤
│  皮肤层 (com.morphkit.theme)                     │
│  MorphStyleResolver / MorphTheme / MorphTokens   │
│  compose/ MorphComposeTheme + MorphButton        │
├─────────────────────────────────────────────────┤
│  控件层 (com.morphkit.widget)                    │
│  button / text / container / selection           │
└─────────────────────────────────────────────────┘
```

**核心流程：**

1. `MorphInitProvider` (ContentProvider) 在 App 启动时触发 `MorphInstaller.install()`
2. `MorphInstaller` 注册 `ActivityLifecycleCallbacks`，在每个 Activity 的 `onActivityPreCreated` 注入 `MorphFactory2`
3. 布局膨胀时，`MorphFactory2.onCreateView` 执行三阶段责任链：先调 `originalFactory`（AppCompat） -> 再执行硬替换 -> 最后软修改兜底
4. 风格决策由 `MorphStyleResolver` 按优先级解析：OEM 系统设置 > `StylePolicy` 配置 > AUTO 检测

**Token 驱动设计：** 所有视觉属性（颜色、圆角、排版）通过设计 Token 统一管理。View 体系通过 `MaterialColors.getColor(context, ...)` 从 Theme 读取，Compose 体系通过 `CompositionLocal` 传播，两套体系共享同一色彩真相源。

---

## 控件清单

| Morph 控件 | 替换目标 | 所属包 | 说明 |
|---|---|---|---|
| `MorphButton` | Button / AppCompatButton | `widget.button` | 双模式交互分发，iOS 按压动画，StateListAnimator |
| `MorphRadioButton` | RadioButton | `widget.button` | 自定义圆形指示器，MorphClickListener 防抖 |
| `MorphTextView` | TextView / AppCompatTextView | `widget.text` | textStyle 重映射 (NORMAL -> MEDIUM)，语义色支持 |
| `MorphEditText` | EditText / AppCompatEditText | `widget.text` | BARE / SEARCH 双样式，焦点背景反馈 |
| `MorphCardView` | CardView / MaterialCardView | `widget.container` | 极简白卡片 / 毛玻璃模式，Bitmap 安全回收 |
| `MorphCheckBox` | CheckBox | `widget.selection` | 自定义圆角方形勾选，MorphClickListener 防抖 |

Compose 侧提供 `MorphButton` (composable) 和 `MorphTheme` 包裹器。

---

## Compose 集成

在 `setContent` 最外层包裹 `MorphTheme`：

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        MorphTheme {
            MyAppNavigation()
        }
    }
}
```

`MorphTheme` 内部通过 `CompositionLocalProvider` 向下传播 `LocalMorphColors`（37 个语义色字段）、`LocalMorphShape` 和 `LocalMorphInteractionMode`，确保 Compose 组件与 View 体系视觉完全一致。

Compose 依赖声明为 `compileOnly`，不使用 Compose 的宿主 App 不会打包 Compose 运行时（约 2-3 MB）。使用 Compose 的宿主需自行声明 Compose 依赖。

---

## 自定义皮肤

宿主 App 可继承内置 Theme 并覆写 `morph*Style` 属性来扩展自定义皮肤：

```xml
<!-- styles.xml -->
<style name="Theme.MyApp.Promotion" parent="Theme.MorphKit.iOS">
    <item name="morphButtonStyle">@style/Widget.MyApp.Button.Promotion</item>
</style>

<style name="Widget.MyApp.Button.Promotion" parent="Widget.MorphKit.Button.iOS">
    <item name="morphCornerRadius">20dp</item>
    <item name="morphInteractionMode">material</item>
    <item name="android:background">#FF6200EE</item>
</style>
```

```xml
<!-- AndroidManifest.xml -->
<application android:theme="@style/Theme.MyApp.Promotion" ... >
```

可覆写的 Theme 级属性：`morphButtonStyle`、`morphTextViewStyle`、`morphEditTextStyle`、`morphCardStyle`。

可覆写的 Style 级属性：`morphInteractionMode`（0=ios / 1=material）、`morphCornerRadius`。

---

## 构建要求

| 属性 | 值 |
|---|---|
| minSdk | 35 (Android 15) |
| compileSdk | 35 |
| targetSdk | 35 |
| AGP | 8.9.1 |
| Kotlin | 2.1.21 |
| JVM Target | 17 |
| Compose Compiler | 2.1.21 (plugin) |

minSdk 35 确保 `RenderEffect.createBlurEffect`（API 31+）、`onActivityPreCreated`（API 29+）等能力始终可用，无需版本守卫。

---

## License

```
Copyright (c) MorphKit Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
