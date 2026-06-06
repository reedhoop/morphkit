# MorphKit Consumer 混淆规则。
# 此文件随 AAR 分发，自动合并到宿主 App 的混淆配置中。
# 开发者无需额外配置，以下规则已覆盖 MorphKit 的所有反射与类名校验需求。

# Morph* 前缀控件必须保持类名，供运行时前缀校验
-keep class com.morphkit.engine.Morph* { *; }

# 核心引擎类
-keep class com.morphkit.engine.MorphKit { *; }
-keep class com.morphkit.engine.MorphConfig { *; }
-keep class com.morphkit.engine.MorphFactory2 { *; }
-keep class com.morphkit.engine.MorphInstaller { *; }
-keep class com.morphkit.engine.MorphTheme { *; }

# 顶层属性与扩展
-keepclassmembers class com.morphkit.engine.MorphKitKt { public static *; }
-keepclassmembers class com.morphkit.engine.MorphThemeKt { public static *; }
-keepclassmembers class com.morphkit.engine.MorphKitIOSConfigKt { public static *; }
