package com.morphkit.theme.compose

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.morphkit.theme.MorphTokens
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * JVM unit tests for the MorphKit Compose theme module.
 *
 * Uses reflection to verify class structure, enum values, data class fields,
 * and CompositionLocal provider existence — all without requiring a running
 * Compose runtime.
 */
class MorphComposeThemeTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
    }

    // ── 1. Class existence & correct package ──────────────────────────────

    @Test
    fun morphStyle_enumExistsInCorrectPackage() {
        val clazz = Class.forName("com.morphkit.theme.compose.MorphStyle")
        assertThat(clazz.`package`?.name).isEqualTo("com.morphkit.theme.compose")
        assertThat(clazz.isEnum).isTrue()
    }

    @Test
    fun morphInteractionMode_enumExistsWithCorrectValues() {
        val clazz = Class.forName("com.morphkit.theme.compose.MorphInteractionMode")
        assertThat(clazz.`package`?.name).isEqualTo("com.morphkit.theme.compose")
        assertThat(clazz.isEnum).isTrue()

        val enumConstants = clazz.enumConstants?.map { (it as Enum<*>).name } ?: emptyList()
        assertThat(enumConstants).containsExactly("IOS", "MATERIAL")
    }

    // ── 2. MorphTokens constants accessible from Compose ──────────────────

    @Test
    fun morphTokens_colorConstantsAreAccessible() {
        // Verify key color tokens used by MorphColorPalette are present and non-zero
        assertThat(MorphTokens.colorBlue500).isNotEqualTo(0)
        assertThat(MorphTokens.colorOnPrimary).isNotEqualTo(0)
        assertThat(MorphTokens.colorSurface).isNotEqualTo(0)
        assertThat(MorphTokens.colorRed500).isNotEqualTo(0)
        assertThat(MorphTokens.colorGreen500).isNotEqualTo(0)
        assertThat(MorphTokens.colorOrange500).isNotEqualTo(0)
    }

    @Test
    fun morphTokens_cornerRadiusConstantsAreAccessible() {
        // Verify shape tokens used by MorphShape are present
        assertThat(MorphTokens.cornerRadiusButtonIos).isEqualTo(12)
        assertThat(MorphTokens.cornerRadiusButtonPixel).isEqualTo(8)
        assertThat(MorphTokens.cornerRadiusCardIos).isEqualTo(16)
        assertThat(MorphTokens.cornerRadiusSmall).isEqualTo(8)
        assertThat(MorphTokens.cornerRadiusMedium).isEqualTo(12)
        assertThat(MorphTokens.cornerRadiusLarge).isEqualTo(16)
    }

    // ── 3. Data class field verification ──────────────────────────────────

    @Test
    fun morphColorPalette_hasAllExpectedFields() {
        val clazz = Class.forName("com.morphkit.theme.compose.MorphColorPalette")
        val expectedFields = setOf(
            "primary", "onPrimary", "primaryContainer", "onPrimaryContainer",
            "surface", "surfaceVariant", "onSurface", "onSurfaceVariant",
            "outlineVariant", "background", "onBackground", "error",
            "success", "warning"
        )
        val actualFields = clazz.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) }
            .map { it.name }
            .toSet()

        assertThat(actualFields).containsAtLeastElementsIn(expectedFields)
    }

    @Test
    fun morphShape_hasAllExpectedFields() {
        val clazz = Class.forName("com.morphkit.theme.compose.MorphShape")
        val expectedFields = setOf(
            "cornerRadiusButton", "cornerRadiusCard", "cornerRadiusTextField",
            "cornerRadiusSmall", "cornerRadiusMedium", "cornerRadiusLarge"
        )
        val actualFields = clazz.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) }
            .map { it.name }
            .toSet()

        assertThat(actualFields).containsAtLeastElementsIn(expectedFields)
    }

    // ── 4. CompositionLocal providers exist ───────────────────────────────

    @Test
    fun compositionLocalProviders_existAsTopLevelFields() {
        // LocalMorphColors, LocalMorphShape, LocalMorphInteractionMode, LocalMorphStyle
        // are top-level vals in the package. Since MorphComposeThemeKt has static
        // initializers that call staticCompositionLocalOf (requiring Compose runtime),
        // we cannot load the class. Instead, read the compiled class file and check
        // that the field names exist in its constant pool (UTF-8 entries).
        val classResource = javaClass.classLoader?.getResourceAsStream(
            "com/morphkit/theme/compose/MorphComposeThemeKt.class"
        )
        assertThat(classResource).isNotNull()

        val classBytes = classResource!!.readBytes()
        // Field names appear as UTF-8 constants in the class file constant pool.
        // Check for the presence of each CompositionLocal provider name.
        val utf8Strings = extractUtf8Constants(classBytes)

        assertThat(utf8Strings).contains("LocalMorphColors")
        assertThat(utf8Strings).contains("LocalMorphShape")
        assertThat(utf8Strings).contains("LocalMorphInteractionMode")
        assertThat(utf8Strings).contains("LocalMorphStyle")
    }

    /**
     * Extracts UTF-8 constant pool entries from a .class file.
     * Format: constant_pool_count (u2), then constant_pool entries.
     * Tag 1 = CONSTANT_Utf8, followed by length (u2) and bytes.
     */
    private fun extractUtf8Constants(classBytes: ByteArray): Set<String> {
        val constants = mutableSetOf<String>()
        // Skip magic (4), minor_version (2), major_version (2)
        var offset = 8
        val cpCount = ((classBytes[offset].toInt() and 0xFF) shl 8) or
                (classBytes[offset + 1].toInt() and 0xFF)
        offset += 2

        var i = 1 // constant_pool is 1-indexed
        while (i < cpCount) {
            val tag = classBytes[offset].toInt() and 0xFF
            offset++
            when (tag) {
                1 -> { // CONSTANT_Utf8
                    val len = ((classBytes[offset].toInt() and 0xFF) shl 8) or
                            (classBytes[offset + 1].toInt() and 0xFF)
                    offset += 2
                    constants.add(String(classBytes, offset, len, Charsets.UTF_8))
                    offset += len
                }
                7 -> offset += 2 // CONSTANT_Class: name_index (u2)
                8 -> offset += 2 // CONSTANT_String: string_index (u2)
                3 -> offset += 4 // CONSTANT_Integer: bytes (u4)
                4 -> offset += 4 // CONSTANT_Float: bytes (u4)
                5 -> { offset += 8; i++ } // CONSTANT_Long: takes 2 slots
                6 -> { offset += 8; i++ } // CONSTANT_Double: takes 2 slots
                9 -> offset += 4 // CONSTANT_Fieldref: class_index + name_and_type_index
                10 -> offset += 4 // CONSTANT_Methodref
                11 -> offset += 4 // CONSTANT_InterfaceMethodref
                12 -> offset += 4 // CONSTANT_NameAndType
                15 -> offset += 3 // CONSTANT_MethodHandle
                16 -> offset += 2 // CONSTANT_MethodType
                17 -> offset += 4 // CONSTANT_Dynamic
                18 -> offset += 4 // CONSTANT_InvokeDynamic
                19 -> offset += 2 // CONSTANT_Module
                20 -> offset += 2 // CONSTANT_Package
                else -> break // Unknown tag, stop parsing
            }
            i++
        }
        return constants
    }

    // ── 5. MorphStyle enum values ─────────────────────────────────────────

    @Test
    fun morphStyle_hasAutoIosPixelValues() {
        val clazz = Class.forName("com.morphkit.theme.compose.MorphStyle")
        val enumConstants = clazz.enumConstants?.map { (it as Enum<*>).name } ?: emptyList()

        assertThat(enumConstants).containsExactly("Auto", "iOS", "Pixel")
    }
}
