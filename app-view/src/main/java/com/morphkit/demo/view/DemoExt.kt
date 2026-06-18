package com.morphkit.demo.view

import android.content.Context

/**
 * Demo 公共工具（M12 修复）。
 *
 * 消除各 Page 中重复的 `val density = ...; val dp16 = (16 * density).toInt()` 样板代码。
 * 用法：`val dp16 = requireContext().dp(16)`
 */
fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
