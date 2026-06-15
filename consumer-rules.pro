# ═══════════════════════════════════════════════════════════════════════════════
# MorphKit Consumer Proguard Rules
# 这些规则会自动应用到宿主 App 的混淆配置中
# ═══════════════════════════════════════════════════════════════════════════════

# ═══════════════════════════════════════════════════════════════════════════════
# 1. MorphKit 控件类 — 类名前缀校验依赖 "Morph" 前缀
#    必须保留类名和 (Context, AttributeSet) 构造函数
#    LayoutInflater 通过反射调用此构造函数，R8 移除会导致运行时 Crash
# ═══════════════════════════════════════════════════════════════════════════════
-keep class com.morphkit.widget.button.MorphButton {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class com.morphkit.widget.button.MorphRadioButton {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class com.morphkit.widget.text.MorphTextView {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class com.morphkit.widget.text.MorphEditText {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class com.morphkit.widget.container.MorphCardView {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class com.morphkit.widget.selection.MorphCheckBox {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ═══════════════════════════════════════════════════════════════════════════════
# 2. MorphKit Compose 模块 — Compose 编译器需要保留 Composable 函数名
# ═══════════════════════════════════════════════════════════════════════════════
-keep class com.morphkit.theme.compose.** { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# 3. MorphKit Token 层 — 常量可能被内联，但类本身需保留
#    防止 R8 判定为未使用而移除整个类
# ═══════════════════════════════════════════════════════════════════════════════
-keep class com.morphkit.theme.MorphTokens { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# 4. MorphKit 枚举 — StylePolicy / InteractionMode
#    枚举在 Kotlin/JVM 中通常被自动 Keep，但显式声明更安全
# ═══════════════════════════════════════════════════════════════════════════════
-keepclassmembers enum com.morphkit.core.** {
    **[] $VALUES;
    *;
}
-keepclassmembers enum com.morphkit.theme.** {
    **[] $VALUES;
    *;
}
-keepclassmembers enum com.morphkit.theme.compose.** {
    **[] $VALUES;
    *;
}

# ═══════════════════════════════════════════════════════════════════════════════
# 5. MorphKit ContentProvider — 必须保留全限定名
# ═══════════════════════════════════════════════════════════════════════════════
-keep class com.morphkit.core.MorphInitProvider { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# 6. MorphStyleResolver 反射目标 — DynamicColors.isDynamicColorAvailable
#    Material 库自带 Keep 规则，此处作为防御性补充
# ═══════════════════════════════════════════════════════════════════════════════
-keep class com.google.android.material.color.DynamicColors {
    public static boolean isDynamicColorAvailable(android.content.Context);
}

# ═══════════════════════════════════════════════════════════════════════════════
# 7. LayoutInflater 反射字段 — MorphInstaller 通过反射修改这些字段
#    Framework 类不会被 R8 混淆，但显式声明防止未来工具链变更
# ═══════════════════════════════════════════════════════════════════════════════
-keepclassmembers class android.view.LayoutInflater {
    android.view.LayoutInflater$Factory2 mFactory2;
    android.view.LayoutInflater$Factory mFactory;
    boolean mFactorySet;
}

# ═══════════════════════════════════════════════════════════════════════════════
# 8. MorphFactory2 — 必须保留类名，因为 MorphInstaller 通过 instanceof 检查
# ═══════════════════════════════════════════════════════════════════════════════
-keep class com.morphkit.core.MorphFactory2 { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# 9. MorphKit 核心单例 — 反射和全局访问入口
# ═══════════════════════════════════════════════════════════════════════════════
-keep class com.morphkit.core.MorphKit { *; }
-keep class com.morphkit.core.MorphConfig { *; }
-keep class com.morphkit.theme.MorphStyleResolver { *; }
-keep class com.morphkit.core.MorphInstaller { *; }
-keep class com.morphkit.theme.MorphTheme { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# 10. Widget 注册入口 — MorphInitProvider 通过回调注入 registerDefaultWidgets
# ═══════════════════════════════════════════════════════════════════════════════
-keep class com.morphkit.widget.WidgetRegistryKt { *; }

# ═══════════════════════════════════════════════════════════════════════════════
# 11. 内部反射工具 — MorphInstaller 通过反射操作 LayoutInflater 字段
# ═══════════════════════════════════════════════════════════════════════════════
-keep class com.morphkit.internal.ReflectionHelper { *; }
