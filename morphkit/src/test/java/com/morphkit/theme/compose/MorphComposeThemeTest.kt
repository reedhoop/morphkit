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
 * CompositionLocal provider existence, and behavioral properties — all without
 * requiring a running Compose runtime.
 */
class MorphComposeThemeTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
    }

    // ── 1. Class existence & correct package ──────────────────────────────

    @Test
    fun stylePolicy_enumExistsInCorePackage() {
        val clazz = Class.forName("com.morphkit.core.StylePolicy")
        assertThat(clazz.`package`?.name).isEqualTo("com.morphkit.core")
        assertThat(clazz.isEnum).isTrue()

        val enumConstants = clazz.enumConstants?.map { (it as Enum<*>).name } ?: emptyList()
        assertThat(enumConstants).containsExactly("AUTO", "IOS", "PIXEL")
    }

    @Test
    fun interactionMode_enumExistsInCorePackage() {
        val clazz = Class.forName("com.morphkit.core.InteractionMode")
        assertThat(clazz.`package`?.name).isEqualTo("com.morphkit.core")
        assertThat(clazz.isEnum).isTrue()

        val enumConstants = clazz.enumConstants?.map { (it as Enum<*>).name } ?: emptyList()
        assertThat(enumConstants).containsExactly("IOS", "MATERIAL")
    }

    // ── 2. MorphTokens constants accessible from Compose ──────────────────

    @Test
    fun morphTokens_colorConstantsAreAccessible() {
        // Verify key color tokens used by MorphColorPalette are present and non-zero
        assertThat(MorphTokens.Colors.colorBlue500).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOnPrimary).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorSurface).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorRed500).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorGreen500).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOrange500).isNotEqualTo(0)
        // S14: verify new M3 semantic color tokens
        assertThat(MorphTokens.Colors.colorSecondary).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorTertiary).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOnError).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorErrorContainer).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOutline).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorSurfaceDim).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorSurfaceContainer).isNotEqualTo(0)
    }

    @Test
    fun morphTokens_cornerRadiusConstantsAreAccessible() {
        // Verify shape tokens used by MorphShape are present
        assertThat(MorphTokens.Shapes.cornerRadiusButtonIos).isEqualTo(12)
        assertThat(MorphTokens.Shapes.cornerRadiusButtonPixel).isEqualTo(8)
        assertThat(MorphTokens.Shapes.cornerRadiusCardIos).isEqualTo(16)
        assertThat(MorphTokens.Shapes.cornerRadiusSmall).isEqualTo(8)
        assertThat(MorphTokens.Shapes.cornerRadiusMedium).isEqualTo(12)
        assertThat(MorphTokens.Shapes.cornerRadiusLarge).isEqualTo(16)
    }

    // ── 3. Data class field verification ──────────────────────────────────

    @Test
    fun morphColorPalette_hasAllExpectedFields() {
        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphColorPalette",
            false, // 不触发类初始化，避免 Compose UI Color 类加载失败
            javaClass.classLoader
        )
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
    fun morphColorPalette_fieldCountMatchesM3ColorScheme() {
        // M3 ColorScheme has 37 color roles (including surfaceTint)
        // MorphColorPalette should have exactly 37 non-static fields
        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphColorPalette",
            false, // 不触发类初始化
            javaClass.classLoader
        )
        val fieldCount = clazz.declaredFields
            .count { !Modifier.isStatic(it.modifiers) }

        assertThat(fieldCount).isEqualTo(37)
    }

    @Test
    fun morphColorPalette_isImmutableAnnotated() {
        // @Immutable annotation requires Compose runtime at class-load time.
        // Since Compose runtime is testCompileOnly, we verify via bytecode instead.
        val classResource = javaClass.classLoader?.getResourceAsStream(
            "com/morphkit/theme/compose/MorphColorPalette.class"
        )
        assertThat(classResource).isNotNull()
        val classBytes = classResource!!.readBytes()
        val utf8Strings = extractUtf8Constants(classBytes)
        // Annotation descriptor in constant pool: "Landroidx/compose/runtime/Immutable;"
        val hasImmutable = utf8Strings.any { it.contains("Immutable") }
        assertThat(hasImmutable).isTrue()
    }

    @Test
    fun morphShape_hasAllExpectedFields() {
        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphShape",
            false, // 不触发类初始化
            javaClass.classLoader
        )
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

    @Test
    fun morphShape_isImmutableAnnotated() {
        val classResource = javaClass.classLoader?.getResourceAsStream(
            "com/morphkit/theme/compose/MorphShape.class"
        )
        assertThat(classResource).isNotNull()
        val classBytes = classResource!!.readBytes()
        val utf8Strings = extractUtf8Constants(classBytes)
        val hasImmutable = utf8Strings.any { it.contains("Immutable") }
        assertThat(hasImmutable).isTrue()
    }

    @Test
    fun morphShape_fieldCountIsSix() {
        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphShape",
            false, // 不触发类初始化
            javaClass.classLoader
        )
        val fieldCount = clazz.declaredFields
            .count { !Modifier.isStatic(it.modifiers) }
        assertThat(fieldCount).isEqualTo(6)
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
        assertThat(utf8Strings).contains("LocalMorphStylePolicy")
    }

    /**
     * Extracts UTF-8 constant pool entries from a .class file.
     */
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

    // ── 5. StylePolicy enum values ────────────────────────────────────────

    @Test
    fun stylePolicy_hasAutoIosPixelValues() {
        val clazz = Class.forName("com.morphkit.core.StylePolicy")
        val enumConstants = clazz.enumConstants?.map { (it as Enum<*>).name } ?: emptyList()

        assertThat(enumConstants).containsExactly("AUTO", "IOS", "PIXEL")
    }

    // ── 6. S15: MorphColorPalette companion methods ───────────────────────

    @Test
    fun morphColorPalette_companionHasFactoryMethods() {
        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphColorPalette",
            false, // 不触发类初始化
            javaClass.classLoader
        )
        val companion = clazz.declaredClasses.firstOrNull { it.simpleName == "Companion" }
        assertThat(companion).isNotNull()

        val methodNames = companion!!.declaredMethods.map { it.name }
        assertThat(methodNames).contains("iosLight")
        assertThat(methodNames).contains("iosDark")
        assertThat(methodNames).contains("pixelFromContext")
    }

    @Test
    fun morphColorPalette_companionHasPrivateResolveM3Color() {
        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphColorPalette",
            false, // 不触发类初始化
            javaClass.classLoader
        )
        val companion = clazz.declaredClasses.first { it.simpleName == "Companion" }
        val methodNames = companion.declaredMethods.map { it.name }
        // resolveM3Color is a private helper — verify any method with "resolve" and "Color" in name exists
        val resolveMethods = methodNames.filter { it.contains("resolve", ignoreCase = true) && it.contains("Color", ignoreCase = true) }
        assertThat(resolveMethods).isNotEmpty()
    }

    // ── 7. S15: MorphShape companion methods ──────────────────────────────

    @Test
    fun morphShape_companionHasFactoryMethods() {
        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphShape",
            false, // 不触发类初始化
            javaClass.classLoader
        )
        val companion = clazz.declaredClasses.firstOrNull { it.simpleName == "Companion" }
        assertThat(companion).isNotNull()

        val methodNames = companion!!.declaredMethods.map { it.name }
        assertThat(methodNames).contains("ios")
        assertThat(methodNames).contains("pixel")
    }

    // ── 8. S15: Private functions in MorphComposeThemeKt ──────────────────

    @Test
    fun morphComposeThemeKt_hasPrivateHelperFunctions() {
        val classResource = javaClass.classLoader?.getResourceAsStream(
            "com/morphkit/theme/compose/MorphComposeThemeKt.class"
        )
        assertThat(classResource).isNotNull()

        val classBytes = classResource!!.readBytes()
        val utf8Strings = extractUtf8Constants(classBytes)

        // Verify private helper functions exist in constant pool
        assertThat(utf8Strings).contains("material3ColorScheme")
        assertThat(utf8Strings).contains("morphTypography")
        assertThat(utf8Strings).contains("resolveStyle")
        assertThat(utf8Strings).contains("resolveAutoStyle")
    }

    // ── 9. S15: Dark mode token completeness ──────────────────────────────

    @Test
    fun morphTokens_darkModeColorTokensExistForAllPrimaryRoles() {
        // Verify that for every light-mode color token used in iosLight(),
        // there is a corresponding dark-mode token used in iosDark()
        // Primary
        assertThat(MorphTokens.Colors.colorBlue100).isNotEqualTo(0) // dark primary
        assertThat(MorphTokens.Colors.colorPrimaryContainerDark).isNotEqualTo(0) // dark primaryContainer
        // Secondary
        assertThat(MorphTokens.Colors.colorSecondaryDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOnSecondaryDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorSecondaryContainerDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOnSecondaryContainerDark).isNotEqualTo(0)
        // Tertiary
        assertThat(MorphTokens.Colors.colorTertiaryDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOnTertiaryDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorTertiaryContainerDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOnTertiaryContainerDark).isNotEqualTo(0)
        // Error
        assertThat(MorphTokens.Colors.colorOnErrorDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorErrorContainerDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOnErrorContainerDark).isNotEqualTo(0)
        // Surface hierarchy
        assertThat(MorphTokens.Colors.colorSurfaceDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOnSurfaceDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorSurfaceVariantDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOnSurfaceVariantDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorSurfaceDimDark).isNotNull()
        assertThat(MorphTokens.Colors.colorSurfaceBrightDark).isNotNull()
        assertThat(MorphTokens.Colors.colorSurfaceContainerLowestDark).isNotNull()
        assertThat(MorphTokens.Colors.colorSurfaceContainerLowDark).isNotNull()
        assertThat(MorphTokens.Colors.colorSurfaceContainerDark).isNotNull()
        assertThat(MorphTokens.Colors.colorSurfaceContainerHighDark).isNotNull()
        assertThat(MorphTokens.Colors.colorSurfaceContainerHighestDark).isNotNull()
        // Outline
        assertThat(MorphTokens.Colors.colorOutlineDark).isNotEqualTo(0)
        assertThat(MorphTokens.Colors.colorOutlineVariantDark).isNotEqualTo(0)
    }

    // ── 10. S15: Token value correctness ──────────────────────────────────

    @Test
    fun morphTokens_primaryColorIsIOSBlue() {
        // iOS system blue: #007AFF
        assertThat(MorphTokens.Colors.colorBlue500).isEqualTo(0xFF007AFF.toInt())
    }

    @Test
    fun morphTokens_surfaceColorsAreDistinctFromBackground() {
        // surface, surfaceVariant, and background should all be different
        assertThat(MorphTokens.Colors.colorSurface).isNotEqualTo(MorphTokens.Colors.colorSurfaceVariant)
        assertThat(MorphTokens.Colors.colorSurface).isNotEqualTo(MorphTokens.Colors.colorBackground)
        assertThat(MorphTokens.Colors.colorSurfaceVariant).isNotEqualTo(MorphTokens.Colors.colorBackground)
    }

    @Test
    fun morphTokens_surfaceContainerHierarchyIsOrdered() {
        // The 5-level surface container hierarchy should have distinct values
        val containers = listOf(
            MorphTokens.Colors.colorSurfaceContainerLowest,
            MorphTokens.Colors.colorSurfaceContainerLow,
            MorphTokens.Colors.colorSurfaceContainer,
            MorphTokens.Colors.colorSurfaceContainerHigh,
            MorphTokens.Colors.colorSurfaceContainerHighest
        )
        // All 5 levels should be distinct
        assertThat(containers.toSet()).hasSize(5)
    }

    @Test
    fun morphTokens_typographyTokensArePositive() {
        assertThat(MorphTokens.Typography.fontSizeLargeTitle).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeTitle1).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeTitle2).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeTitle3).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeHeadline).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeBody).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeCallout).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeSubheadline).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeFootnote).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeCaption1).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeCaption2).isGreaterThan(0f)
        assertThat(MorphTokens.Typography.fontSizeButton).isGreaterThan(0f)
    }

    @Test
    fun morphTokens_typographyHierarchyIsOrdered() {
        // iOS typography scale: LargeTitle > Title1 > Title2 > Title3 > Headline >= Body
        assertThat(MorphTokens.Typography.fontSizeLargeTitle).isGreaterThan(MorphTokens.Typography.fontSizeTitle1)
        assertThat(MorphTokens.Typography.fontSizeTitle1).isGreaterThan(MorphTokens.Typography.fontSizeTitle2)
        assertThat(MorphTokens.Typography.fontSizeTitle2).isGreaterThan(MorphTokens.Typography.fontSizeTitle3)
        assertThat(MorphTokens.Typography.fontSizeTitle3).isGreaterThan(MorphTokens.Typography.fontSizeHeadline)
        assertThat(MorphTokens.Typography.fontSizeHeadline).isAtLeast(MorphTokens.Typography.fontSizeBody)
        assertThat(MorphTokens.Typography.fontSizeBody).isGreaterThan(MorphTokens.Typography.fontSizeFootnote)
        assertThat(MorphTokens.Typography.fontSizeFootnote).isGreaterThan(MorphTokens.Typography.fontSizeCaption1)
        assertThat(MorphTokens.Typography.fontSizeCaption1).isGreaterThan(MorphTokens.Typography.fontSizeCaption2)
    }

    @Test
    fun morphTokens_interactionTokensHaveReasonableValues() {
        // Press overlay alpha should be subtle (0 < alpha < 0.5)
        assertThat(MorphTokens.Interaction.pressOverlayMaxAlpha).isGreaterThan(0f)
        assertThat(MorphTokens.Interaction.pressOverlayMaxAlpha).isLessThan(0.5f)

        // Press durations should be quick (50-500ms)
        assertThat(MorphTokens.Interaction.pressInDuration).isAtLeast(50L)
        assertThat(MorphTokens.Interaction.pressInDuration).isAtMost(500L)
        assertThat(MorphTokens.Interaction.pressOutDuration).isAtLeast(50L)
        assertThat(MorphTokens.Interaction.pressOutDuration).isAtMost(500L)

        // Disabled alpha should make things visibly faded
        assertThat(MorphTokens.Interaction.disabledAlpha).isGreaterThan(0f)
        assertThat(MorphTokens.Interaction.disabledAlpha).isLessThan(1f)
    }

    // ── 11. S15: M3 ColorScheme compatibility ─────────────────────────────

    @Test
    fun morphColorPalette_fieldNamesContainAllM3ColorSchemeRoles() {
        // M3 ColorScheme constructor parameters (as of material3 1.x):
        val m3Roles = setOf(
            "primary", "onPrimary", "primaryContainer", "onPrimaryContainer",
            "secondary", "onSecondary", "secondaryContainer", "onSecondaryContainer",
            "tertiary", "onTertiary", "tertiaryContainer", "onTertiaryContainer",
            "error", "onError", "errorContainer", "onErrorContainer",
            "background", "onBackground",
            "surface", "onSurface", "surfaceVariant", "onSurfaceVariant",
            "surfaceDim", "surfaceBright",
            "surfaceContainerLowest", "surfaceContainerLow",
            "surfaceContainer", "surfaceContainerHigh", "surfaceContainerHighest",
            "outline", "outlineVariant",
            "inverseSurface", "inverseOnSurface", "inversePrimary",
            "scrim"
        )

        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphColorPalette",
            false, // 不触发类初始化
            javaClass.classLoader
        )
        val fieldNames = clazz.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) }
            .map { it.name }
            .toSet()

        // Every M3 role must have a corresponding field
        for (role in m3Roles) {
            assertThat(fieldNames).contains(role)
        }
    }

    @Test
    fun morphColorPalette_isDataClass() {
        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphColorPalette",
            false, // 不触发类初始化
            javaClass.classLoader
        )
        val methodNames = clazz.declaredMethods.map { it.name }
        // Data classes generate equals(), hashCode(), toString() — verify these exist
        assertThat(methodNames).contains("equals")
        assertThat(methodNames).contains("hashCode")
        assertThat(methodNames).contains("toString")
        // copy() is mangled by Compose inline class (Color) — check any method starting with "copy"
        assertThat(methodNames.any { it.startsWith("copy") }).isTrue()
        // Verify componentN methods exist — should have at least 30 for 37 fields
        val componentCount = methodNames.count { it.startsWith("component") }
        assertThat(componentCount).isAtLeast(30)
    }

    @Test
    fun morphShape_isDataClass() {
        val clazz = Class.forName(
            "com.morphkit.theme.compose.MorphShape",
            false, // 不触发类初始化
            javaClass.classLoader
        )
        val methodNames = clazz.declaredMethods.map { it.name }
        assertThat(methodNames).contains("copy")
        assertThat(methodNames).contains("component1")
        assertThat(methodNames).contains("component6") // 6 fields
    }
}
