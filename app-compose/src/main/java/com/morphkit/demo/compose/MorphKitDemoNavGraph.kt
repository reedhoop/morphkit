package com.morphkit.demo.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.morphkit.demo.compose.pages.ButtonPage
import com.morphkit.demo.compose.pages.CatalogPage
import com.morphkit.demo.compose.pages.SettingsPage
import com.morphkit.demo.compose.pages.ThemePage

@Composable
fun MorphKitDemoNavGraph() {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "catalog",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("catalog") {
                CatalogPage(
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable("button") {
                ButtonPage(onBack = { navController.popBackStack() })
            }
            composable("theme") {
                ThemePage(onBack = { navController.popBackStack() })
            }
            composable("settings") {
                SettingsPage(onBack = { navController.popBackStack() })
            }
        }
    }
}
