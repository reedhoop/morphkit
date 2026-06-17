package com.morphkit.widget.container

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.util.Log
import android.view.View
import android.view.ViewGroup

/**
 * MorphKit 背景模糊辅助工具。
 *
 * 实现真正的「behind-blur」毛玻璃效果：截取父容器在卡片区域下方的像素，
 * 对其进行高斯模糊，然后将模糊后的 Bitmap 作为卡片背景图层显示。
 *
 * ## 模糊策略（minSdk=35）
 *
 * minSdk=35（Android 15）保证 [RenderEffect.createBlurEffect]（API 31+）始终可用，
 * 使用 GPU 加速的高斯模糊作为唯一路径。
 * 当硬件加速不可用时（极少数场景），降级为软件 Stack Blur。
 *
 * | 场景             | 技术                              | 质量     |
 * |-----------------|----------------------------------|---------|
 * | 默认（硬件加速）  | [RenderEffect.createBlurEffect]  | GPU 高斯 |
 * | 软件 Canvas 降级  | 软件 Stack Blur（水平+垂直两遍）   | 近似高斯 |
 *
 * ## Bitmap 对象池
 *
 * 毛玻璃卡片在滚动时每帧都需要截取和模糊 Bitmap，频繁创建/回收会造成 GC 抖动。
 * 内置 Bitmap 对象池（最大 [POOL_MAX_SIZE] 个），复用同尺寸 Bitmap 消除 GC 压力。
 *
 * - [obtainBitmap]：从池中获取或创建新 Bitmap
 * - [recycleToPool]：将 Bitmap 归还池中复用，池满时真正回收
 *
 * @see MorphCardView 毛玻璃卡片容器
 */
internal object BackdropBlurHelper {

    /** 复用 RenderNode，避免每次 blur() 调用都新建对象 */
    private val blurRenderNode = RenderNode("morphBlur")

    /** 临时数组缓存，供 stackBlurHorizontal / stackBlurVertical 复用 */
    private var blurTempArray: IntArray? = null

    // ═══════════════════════════════════════════════════════════════════════
    // Bitmap 对象池 — 滚动场景下复用 Bitmap 减少 GC
    // ═══════════════════════════════════════════════════════════════════════

    /** 对象池最大容量 */
    private const val POOL_MAX_SIZE = 3

    /** 池中缓存的 Bitmap，按尺寸复用（所有访问通过 synchronized 保护） */
    private val bitmapPool = ArrayDeque<Bitmap>(POOL_MAX_SIZE)

