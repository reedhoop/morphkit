package com.morphkit.core

import com.morphkit.core.MorphClickListener
import android.view.View
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test

/**
 * MorphClickListener 防抖逻辑测试。
 *
 * 验证在指定冷却时间窗口内仅响应首次点击，
 * 防止快速连点导致的重复提交。
 *
 * ## CI/CD 合规要求
 *
 * MorphKit 的合并请求（PR）必须通过所有 `src/test` 下的测试用例，
 * 否则严禁合入主分支。
 */
class MorphClickListenerTest {

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 C1：300ms 内连续点击两次，业务 Listener 仅被调用 1 次
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `300ms内连续点击两次_业务Listener仅被调用1次`() {
        var clickCount = 0
        val listener = MorphClickListener(debounceInterval = 300L) {
            clickCount++
        }

        val mockView: View = io.mockk.mockk(relaxed = true)

        // 第一次点击
        listener.onClick(mockView)
        assertEquals("第一次点击应被响应", 1, clickCount)

        // 第二次点击（在 300ms 冷却期内）
        listener.onClick(mockView)
        assertEquals("冷却期内第二次点击应被忽略", 1, clickCount)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 C2：冷却期结束后再次点击，业务 Listener 被调用
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `冷却期结束后再次点击_业务Listener被调用`() {
        var clickCount = 0
        val listener = MorphClickListener(debounceInterval = 100L) {
            clickCount++
        }

        val mockView: View = io.mockk.mockk(relaxed = true)

        // 第一次点击
        listener.onClick(mockView)
        assertEquals(1, clickCount)

        // 等待冷却期结束
        Thread.sleep(150)

        // 冷却期结束后的点击应被响应
        listener.onClick(mockView)
        assertEquals("冷却期结束后应响应点击", 2, clickCount)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 C3：自定义冷却时间生效
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `自定义冷却时间生效`() {
        var clickCount = 0
        val listener = MorphClickListener(debounceInterval = 500L) {
            clickCount++
        }

        val mockView: View = io.mockk.mockk(relaxed = true)

        // 第一次点击
        listener.onClick(mockView)
        assertEquals(1, clickCount)

        // 200ms 后点击（仍在 500ms 冷却期内）
        Thread.sleep(200)
        listener.onClick(mockView)
        assertEquals("200ms 后仍在 500ms 冷却期内", 1, clickCount)

        // 再等 350ms（总计 550ms，冷却期已过）
        Thread.sleep(350)
        listener.onClick(mockView)
        assertEquals("550ms 后冷却期已过，应响应", 2, clickCount)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 C4：默认冷却时间为 300ms
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `默认冷却时间为300ms`() {
        assertEquals(
            "默认冷却时间应为 300ms",
            300L,
            MorphClickListener.DEFAULT_DEBOUNCE_INTERVAL
        )
    }
}
