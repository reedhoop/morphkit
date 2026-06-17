# ═══════════════════════════════════════════════════════════════════════════════
# App-Compose ProGuard Rules
# Demo application — minify disabled, rules for future reference
# ═══════════════════════════════════════════════════════════════════════════════

# Compose — keep Composable function names for debugging
-keep class com.morphkit.demo.compose.** { *; }