    /**
     * 从池中获取指定尺寸的 Bitmap，池中无匹配时创建新 Bitmap。
     *
     * @param width  宽度（px）
     * @param height 高度（px）
     * @return 可用的 Bitmap（已擦除为透明）
     */
    @Synchronized
    fun obtainBitmap(width: Int, height: Int): Bitmap {
        // 从池中查找同尺寸 Bitmap
        val iterator = bitmapPool.iterator()
        while (iterator.hasNext()) {
            val bitmap = iterator.next()
            if (!bitmap.isRecycled && bitmap.width == width && bitmap.height == height) {
                iterator.remove()
                bitmap.eraseColor(0) // 清除旧像素
                return bitmap
            }
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    /**
     * 将 Bitmap 归还池中复用。池满时真正回收最旧的 Bitmap。
     *
     * @param bitmap 要归还的 Bitmap
     */
    @Synchronized
    fun recycleToPool(bitmap: Bitmap) {
        if (bitmap.isRecycled) return
        if (bitmapPool.size >= POOL_MAX_SIZE) {
            // 池满，回收最旧的 Bitmap
            val oldest = bitmapPool.removeFirst()
            oldest.recycle()
        }
        bitmapPool.addLast(bitmap)
    }

    /**
     * 响应系统内存压力，清空对象池中所有缓存的 Bitmap。
     *
     * 应在 [android.app.Application.onTrimMemory] 或
     * [android.content.ComponentCallbacks2.onTrimMemory] 回调中调用，
     * 当 level >= [android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE] 时触发。
     *
     * 典型接入方式：
     * ```kotlin
     * override fun onTrimMemory(level: Int) {
     *     super.onTrimMemory(level)
     *     if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
     *         BackdropBlurHelper.clearPool()
     *     }
     * }
     * ```
     */
    @Synchronized
    fun clearPool() {
        while (bitmapPool.isNotEmpty()) {
            bitmapPool.removeFirst().recycle()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 父容器截图
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 截取 [view] 在父容器中所占区域的像素。
     *
     * 原理：临时将 [view] 设为 [View.INVISIBLE]（不参与绘制但仍占布局空间），
     * 然后在父容器的 Canvas 上绘制，绘制完成后恢复 [View.VISIBLE]。
     * 这样截取的 Bitmap 只包含卡片**背后**的内容，不含卡片自身。
     *
     * @param view 要截取背景的 View（必须已 attach 且 parent 为 [ViewGroup]）
     * @return 父容器在 View 区域的像素快照，失败返回 null
     */
    @androidx.annotation.MainThread
    fun captureParentArea(view: View): Bitmap? {
        val parent = view.parent as? ViewGroup ?: return null
        if (parent.width <= 0 || parent.height <= 0) return null
        if (view.width <= 0 || view.height <= 0) return null

        // 临时隐藏卡片，使 parent.draw() 不包含卡片自身
        val originalVisibility = view.visibility
        view.visibility = View.INVISIBLE

        val bitmap = obtainBitmap(view.width, view.height)
        try {
            val canvas = Canvas(bitmap)
            // 将父容器绘制偏移到 View 在父容器中的位置
            canvas.translate(-view.left.toFloat(), -view.top.toFloat())
            parent.draw(canvas)
            view.visibility = originalVisibility
            return bitmap
        } catch (e: Exception) {
            // parent.draw() 在某些场景下可能失败（如硬件加速限制）
            Log.d("MorphKit", "captureParentArea: parent.draw() 失败，返回 null", e)
            view.visibility = originalVisibility
            recycleToPool(bitmap)
            return null
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 模糊处理
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 对 [source] Bitmap 应用高斯模糊。
     *
     * minSdk=35 保证 [RenderEffect.createBlurEffect] 始终可用，
     * 通过 [RenderNode] + GPU 加速实现高质量高斯模糊。
     * 当硬件加速不可用时，降级为软件 Stack Blur。
     *
     * @param source 源 Bitmap
     * @param radius 模糊半径（px），推荐 15–35
     * @return 模糊后的新 Bitmap，失败返回 null
     */
    @androidx.annotation.MainThread
    fun blur(source: Bitmap, radius: Float): Bitmap? {
        if (source.width <= 0 || source.height <= 0) return null

        return try {
            val w = source.width
            val h = source.height
            val output = obtainBitmap(w, h)

            val node = blurRenderNode
            node.setPosition(0, 0, w, h)

            val blurEffect = RenderEffect.createBlurEffect(
                radius, radius, Shader.TileMode.CLAMP
            )
            node.setRenderEffect(blurEffect)

            val canvas = node.beginRecording(w, h)
            canvas.drawBitmap(source, 0f, 0f, null)
            node.endRecording()

            val outputCanvas = Canvas(output)
            if (outputCanvas.isHardwareAccelerated) {
                outputCanvas.drawRenderNode(node)
                node.discardDisplayList()
                output
            } else {
                // 极少数场景：软件 Canvas 不支持 drawRenderNode，降级为 Stack Blur
                recycleToPool(output)
                node.discardDisplayList()
                blurSoftware(source, radius.toInt().coerceAtLeast(1))
            }
        } catch (e: Exception) {
            // RenderEffect 可能因硬件加速关闭等原因抛异常，降级为软件模糊
            Log.d("MorphKit", "GPU 模糊失败，降级为软件 Stack Blur: ${e.javaClass.simpleName}")
            blurSoftware(source, radius.toInt().coerceAtLeast(1))
        }
    }

    // ── 软件 Stack Blur 降级路径 ──

    /**
     * 软件 Stack Blur 实现。
     *
     * 使用水平+垂直两遍均值滤波（moving average）近似高斯模糊。
     * 时间复杂度 O(w × h)，与半径无关，适合大半径模糊。
     *
     * @param source 源 Bitmap（不会被修改）
     * @param radius 模糊半径（px），至少 1
     * @return 模糊后的新 Bitmap
     */
    private fun blurSoftware(source: Bitmap, radius: Int): Bitmap {
        val w = source.width
        val h = source.height
        val result = source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)

        if (radius < 1) return result

        val pixels = pixelBuffer.getOrSet(w * h)
        result.getPixels(pixels, 0, w, 0, 0, w, h)

        // 水平方向均值滤波
        stackBlurHorizontal(pixels, w, h, radius)
        // 垂直方向均值滤波
        stackBlurVertical(pixels, w, h, radius)

        result.setPixels(pixels, 0, w, 0, 0, w, h)
        return result
    }

    /**
     * 像素缓冲区 — 复用 IntArray 减少 GC 压力。
     *
     * 软件 Stack Blur 每次需要 `IntArray(w * h)` 临时数组，
     * 大图场景下频繁分配/回收造成 GC 抖动。
     * 通过缓存最近使用的数组消除此问题，支持多尺寸场景。
     */
    private object pixelBuffer {
        private const val MAX_CACHE_SIZE = 2

        @Volatile
        private var cached: IntArray? = null
        private var cachedSize: Int = 0

        // 二级缓存：保存上一次被替换的数组，支持两种尺寸交替使用
        @Volatile
        private var secondary: IntArray? = null
        private var secondarySize: Int = 0

        /**
         * 获取指定容量的 IntArray。
         * 若主缓存或二级缓存的数组容量 >= [size] 则复用，
         * 否则创建新数组并将旧数组降级到二级缓存。
         */
        @Synchronized
        fun getOrSet(size: Int): IntArray {
            val existing = cached
            if (existing != null && existing.size >= size) {
                return existing
            }
            // 主缓存不够大，检查二级缓存
            val sec = secondary
            if (sec != null && sec.size >= size) {
                // 二级缓存满足需求，交换主/次
                secondary = cached
                secondarySize = cachedSize
                cached = sec
                cachedSize = secondarySize
                return sec
            }
            // 都不满足，创建新数组
            val newBuffer = IntArray(size)
            // 旧主缓存降级到二级
            secondary = cached
            secondarySize = cachedSize
            cached = newBuffer
            cachedSize = size
            return newBuffer
        }
    }

    /**
     * 水平方向 Stack Blur（均值滤波）。
     *
     * 对每一行从左到右滑动窗口求均值，利用滑动窗口增量更新避免重复求和。
     */
    private fun stackBlurHorizontal(pixels: IntArray, w: Int, h: Int, radius: Int) {
        val div = radius + radius + 1
        val divSum = div.toLong()
        val temp = blurTempArray?.takeIf { it.size >= w } ?: IntArray(w).also { blurTempArray = it }

        for (y in 0 until h) {
            var rSum = 0L; var gSum = 0L; var bSum = 0L; var aSum = 0L
            val rowOffset = y * w

            // 初始化窗口：左边缘镜像填充
            for (i in -radius..radius) {
                val px = pixels[rowOffset + i.coerceIn(0, w - 1)]
                aSum += (px ushr 24) and 0xFF
                rSum += (px ushr 16) and 0xFF
                gSum += (px ushr 8) and 0xFF
                bSum += px and 0xFF
            }

            for (x in 0 until w) {
                temp[x] = ((aSum / divSum shl 24) or
                        (rSum / divSum shl 16) or
                        (gSum / divSum shl 8) or
                        (bSum / divSum)).toInt()

                // 滑动窗口：移除左侧像素，加入右侧像素
                val removeIdx = rowOffset + (x - radius).coerceIn(0, w - 1)
                val addIdx = rowOffset + (x + radius + 1).coerceIn(0, w - 1)
                val removePx = pixels[removeIdx]
                val addPx = pixels[addIdx]
                aSum += ((addPx ushr 24) and 0xFF) - ((removePx ushr 24) and 0xFF)
                rSum += ((addPx ushr 16) and 0xFF) - ((removePx ushr 16) and 0xFF)
                gSum += ((addPx ushr 8) and 0xFF) - ((removePx ushr 8) and 0xFF)
                bSum += (addPx and 0xFF) - (removePx and 0xFF)
            }

            // 写回
            System.arraycopy(temp, 0, pixels, rowOffset, w)
        }
    }

    /**
     * 垂直方向 Stack Blur（均值滤波）。
     *
     * 对每一列从上到下滑动窗口求均值。
     */
    private fun stackBlurVertical(pixels: IntArray, w: Int, h: Int, radius: Int) {
        val div = radius + radius + 1
        val divSum = div.toLong()
        val temp = blurTempArray?.takeIf { it.size >= h } ?: IntArray(h).also { blurTempArray = it }

        for (x in 0 until w) {
            var rSum = 0L; var gSum = 0L; var bSum = 0L; var aSum = 0L

            // 初始化窗口：上边缘镜像填充
            for (i in -radius..radius) {
                val px = pixels[i.coerceIn(0, h - 1) * w + x]
                aSum += (px ushr 24) and 0xFF
                rSum += (px ushr 16) and 0xFF
                gSum += (px ushr 8) and 0xFF
                bSum += px and 0xFF
            }

            for (y in 0 until h) {
                temp[y] = ((aSum / divSum shl 24) or
                        (rSum / divSum shl 16) or
                        (gSum / divSum shl 8) or
                        (bSum / divSum)).toInt()

                val removeIdx = (y - radius).coerceIn(0, h - 1) * w + x
                val addIdx = (y + radius + 1).coerceIn(0, h - 1) * w + x
                val removePx = pixels[removeIdx]
                val addPx = pixels[addIdx]
                aSum += ((addPx ushr 24) and 0xFF) - ((removePx ushr 24) and 0xFF)
                rSum += ((addPx ushr 16) and 0xFF) - ((removePx ushr 16) and 0xFF)
                gSum += ((addPx ushr 8) and 0xFF) - ((removePx ushr 8) and 0xFF)
                bSum += (addPx and 0xFF) - (removePx and 0xFF)
            }

            // 写回
            for (y in 0 until h) {
                pixels[y * w + x] = temp[y]
            }
        }
    }
}
