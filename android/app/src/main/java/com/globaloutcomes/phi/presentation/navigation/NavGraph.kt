package com.globaloutcomes.phi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

// Stubbed for test compilation
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
fun PHINavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    // Stubbed for test compilation
}
