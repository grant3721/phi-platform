package com.globaloutcomes.phi.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.globaloutcomes.phi.presentation.home.HomeScreen
import com.globaloutcomes.phi.presentation.patient.registration.RegistrationScreen
import com.globaloutcomes.phi.presentation.scan.ScanScreen

// Navigation Routes
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PatientList : Screen("patients")
    object Registration : Screen("registration")
    object PatientDetail : Screen("patient/{patientId}") {
        fun createRoute(patientId: String) = "patient/$patientId"
    }
    object Scan : Screen("scan/{patientId}") {
        fun createRoute(patientId: String) = "scan/$patientId"
    }
    object ScanResults : Screen("scan-results/{scanId}") {
        fun createRoute(scanId: String) = "scan-results/$scanId"
    }
    object NcdSurvey : Screen("survey/ncd/{patientId}?scanId={scanId}") {
        fun createRoute(patientId: String, scanId: String?) =
            "survey/ncd/$patientId" + (scanId?.let { "?scanId=$it" } ?: "")
    }
    object MaternalSurvey : Screen("survey/maternal/{patientId}?scanId={scanId}") {
        fun createRoute(patientId: String, scanId: String?) =
            "survey/maternal/$patientId" + (scanId?.let { "?scanId=$it" } ?: "")
    }
    object ReferralList : Screen("referrals")
    object ReferralDetail : Screen("referral/{referralId}") {
        fun createRoute(referralId: String) = "referral/$referralId"
    }
}

@Composable
fun PHINavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToNewScan = {
                    navController.navigate(Screen.Registration.route)
                },
                onNavigateToPatients = {
                    navController.navigate(Screen.PatientList.route)
                }
            )
        }

        // Patient Registration
        composable(Screen.Registration.route) {
            RegistrationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistrationSuccess = { patientId ->
                    // After registration, navigate to scan screen
                    navController.navigate(Screen.Scan.createRoute(patientId)) {
                        // Pop registration screen from back stack
                        popUpTo(Screen.Registration.route) { inclusive = true }
                    }
                }
            )
        }

        // Scan Screen
        composable(
            route = Screen.Scan.route,
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType }
            )
        ) {
            ScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onScanComplete = { scanId ->
                    // After scan, navigate to results screen
                    navController.navigate(Screen.ScanResults.createRoute(scanId)) {
                        // Pop scan screen from back stack
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                }
            )
        }

        // Patient List (Placeholder for now)
        composable(Screen.PatientList.route) {
            // TODO: Implement PatientListScreen
            PlaceholderScreen(
                title = "Patient List",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Patient Detail (Placeholder for now)
        composable(
            route = Screen.PatientDetail.route,
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType }
            )
        ) {
            // TODO: Implement PatientDetailScreen
            PlaceholderScreen(
                title = "Patient Detail",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Scan Results (Placeholder for now)
        composable(
            route = Screen.ScanResults.route,
            arguments = listOf(
                navArgument("scanId") { type = NavType.StringType }
            )
        ) {
            // TODO: Implement ScanResultsScreen
            PlaceholderScreen(
                title = "Scan Results",
                onNavigateBack = {
                    // Navigate back to home after viewing results
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // NCD Survey (Placeholder for now)
        composable(
            route = Screen.NcdSurvey.route,
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("scanId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            // TODO: Implement NcdSurveyScreen
            PlaceholderScreen(
                title = "NCD Survey",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Maternal Survey (Placeholder for now)
        composable(
            route = Screen.MaternalSurvey.route,
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("scanId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            // TODO: Implement MaternalSurveyScreen
            PlaceholderScreen(
                title = "Maternal Survey",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Referral List (Placeholder for now)
        composable(Screen.ReferralList.route) {
            // TODO: Implement ReferralListScreen
            PlaceholderScreen(
                title = "Referrals",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Referral Detail (Placeholder for now)
        composable(
            route = Screen.ReferralDetail.route,
            arguments = listOf(
                navArgument("referralId") { type = NavType.StringType }
            )
        ) {
            // TODO: Implement ReferralDetailScreen
            PlaceholderScreen(
                title = "Referral Detail",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Temporary placeholder screen for unimplemented screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$title\n(Coming Soon)",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
