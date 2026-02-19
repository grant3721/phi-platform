package com.globaloutcomes.phi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.globaloutcomes.phi.navigation.GoBottomNavBar
import com.globaloutcomes.phi.navigation.GoNavGraph
import com.globaloutcomes.phi.navigation.Routes
import com.globaloutcomes.phi.presentation.theme.GlobalOutcomesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Single Activity Architecture with Jetpack Compose
 * All navigation handled by Compose Navigation
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlobalOutcomesTheme {
                PhiApp()
            }
        }
    }
}

@Composable
fun PhiApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Only show bottom nav on main screens
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute in Routes.bottomNavRoutes.map { it.route }) {
                GoBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        GoNavGraph(
            navController = navController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun GoNavGraph(
    navController: androidx.navigation.NavHostController,
    startDestination: String,
    modifier: Modifier
) {
    com.globaloutcomes.phi.navigation.GoNavGraph(
        navController = navController,
        startDestination = startDestination
    )
}
