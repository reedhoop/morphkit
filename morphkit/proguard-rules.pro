# ╔════════════════════════════════════════════════════════════════════════════╗
# ║                     MorphKit 混淆规则                                      ║
# ║                  (ProGuard / R8 Rules)                                     ║
# ╠════════════════════════════════════════════════════════════════════════════╣
# ║                                                                            ║
# ║  L21 修复：library module 的混淆规则统一在 consumer-rules.pro 维护，       ║
# ║  发布时自动应用到宿主。本文件仅保留本 module 测试编译专用规则，            ║
# ║  避免与 consumer-rules.pro 双份维护导致漂移。                              ║
# ║                                                                            ║
# ║  MorphKit 框架依赖反射与类名校验，详见 consumer-rules.pro。                ║
# ║                                                                            ║
# ╚════════════════════════════════════════════════════════════════════════════╝

# ═════════════════════════════════════════════════════════════════════════════
#  1. 通用 Android View 保护（测试编译专用）
# ═════════════════════════════════════════════════════════════════════════════

# 任何继承 View 的 MorphKit 自定义控件的构造函数必须保留
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ═════════════════════════════════════════════════════════════════════════════
#  2. 注解保护
# ═════════════════════════════════════════════════════════════════════════════

# 保留所有 @Keep 注解标注的元素
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ═════════════════════════════════════════════════════════════════════════════
#  3. LayoutInflater 反射目标字段 — 警告性保护
# ═════════════════════════════════════════════════════════════════════════════
#
# MorphInstaller 通过 ReflectionHelper 反射访问 android.view.LayoutInflater 的以下字段：
# - mFactory2 (LayoutInflater.Factory2)
# - mFactory (LayoutInflater.Factory)
# - mFactorySet (boolean)
#
# 这些字段属于 Android framework，不受 ProGuard/R8 管理，
# 此处声明仅作文档目的，提醒维护者不要修改相关反射逻辑。

# (无需 -keep 规则，framework 类不受混淆影响)
