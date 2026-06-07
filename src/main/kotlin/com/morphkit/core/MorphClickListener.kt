package com.morphkit.core

import android.view.View

/**
 * 防抖点击监听器。
 *
 * 包装业务方的 [View.OnClickListener]，在指定的冷却时间窗口内仅响应首次点击，
 * 防止用户快速连点导致的重复提交、页面重复跳转等问题。
 *
 * ## 使用方式
 *
 * ```kotlin
 * button.setOnClickListener(MorphClickListener {
 *     // 业务逻辑，300ms 内不会被重复调用
 *     submitOrder()
 * })
 * ```
 *
 * ## 实现原理
 *
 * 记录上次点击的 elapsedRealtime [lastClickTime]，若两次点击间隔小于 [debounceInterval]，
 * 则忽略本次点击。默认冷却时间 300ms，可通过构造函数自定义。
 *
 * @param debounceInterval 防抖冷却时间（毫秒），默认 300ms
 * @param block 业务点击逻辑
 */
class MorphClickListener(
    private val debounceInterval: Long = DEFAULT_DEBOUNCE_INTERVAL,
    private val block: (View) -> Unit
) : View.OnClickListener {

    private var lastClickTime: Long = 0L

    override fun onClick(v: View) {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastClickTime < debounceInterval) {
            return // 冷却期内，忽略重复点击
        }
        lastClickTime = now
        block(v)
    }

    companion object {
        /** 默认防抖冷却时间：300ms */
        const val DEFAULT_DEBOUNCE_INTERVAL = 300L
    }
}
