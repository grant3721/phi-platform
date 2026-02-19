package com.globaloutcomes.phi.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.globaloutcomes.phi.presentation.home.HomeScreen
import com.globaloutcomes.phi.presentation.patient.list.PatientListScreen
import com.globaloutcomes.phi.presentation.patient.registration.RegistrationScreen
import com.globaloutcomes.phi.presentation.referral.list.ReferralListScreen
import com.globaloutcomes.phi.presentation.scan.ScanScreen
import com.globaloutcomes.phi.presentation.scan.result.ScanResultScreen
import com.globaloutcomes.phi.presentation.settings.SettingsScreen

/**
 * Navigation Graph - Defines all navigation routes and composable screens
 * Uses Jetpack Compose Navigation with type-safe arguments
 */
@Composable
fun GoNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToNewScan = { navController.navigate(Routes.PatientRegistration.route) },
                onNavigateToPatients = { navController.navigate(Routes.PatientList.route) },
                onNavigateToReferrals = { navController.navigate(Routes.ReferralList.route) }
            )
        }

        // Patient List Screen
        composable(Routes.PatientList.route) {
            PatientListScreen(
                onNavigateToPatientDetail = { patientId ->
                    navController.navigate(Routes.PatientDetail.createRoute(patientId))
                },
                onNavigateToNewPatient = {
                    navController.navigate(Routes.PatientRegistration.route)
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Patient Registration Screen
        composable(Routes.PatientRegistration.route) {
            RegistrationScreen(
                onNavigateToScan = { patientId ->
                    navController.navigate(Routes.Scan.createRoute(patientId)) {
                        // Remove registration screen from back stack after successful registration
                        popUpTo(Routes.PatientList.route)
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Scan Screen
        composable(
            route = Routes.Scan.route,
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: return@composable
            ScanScreen(
                patientId = patientId,
                onNavigateToResult = { scanId ->
                    navController.navigate(Routes.ScanResult.createRoute(scanId)) {
                        // Remove scan screen from back stack
                        popUpTo(Routes.Home.route)
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Scan Result Screen
        composable(
            route = Routes.ScanResult.route,
            arguments = listOf(
                navArgument("scanId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: return@composable
            ScanResultScreen(
                scanId = scanId,
                onNavigateToSurvey = { surveyType ->
                    // Navigate to appropriate survey based on type
                    navController.navigate(Routes.SurveySelection.createRoute(scanId))
                },
                onNavigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        // Clear back stack
                        popUpTo(Routes.Home.route) {
                            inclusive = false
                        }
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Referral List Screen
        composable(Routes.ReferralList.route) {
            ReferralListScreen(
                onNavigateToReferralDetail = { referralId ->
                    navController.navigate(Routes.ReferralDetail.createRoute(referralId))
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Settings Screen
        composable(Routes.Settings.route) {
            SettingsScreen(
                onNavigateToDevMenu = {
                    navController.navigate(Routes.DevMenu.route)
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Additional screens will be added in later phases:
        // - Patient Detail
        // - Referral Detail
        // - Survey Screens (NCD, Maternal, Infectious, Mental Health)
        // - Clinical Encounter
        // - Lab Results
        // - Claims
        // - BHS/RHU Dashboards
        // - Dev Menu
    }
}
