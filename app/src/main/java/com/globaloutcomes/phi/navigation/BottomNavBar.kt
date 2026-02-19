package com.globaloutcomes.phi.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom Navigation Bar - Primary navigation for BHW workflow
 * Four main destinations: Home, Patients, Referrals, Settings
 */
@Composable
fun GoBottomNavBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route.route } == true,
                onClick = {
                    navController.navigate(item.route.route) {
                        // Pop up to start destination to avoid large back stack
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Bottom Navigation Items
 */
private data class BottomNavItem(
    val route: Routes,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = Routes.Home,
        icon = Icons.Filled.Home,
        label = "Home"
    ),
    BottomNavItem(
        route = Routes.PatientList,
        icon = Icons.Filled.Person,
        label = "Patients"
    ),
    BottomNavItem(
        route = Routes.ReferralList,
        icon = Icons.Filled.Warning,
        label = "Referrals"
    ),
    BottomNavItem(
        route = Routes.Settings,
        icon = Icons.Filled.Settings,
        label = "Settings"
    )
)
