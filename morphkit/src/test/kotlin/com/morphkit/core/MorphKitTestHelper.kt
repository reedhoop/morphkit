package com.morphkit.core

/**
 * MorphKit 测试辅助工具 — 反射重置单例状态。
 *
 * 多个测试类需要重置 MorphKit / MorphInstaller 的内部状态，
 * 此工具类消除重复的反射重置代码（L5 修复）。
 *
 * 使用方式：
 * ```kotlin
 * @BeforeEach
 * fun setUp() {
 *     MorphKitTestHelper.resetMorphKit()
 *     MorphKitTestHelper.resetMorphInstaller()
 * }
 * ```
 */
object MorphKitTestHelper {

    /**
     * 反射重置 MorphKit 的初始化状态（initGuard / initialized / config）。
     *
     * 必须在每个测试前调用，防止前一个测试的初始化状态影响后续测试。
     * 反射失败时静默忽略——测试可能因此失败并给出明确错误信息。
     */
    fun resetMorphKit() {
        try {
            // Reset initGuard (AtomicBoolean) — 防止重复初始化的守卫
            val initGuardField = MorphKit::class.java.getDeclaredField("initGuard")
            initGuardField.isAccessible = true
            (initGuardField.get(MorphKit) as java.util.concurrent.atomic.AtomicBoolean).set(false)

            // Reset initialized (@Volatile Boolean) — 初始化完成标志
            val initializedField = MorphKit::class.java.getDeclaredField("initialized")
            initializedField.isAccessible = true
            initializedField.setBoolean(MorphKit, false)

            val configField = MorphKit::class.java.getDeclaredField("config")
            configField.isAccessible = true
            // 将 lateinit config 重置为未初始化状态
            configField.set(MorphKit, null)
        } catch (e: Exception) {
            // 反射重置失败时忽略，测试可能因此失败并给出明确错误信息
        }
    }

    /**
     * 反射重置 MorphInstaller 的安装标志（installed AtomicBoolean）。
     *
     * 必须在每个测试前调用，防止前一个测试的安装状态影响后续测试。
     */
    fun resetMorphInstaller() {
        try {
            val field = MorphInstaller::class.java.getDeclaredField("installed")
            field.isAccessible = true
            field.setBoolean(MorphInstaller, false)
        } catch (e: Exception) {
            // 反射重置失败时忽略
        }
    }
}
