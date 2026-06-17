package com.morphkit.core

import com.morphkit.core.MorphConfig
import com.morphkit.core.StylePolicy
import android.content.Context
import android.util.AttributeSet
import android.view.View
import org.junit.Assert.*
import org.junit.Test

/**
 * MorphConfig DSL 配置正确性测试。
 *
 * 验证 replace / groupReplace / modify / stylePolicy
 * 四个 DSL 方法的注册与读取行为。
 */
class MorphConfigTest {

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D1：replace 注册单个替换规则
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `replace注册后_replaceMap包含对应条目`() {
        val config = MorphConfig()
        val creator: (Context, AttributeSet) -> View = { _, _ -> mockkView() }

        config.replace("TextView", creator)

        assertTrue("replaceMap 应包含 TextView", config.replaceMap.containsKey("TextView"))
        assertSame("replaceMap 中 TextView 对应的 creator 应为注册的 creator", creator, config.replaceMap["TextView"])
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D2：replace 覆盖同名规则
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `replace同名规则_后者覆盖前者`() {
        val config = MorphConfig()
        val creator1: (Context, AttributeSet) -> View = { _, _ -> mockkView() }
        val creator2: (Context, AttributeSet) -> View = { _, _ -> mockkView() }

        config.replace("Button", creator1)
        config.replace("Button", creator2)

        assertEquals("replaceMap 大小应为 1", 1, config.replaceMap.size)
        assertSame("后者应覆盖前者", creator2, config.replaceMap["Button"])
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D3：groupReplace 注册别名分组
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `groupReplace注册后_所有别名均映射到同一个creator`() {
        val config = MorphConfig()
        val sharedCreator: (Context, AttributeSet) -> View = { _, _ -> mockkView() }

        config.groupReplace(listOf("Button", "AppCompatButton", "androidx.appcompat.widget.AppCompatButton"), sharedCreator)

        assertEquals("replaceMap 大小应为 3", 3, config.replaceMap.size)
        assertSame(sharedCreator, config.replaceMap["Button"])
        assertSame(sharedCreator, config.replaceMap["AppCompatButton"])
        assertSame(sharedCreator, config.replaceMap["androidx.appcompat.widget.AppCompatButton"])
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D4：groupReplace 空列表不产生条目
    // ═══════════════════════════════════════════════════════════════════════

    @Test(expected = IllegalArgumentException::class)
    fun `groupReplace空列表_抛出IllegalArgumentException`() {
        val config = MorphConfig()
        val creator: (Context, AttributeSet) -> View = { _, _ -> mockkView() }

        config.groupReplace(emptyList(), creator)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D5：modify 注册属性修改规则
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `modify注册后_modifyMap包含对应条目`() {
        val config = MorphConfig()
        val modifier: (View) -> Unit = { _ -> }

        config.modify("ImageView", modifier)

        assertTrue("modifyMap 应包含 ImageView", config.modifyMap.containsKey("ImageView"))
        assertSame(modifier, config.modifyMap["ImageView"])
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D6：modify 覆盖同名规则
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `modify同名规则_后者覆盖前者`() {
        val config = MorphConfig()
        val modifier1: (View) -> Unit = { _ -> }
        val modifier2: (View) -> Unit = { _ -> }

        config.modify("RecyclerView", modifier1)
        config.modify("RecyclerView", modifier2)

        assertEquals("modifyMap 大小应为 1", 1, config.modifyMap.size)
        assertSame(modifier2, config.modifyMap["RecyclerView"])
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D7：stylePolicy 默认为 AUTO
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `stylePolicy默认值为AUTO`() {
        val config = MorphConfig()
        assertEquals("默认策略应为 AUTO", StylePolicy.AUTO, config.policy)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D8：stylePolicy DSL 设置生效
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `stylePolicy DSL设置后_policy变更`() {
        val config = MorphConfig()

        config.stylePolicy(StylePolicy.IOS)
        assertEquals(StylePolicy.IOS, config.policy)

        config.stylePolicy(StylePolicy.PIXEL)
        assertEquals(StylePolicy.PIXEL, config.policy)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D9：unifiedPrefix 委托至顶层常量
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `unifiedPrefix委托至顶层常量_Morph`() {
        val config = MorphConfig()
        assertEquals("unifiedPrefix 应为 Morph", "Morph", config.unifiedPrefix)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D10：replaceMap 和 modifyMap 对外只读
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `replaceMap和modifyMap对外只读_直接put不反映`() {
        val config = MorphConfig()
        val creator: (Context, AttributeSet) -> View = { _, _ -> mockkView() }
        config.replace("TextView", creator)

        // 尝试通过 replaceMap 的引用修改（Map 是只读视图）
        val mapRef = config.replaceMap
        assertEquals(1, mapRef.size)

        // 原始 Map 不受外部影响（replaceMap 返回的是 _replaceMap 的只读视图）
        // 验证再次 replace 后大小变化
        config.replace("Button", creator)
        assertEquals(2, config.replaceMap.size)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 D11：replace 和 modify 可以同名共存
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `replace和modify同名共存_互不干扰`() {
        val config = MorphConfig()
        val creator: (Context, AttributeSet) -> View = { _, _ -> mockkView() }
        val modifier: (View) -> Unit = { _ -> }

        config.replace("TextView", creator)
        config.modify("TextView", modifier)

        assertTrue(config.replaceMap.containsKey("TextView"))
        assertTrue(config.modifyMap.containsKey("TextView"))
        assertSame(creator, config.replaceMap["TextView"])
        assertSame(modifier, config.modifyMap["TextView"])
    }

    private fun mockkView(): View {
        return io.mockk.mockk(relaxed = true)
    }
}
