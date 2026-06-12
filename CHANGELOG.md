# Changelog

All notable changes to the MorphKit project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **MorphColors object**: Extracted color utility functions (`overlayColor`, `adjustAlpha`, `blendColor`, `createColorStateList`, `isDarkMode`) from `MorphTheme` into dedicated `MorphColors.kt`, reducing God Object complexity
- **MorphTypography object**: Standalone typography token module (`MorphTypography.kt`) with iOS SF Pro visual weight alignment (11 text style levels, `FontWeight` enum with `toTypeface()` mapping)
- **MorphShape object**: Standalone shape token module (`MorphShape.kt`) with Context-aware dp conversion for multi-window/foldable device support (`cornerSmall`, `cornerMedium`, `cornerLarge`, `cornerFull`)
- **MorphTokens nested objects**: Token layer reorganized into nested sub-objects (`Colors`, `Shapes`, `Typography`, `Interaction`, `Spacing`, `Elevation`, `Motion`) with top-level flat delegation constants for backward compatibility
- **ConcurrencyTest**: Concurrent initialization, debounce, and ConcurrentHashMap registration tests covering multi-thread safety
- **MorphColorsTest**: Unit tests for `isDarkMode` dark mode detection (YES/NO/UNDEFINED)
- **MorphFactory2ChainTest**: Responsibility chain pattern tests covering hit/miss/exception/fallback/delayed-factory scenarios
- **ReflectionHelperTest**: 18 tests covering `safeSetFactory2` full path scenarios
- **MorphKitIOSConfigTest**: 18 tests validating `stylePolicy(IOS)`, widget registration, and RecyclerView modification
- **MorphComposeThemeBehaviorTest**: 18 tests for Compose theme behavior
- **Version Catalog**: `gradle/libs.versions.toml` for unified dependency version management
- **CI Pipeline**: `.github/workflows/ci.yml` GitHub Actions workflow with JDK 17, Android SDK 35, and automated test execution
- **Compose-only MorphTheme overload**: `MorphTheme(colors, shape, interactionMode)` for Compose-only usage without Context
- **Pixel dark mode fallback**: `pixelDarkFallback` dark color scheme using `MorphTokens` Dark series constants
- **MorphCompoundButtonHelper**: Extracted shared iOS mode logic from `MorphRadioButton`/`MorphCheckBox` (~120 lines deduplicated)
- **Bitmap object pool** in `BackdropBlurHelper` with `clearPool()` for memory pressure response
- **ComponentCallbacks2** registration in `MorphInitProvider` for automatic Bitmap pool cleanup
- **BackdropBlurHelper dual-level cache**: Primary/secondary `pixelBuffer` caches supporting alternating card sizes
- **MorphCardView scroll throttle**: 16ms interval blur throttle to reduce excessive redraws
- **MorphEditText focus color animation**: `ValueAnimator` + `DecelerateInterpolator` 150ms transition
- **Deprecated backward compatibility tests**: `@Suppress("DEPRECATION")` tests verifying `MorphTheme.*` deprecated methods delegate correctly to `MorphColors`
- **MorphTypographyTest**: Comprehensive tests for all 11 typography tokens, `FontWeight` enum, `toTypeface()` mapping, and `MorphTokens.Typography` consistency
- **MorphShapeTest**: Tests for shape tokens across multiple screen densities (mdpi/xhdpi/xxhdpi/xxxhdpi), `MorphTokens.Shapes` consistency, and ordering invariants
- **CI lint check**: Added `./gradlew lintDebug` step and lint report upload in CI workflow

### Changed

