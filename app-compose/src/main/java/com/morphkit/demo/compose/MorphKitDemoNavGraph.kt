package com.morphkit.demo.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.morphkit.demo.compose.pages.ButtonPage
import com.morphkit.demo.compose.pages.CardPage
import com.morphkit.demo.compose.pages.CatalogPage
import com.morphkit.demo.compose.pages.SelectionPage
import com.morphkit.demo.compose.pages.SettingsPage
import com.morphkit.demo.compose.pages.TextPage
import com.morphkit.demo.compose.pages.ThemePage

/** Route constants for navigation destinations. */
object Routes {
    const val CATALOG = "catalog"
    const val BUTTON = "button"
    const val TEXT = "text"
    const val CARD = "card"
    const val SELECTION = "selection"
    const val THEME = "theme"
    const val SETTINGS = "settings"
}

@Composable
fun MorphKitDemoNavGraph() {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CATALOG,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.CATALOG) {
                CatalogPage(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.BUTTON) {
                ButtonPage(onBack = { navController.popBackStack() })
            }
            composable(Routes.TEXT) {
                TextPage(onBack = { navController.popBackStack() })
            }
            composable(Routes.CARD) {
                CardPage(onBack = { navController.popBackStack() })
            }
            composable(Routes.SELECTION) {
                SelectionPage(onBack = { navController.popBackStack() })
            }
            composable(Routes.THEME) {
                ThemePage(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsPage(onBack = { navController.popBackStack() })
            }
        }
    }
}
