package com.morphkit.core

/**
 * MorphKit 交互模式枚举 — 控件按压/焦点反馈策略的统一分发依据。
 *
 * 所有 Morph* 控件（MorphButton、MorphRadioButton、MorphCheckBox）共享此枚举，
 * 通过 XML 属性 `morphInteractionMode` 或编程式赋值来选择交互策略。
 *
 * | 模式 | 按压反馈 | 焦点反馈 | 设计语言 |
 * |------|---------|---------|---------|
 * | [IOS] | Alpha 变暗（无涟漪） | translationZ 抬起 | iOS HIG |
 * | [MATERIAL] | M3 默认 Ripple | M3 默认焦点指示器 | Material Design 3 |
 */
enum class InteractionMode {
    IOS, MATERIAL
}
