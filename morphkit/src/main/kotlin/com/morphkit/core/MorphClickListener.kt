package com.morphkit.core

import android.os.SystemClock
import android.view.View
import java.util.concurrent.atomic.AtomicLong

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
 * 使用 [AtomicLong] + [SystemClock.elapsedRealtime] 实现无锁原子防抖：
 * - [AtomicLong.compareAndSet] 保证 check-then-act 的原子性，消除竞态条件
 * - [SystemClock.elapsedRealtime] 不受系统时钟调整影响，比 `System.currentTimeMillis()` 更可靠
 * - 时间源可通过内部构造函数注入，便于单元测试
 *
 * @param debounceInterval 防抖冷却时间（毫秒），默认 300ms
 * @param block 业务点击逻辑
 */
class MorphClickListener @JvmOverloads constructor(
    private val debounceInterval: Long = DEFAULT_DEBOUNCE_INTERVAL,
    private val block: (View) -> Unit
) : View.OnClickListener {

    init {
        require(debounceInterval >= 0) { "debounceInterval 不能为负数" }
    }

    /** 仅供测试使用：注入自定义时间源 */
    internal constructor(
        debounceInterval: Long,
        timeSource: () -> Long,
        block: (View) -> Unit
    ) : this(debounceInterval, block) {
        this.timeSource = timeSource
    }

    /** 时间源：默认使用 SystemClock.elapsedRealtime()，测试中可替换 */
    private var timeSource: () -> Long = { SystemClock.elapsedRealtime() }

    /** 上次点击的时间戳，使用 AtomicLong 保证原子性 CAS */
    private val lastClickTime = AtomicLong(-1L)

    override fun onClick(v: View) {
        val now = timeSource()
        // CAS 循环：原子性地检查并更新 lastClickTime，消除竞态条件
        while (true) {
            val last = lastClickTime.get()
            if (last >= 0 && now - last < debounceInterval) {
                return // 冷却期内，忽略重复点击
            }
            if (lastClickTime.compareAndSet(last, now)) {
                break // 成功获取执行权
            }
            // CAS 失败说明其他线程抢先了，重新检查
        }
        block(v)
    }

    companion object {
        /** 默认防抖冷却时间：300ms */
        const val DEFAULT_DEBOUNCE_INTERVAL = 300L
    }
}
