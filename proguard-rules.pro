# ╔════════════════════════════════════════════════════════════════════════════╗
# ║                     MorphKit 混淆规则                                      ║
# ║                  (ProGuard / R8 Rules)                                     ║
# ╠════════════════════════════════════════════════════════════════════════════╣
# ║                                                                            ║
# ║  MorphKit 框架依赖反射与类名校验，以下规则确保关键类/方法/字段               ║
# ║  不被混淆或移除，否则将导致：                                                ║
# ║                                                                            ║
# ║  - LayoutInflater.Factory2 注入失败（反射 mFactory2/mFactory 字段被重命名）  ║
# ║  - 控件类名前缀校验失效（Morph* 类被重命名后无法匹配前缀）                    ║
# ║  - 运行时 ClassNotFound（creator Lambda 被内联/删除）                       ║
# ║                                                                            ║
# ╚════════════════════════════════════════════════════════════════════════════╝

# ═════════════════════════════════════════════════════════════════════════════
#  1. 核心引擎 — 反射保护
# ═════════════════════════════════════════════════════════════════════════════

# MorphKit 单例对象 — 包含 init/createView/modifyView 等核心方法
-keep class com.morphkit.engine.MorphKit { *; }

# MorphConfig — DSL 配置类，replaceMap/modifyMap 通过反射/内联访问
-keep class com.morphkit.engine.MorphConfig { *; }

# MorphFactory2 — LayoutInflater.Factory2 代理，必须保持类名与接口实现
-keep class com.morphkit.engine.MorphFactory2 { *; }

# MorphInstaller — 反射访问 LayoutInflater 的 mFactory2/mFactory 字段
# 其内部所有方法（install/injectFactory2/resolveField/setFieldValue 等）均不可混淆
-keep class com.morphkit.engine.MorphInstaller { *; }

# ═════════════════════════════════════════════════════════════════════════════
#  2. Morph* 前缀控件 — 类名校验保护
# ═════════════════════════════════════════════════════════════════════════════
#
# MorphKit 运行时通过 view.javaClass.simpleName.startsWith("Morph") 校验控件
# 类名前缀。若控件类被混淆，校验将失败并输出大量规范警告。
# 因此所有 Morph* 开头的自定义 View 类必须保持原始类名。
#
# 此规则同时保护控件的构造函数，确保 LayoutInflater 能通过反射创建实例。

-keep class com.morphkit.engine.Morph* {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public *;
}

# ═════════════════════════════════════════════════════════════════════════════
#  3. 设计系统基座 — 保持完整
# ═════════════════════════════════════════════════════════════════════════════

# MorphTheme — 所有控件直接引用其方法/属性
-keep class com.morphkit.engine.MorphTheme { *; }

# MorphTheme$MorphTypography — 排版令牌
-keep class com.morphkit.engine.MorphTheme$MorphTypography { *; }

# MorphTheme$TextStyle — data class，可能被序列化
-keep class com.morphkit.engine.MorphTheme$TextStyle { *; }

# MorphTheme$FontWeight — enum，toTypeface() 方法被广泛调用
-keep class com.morphkit.engine.MorphTheme$FontWeight { *; }

# ═════════════════════════════════════════════════════════════════════════════
#  4. 顶层扩展与常量
# ═════════════════════════════════════════════════════════════════════════════

# unifiedPrefix 顶层属性 — MorphConfig.unifiedPrefix 委托引用
-keepclassmembers class com.morphkit.engine.UnifiedPrefixKt {
    public static *;
}

# MORPH_TAG_KEY 顶层属性 — MorphKit 中 setTag/getTag 依赖
-keepclassmembers class com.morphkit.engine.MorphKitKt {
    public static *;
}

# dp 扩展属性 — Int.dp / Float.dp
-keepclassmembers class com.morphkit.engine.MorphThemeKt {
    public static *;
}

# initIOSStyle 扩展函数
-keepclassmembers class com.morphkit.engine.MorphKitIOSConfigKt {
    public static *;
}

# ═════════════════════════════════════════════════════════════════════════════
#  5. Lambda 保护 — DSL creator / modifier 不可被内联删除
# ═════════════════════════════════════════════════════════════════════════════
#
# MorphConfig 的 replaceMap/modifyMap 中存储的 Lambda 在运行时通过 Map 查找
# 并反射调用。R8 可能将这些 Lambda 识别为"无引用"而优化删除，
# 导致 createView/modifyView 返回 null 或抛异常。

-keepclassmembers class * {
    ** create(android.content.Context, android.util.AttributeSet);
}

# ═════════════════════════════════════════════════════════════════════════════
#  6. LayoutInflater 反射目标字段 — 警告性保护
# ═════════════════════════════════════════════════════════════════════════════
#
# MorphInstaller 通过反射访问 android.view.LayoutInflater 的以下字段：
# - mFactory2 (LayoutInflater.Factory2)
# - mFactory (LayoutInflater.Factory)
#
# 这些字段属于 Android framework，不受 ProGuard/R8 管理，
# 此处声明仅作文档目的，提醒维护者不要修改相关反射逻辑。

# (无需 -keep 规则，framework 类不受混淆影响)

# ═════════════════════════════════════════════════════════════════════════════
#  7. 通用 Android View 保护
# ═════════════════════════════════════════════════════════════════════════════

# 任何继承 View 的自定义控件的构造函数必须保留
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ═════════════════════════════════════════════════════════════════════════════
#  8. 注解保护
# ═════════════════════════════════════════════════════════════════════════════

# 保留所有 @Keep 注解标注的元素
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
