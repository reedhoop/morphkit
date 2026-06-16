package com.morphkit.widget

import com.morphkit.core.MorphConfig

/**
 * MorphKit 内置控件的默认替换规则注册。
 *
 * 将 `registerDefaultWidgets()` 从 [MorphConfig]（core 包）提取到 widget 包，
 * 消除 core -> widget 的概念性反向依赖。依赖方向严格遵循 widget -> theme -> core。
 *
 * 此文件作为 widget 层的注册入口，提供 [MorphConfig] 的扩展函数，
 * 使调用方在 DSL 块中仍可像成员方法一样调用 `registerDefaultWidgets()`。
 *
 * @see MorphConfig
 * @see com.morphkit.core.MorphKit.autoInit
 */

/**
 * 注册 MorphKit 内置控件的默认替换规则。
 *
 * 包含所有 MorphKit 提供的控件替换规则，确保 [com.morphkit.core.MorphKit.autoInit] 和
 * [com.morphkit.theme.initIOSStyle] 使用同一份规则清单，避免遗漏或不同步。
 *
 * 控件清单：
 * - TextView / AppCompatTextView -> MorphTextView
 * - Button / AppCompatButton -> MorphButton
 * - EditText / AppCompatEditText -> MorphEditText
 * - CardView -> MorphCardView
 * - MaterialCardView -> MorphCardView
 * - RadioButton / AppCompatRadioButton -> MorphRadioButton
 * - CheckBox / AppCompatCheckBox -> MorphCheckBox
 */
fun MorphConfig.registerDefaultWidgets() {
    groupReplace(listOf("TextView", "androidx.appcompat.widget.AppCompatTextView")) { ctx, attrs ->
        com.morphkit.widget.text.MorphTextView(ctx, attrs)
    }
    groupReplace(listOf("Button", "androidx.appcompat.widget.AppCompatButton")) { ctx, attrs ->
        com.morphkit.widget.button.MorphButton(ctx, attrs)
    }
    groupReplace(listOf("EditText", "androidx.appcompat.widget.AppCompatEditText")) { ctx, attrs ->
        com.morphkit.widget.text.MorphEditText(ctx, attrs)
    }
    replace("androidx.cardview.widget.CardView") { ctx, attrs ->
        com.morphkit.widget.container.MorphCardView(ctx, attrs)
    }
    replace("com.google.android.material.card.MaterialCardView") { ctx, attrs ->
        com.morphkit.widget.container.MorphCardView(ctx, attrs)
    }
    groupReplace(listOf("RadioButton", "androidx.appcompat.widget.AppCompatRadioButton")) { ctx, attrs ->
        com.morphkit.widget.button.MorphRadioButton(ctx, attrs)
    }
    groupReplace(listOf("CheckBox", "androidx.appcompat.widget.AppCompatCheckBox")) { ctx, attrs ->
        com.morphkit.widget.selection.MorphCheckBox(ctx, attrs)
    }
}
