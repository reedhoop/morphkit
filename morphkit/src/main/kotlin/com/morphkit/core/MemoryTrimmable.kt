package com.morphkit.core

/**
 * 内存压力响应接口。
 *
 * widget 层（或其他需要管理内存的模块）实现此接口并通过
 * [MorphKit.registerMemoryTrimmable] 注册，core 层在收到系统内存压力回调时
 * 统一通知所有注册者，避免 core 层直接依赖 widget 层。
 */
interface MemoryTrimmable {
    /**
     * 系统内存压力回调。
     *
     * @param level trim level，参见 [android.content.ComponentCallbacks2] 常量
     */
    fun onTrimMemory(level: Int)
}
