package com.morphkit.theme

import android.app.Application
import android.util.Log
import android.view.View
import com.morphkit.core.MorphKit
import com.morphkit.core.MorphConfig
import com.morphkit.widget.button.MorphButton
import com.morphkit.widget.text.MorphTextView
import com.morphkit.widget.text.MorphEditText
import com.morphkit.widget.container.MorphCardView

/**
 * iOS 设计系统一键接入配置。
 *
 * 提供 [Application.initIOSStyle] 扩展函数，将所有 iOS 风格 Morph 控件
 * 通过 MorphKit DSL 注入到全局 LayoutInflater 拦截链中。
 *
 * ## 使用方式
 *
 * 在 [Application.onCreate] 中调用即可：
 * ```kotlin
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         initIOSStyle()
 *     }
 * }
 * ```
 *
 * ## 注入清单
 *
 * | 原始控件 | 替换目标 | 方式 |
 * |---|---|---|
 * | TextView / AppCompatTextView | MorphTextView | groupReplace |
 * | Button / AppCompatButton | MorphButton | groupReplace |
 * | EditText / AppCompatEditText | MorphEditText | groupReplace |
 * | CardView | MorphCardView | replace |
 * | MaterialCardView | MorphCardView | replace |
 * | RecyclerView | 取消过度滚动 | modify |
 *
 * @see MorphKit
 * @see MorphConfig
 */

/**
 * 将 iOS 设计系统规则注入 MorphKit 引擎。
 *
 * 在 [Application.onCreate] 中调用此扩展函数，一行代码完成所有 iOS 风格控件的
 * 全局注册与 Factory2 注入。内部按以下顺序执行：
 *
 * 1. 调用 [MorphKit.init] 初始化引擎并执行 DSL 配置
 * 2. 在 DSL 块中注册所有 **硬替换** 规则（groupReplace / replace）
 * 3. 在 DSL 块中注册所有 **软修改兜底** 规则（modify）
 * 4. 打印确认日志
 *
 * ### 为什么 Button 和 EditText 要 groupReplace？
 *
 * AppCompat 会在布局膨胀时将 `<Button>` 自动替换为 `AppCompatButton`，
 * `<EditText>` 替换为 `AppCompatEditText`。这意味着同一个 XML 标签，
 * 在 Factory2 回调中可能以两种不同名称出现：
 * - XML 写 `<Button>` → 回调收到 `"Button"` 或 `"androidx.appcompat.widget.AppCompatButton"`
 * - XML 写 `<EditText>` → 回调收到 `"EditText"` 或 `"androidx.appcompat.widget.AppCompatEditText"`
 *
 * 使用 [MorphConfig.groupReplace] 确保无论哪个名称出现，
 * 都能命中同一个替换逻辑，避免遗漏。
 *
 * ### 为什么 CardView 和 MaterialCardView 用 replace 而不是 groupReplace？
 *
 * CardView 和 MaterialCardView 在项目中通常不会同时出现，
 * 且全限定名较长，分开注册更便于阅读和维护。
 */
fun Application.initIOSStyle() {
    MorphKit.init(this) {

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        //  硬替换：iOS 风格控件全局接管
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        // TextView → MorphTextView
        // 拦截原生 TextView 及 AppCompat 别名，统一替换为 iOS 字重/颜色规范的 MorphTextView
        groupReplace(
            listOf(
                "TextView",
                "androidx.appcompat.widget.AppCompatTextView"
            )
        ) { context, attrs ->
            MorphTextView(context, attrs)
        }

        // Button → MorphButton
        // 拦截原生 Button 及 AppCompat 别名，统一替换为 iOS 触控反馈风格的 MorphButton
        groupReplace(
            listOf(
                "Button",
                "androidx.appcompat.widget.AppCompatButton"
            )
        ) { context, attrs ->
            MorphButton(context, attrs)
        }

        // EditText → MorphEditText
        // 拦截原生 EditText 及 AppCompat 别名，统一替换为 iOS 极简风格的 MorphEditText
        groupReplace(
            listOf(
                "EditText",
                "androidx.appcompat.widget.AppCompatEditText"
            )
        ) { context, attrs ->
            MorphEditText(context, attrs)
        }

        // CardView → MorphCardView
        // 拦截 androidx CardView，替换为 iOS 极简卡片（无阴影 + 毛玻璃能力）
        replace("androidx.cardview.widget.CardView") { context, attrs ->
            MorphCardView(context, attrs)
        }

        // MaterialCardView → MorphCardView
        // 拦截 Material CardView，同样替换为 MorphCardView
        replace("com.google.android.material.card.MaterialCardView") { context, attrs ->
            MorphCardView(context, attrs)
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        //  软修改兜底：属性级 iOS 风格统一
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        // RecyclerView：取消 Android 默认的过度滚动发光效果
        // 改为 iOS 风格的弹性滚动提示（OVER_SCROLL_NEVER 由系统弹性处理）
        modify("androidx.recyclerview.widget.RecyclerView") { rv ->
            rv.overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    Log.i("MorphKit", "iOS Design System Applied.")
}