- **MorphTheme deprecated methods upgraded to `DeprecationLevel.ERROR`**: Forces compile-time migration to `MorphColors`/`MorphShape`/`MorphTypography`
- **MorphTypography and MorphShape split into independent modules**: Previously part of `MorphTheme`, now standalone files
- **`MorphKit.config` marked `@Volatile`**: Ensures cross-thread visibility
- **`MorphStyleResolver.dynamicColorMethod`**: Changed from `@Volatile` + boolean flag to Kotlin `lazy` delegate, eliminating check-then-act race conditions
- **`MorphFactory2.hostThemeChecked`**: Changed to atomic operation for concurrent safety
- **`MorphConfig`**: Switched from `mutableMapOf` to `ConcurrentHashMap` for thread-safe rule registration
- **`MorphClickListener`**: AtomicLong + CAS for race-free debounce, using `elapsedRealtime`
- **`BackdropBlurHelper`**: Bitmap pool methods synchronized with `@Synchronized`; `RenderNode.discardDisplayList()` for GPU resource cleanup
- **`MorphCardView`**: Reuses `BitmapDrawable` for same-size blur; iOS mode sets `rippleColor=TRANSPARENT` instead of disabling `clickable`
- **`MorphTokens`**: Added `@get:ColorInt` annotations on delegated properties; dark mode tokens (`colorOnPrimaryDark`, `colorErrorDark`, `colorSuccessDark`, `colorWarningDark`)
- **`MorphColorPalette.iosDark()`**: All 8 hardcoded colors replaced with `MorphTokens` constant references
- **`MorphEditText`**: Removed private `blendColor()`, now uses `MorphTheme.blendColor()`
- **Compose MorphButton**: Added `defaultMinSize(88dp x 48dp)` for accessibility touch targets; removed hardcoded `fillMaxWidth`/`height(50dp)`
- **`minSdk` upgraded 21 -> 35**: Removed all `Build.VERSION.SDK_INT` version guards
- **StylePolicy/InteractionMode unified**: View and Compose layers now share the same enum classes; deprecated aliases preserved
- **`build.gradle.kts`**: Switched to Version Catalog; merged duplicate Compose BOM declarations; Compose runtime from `testCompileOnly` to `testImplementation`
- **ProGuard**: `create` method keep rules narrowed to `com.morphkit.**`
- **Deprecated API tests in `MorphThemeTest.kt`**: Existing tests migrated from `MorphTheme.*` to `MorphColors.*`; deprecated API backward compat tests added with `@Suppress("DEPRECATION")`
- **`MorphColorsTest.kt`**: Added `overlayColor`, `adjustAlpha`, `blendColor`, `createColorStateList` test suites with mocked `android.graphics.Color`

### Fixed

- **`MorphCardView.blur()` null safety**: Early return when blur returns null, preventing NPE crashes
- **`BackdropBlurHelper` software fallback**: Fixed returning recycled bitmap in `blurWithRenderEffect`/`blurWithRenderNode`
- **`MorphKit` config initialization**: Added `initialized` pre-check for `lateinit` config
- **`MorphFactory2`**: Replaced non-null assertion (`!!`) with safe call (`?.let`) to eliminate potential NPE
- **`MorphKit.createView`**: Removed silent exception swallowing in `stampAndValidateView`
- **`MorphRadioButton`/`MorphCheckBox`**: `setPadding` instead of `setMeasuredDimension` to fix text-indicator overlap
- **`MorphCardView` bitmap cleanup**: Fixed removal/recycling order for blur background views
- **Missing `onConfigurationChanged`**: Added to `MorphButton`, `MorphEditText`, `MorphTextView`
- **Animator XML**: Default state now uses `set` combining `alpha` + `translationZ` restoration
- **`MorphTheme.cornerFull`**: Removed incorrect `@Px` annotation
- **`MorphTextView`**: Removed redundant identity mapping in `setTypeface`; tertiary text alpha uses `MorphTokens.tertiaryTextAlpha`
- **`MorphEditText`/`MorphRadioButton`**: Removed unused dead code (`tintColor`, `onSurfaceColor`)
- **Pixel dark mode fallback**: Fixed Pixel mode falling back to light tokens in dark mode
- **`MorphInstaller`**: Added API 29+ guard (later simplified with minSdk=35)
- **Build environment**: Downgraded Gradle 8.14.4 -> 8.7, AGP 8.9.1 -> 8.5.0, Kotlin 2.1.21 -> 2.0.21 to fix plugin compatibility issues
- **`MorphRadioButton`**: Removed duplicate `initIosMode` call (`StateListAnimator` no longer loaded twice)
- **`MorphCardView`**: `onDetachedFromWindow` clears `blurBackgroundView` to prevent Context leaks
- **`.gitignore`**: Added `META-INF/` rule; removed non-versioned `META-INF/MANIFEST.MF`

### Documentation

- **README.md**: Updated build versions, architecture diagram (skin layer with `MorphColors`/`MorphShape`/`MorphTypography`), thread safety and memory optimization sections
- **agent.md**: Comprehensive review and optimization with build baseline, architecture updates, file index, test coverage matrix, and blur strategy documentation
- **consumer-rules.pro**: Fixed stale enum name references
