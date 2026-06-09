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
        // S14: verify new M3 semantic color tokens
        assertThat(MorphTokens.colorSecondary).isNotEqualTo(0)
        assertThat(MorphTokens.colorTertiary).isNotEqualTo(0)
        assertThat(MorphTokens.colorOnError).isNotEqualTo(0)
        assertThat(MorphTokens.colorErrorContainer).isNotEqualTo(0)
        assertThat(MorphTokens.colorOutline).isNotEqualTo(0)
        assertThat(MorphTokens.colorSurfaceDim).isNotEqualTo(0)
        assertThat(MorphTokens.colorSurfaceContainer).isNotEqualTo(0)
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
            // Primary
            "primary", "onPrimary", "primaryContainer", "onPrimaryContainer",
            // Secondary
            "secondary", "onSecondary", "secondaryContainer", "onSecondaryContainer",
            // Tertiary
            "tertiary", "onTertiary", "tertiaryContainer", "onTertiaryContainer",
            // Error
            "error", "onError", "errorContainer", "onErrorContainer",
            // Surface
            "surface", "onSurface", "surfaceVariant", "onSurfaceVariant",
            "surfaceDim", "surfaceBright",
            "surfaceContainerLowest", "surfaceContainerLow", "surfaceContainer",
            "surfaceContainerHigh", "surfaceContainerHighest",
            // Outline
            "outline", "outlineVariant",
            // Background
            "background", "onBackground",
            // Inverse
            "inverseSurface", "inverseOnSurface", "inversePrimary",
            // Misc
            "scrim", "success", "warning"
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
        val classResource = javaClass.classLoader?.getResourceAsStream(
            "com/morphkit/theme/compose/MorphComposeThemeKt.class"
        )
        assertThat(classResource).isNotNull()

        val classBytes = classResource!!.readBytes()
        val utf8Strings = extractUtf8Constants(classBytes)

        assertThat(utf8Strings).contains("LocalMorphColors")
        assertThat(utf8Strings).contains("LocalMorphShape")
        assertThat(utf8Strings).contains("LocalMorphInteractionMode")
        assertThat(utf8Strings).contains("LocalMorphStyle")
    }

    private fun extractUtf8Constants(classBytes: ByteArray): Set<String> {
        val constants = mutableSetOf<String>()
        var offset = 8
        val cpCount = ((classBytes[offset].toInt() and 0xFF) shl 8) or
                (classBytes[offset + 1].toInt() and 0xFF)
        offset += 2

        var i = 1
        while (i < cpCount) {
            val tag = classBytes[offset].toInt() and 0xFF
            offset++
            when (tag) {
                1 -> {
                    val len = ((classBytes[offset].toInt() and 0xFF) shl 8) or
                            (classBytes[offset + 1].toInt() and 0xFF)
                    offset += 2
                    constants.add(String(classBytes, offset, len, Charsets.UTF_8))
                    offset += len
                }
                7 -> offset += 2
                8 -> offset += 2
                3 -> offset += 4
                4 -> offset += 4
                5 -> { offset += 8; i++ }
                6 -> { offset += 8; i++ }
                9 -> offset += 4
                10 -> offset += 4
                11 -> offset += 4
                12 -> offset += 4
                15 -> offset += 3
                16 -> offset += 2
                17 -> offset += 4
                18 -> offset += 4
                19 -> offset += 2
                20 -> offset += 2
                else -> break
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
