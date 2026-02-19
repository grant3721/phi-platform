package com.globaloutcomes.phi.navigation

/**
 * Navigation Routes - Type-safe navigation destinations
 * Sealed class pattern ensures compile-time safety
 */
sealed class Routes(val route: String) {

    // Main App Flow
    data object Splash : Routes("splash")
    data object Auth : Routes("auth")
    data object Home : Routes("home")

    // Patient Management
    data object PatientList : Routes("patients")
    data object PatientDetail : Routes("patients/{patientId}") {
        fun createRoute(patientId: String) = "patients/$patientId"
    }
    data object PatientRegistration : Routes("patients/new")
    data object PatientEdit : Routes("patients/{patientId}/edit") {
        fun createRoute(patientId: String) = "patients/$patientId/edit"
    }

    // Scan Flow
    data object ScanPrep : Routes("scan/prep/{patientId}") {
        fun createRoute(patientId: String) = "scan/prep/$patientId"
    }
    data object Scan : Routes("scan/{patientId}") {
        fun createRoute(patientId: String) = "scan/$patientId"
    }
    data object ScanResult : Routes("scan/{scanId}/result") {
        fun createRoute(scanId: String) = "scan/$scanId/result"
    }

    // Survey Flow
    data object SurveySelection : Routes("survey/{scanId}/select") {
        fun createRoute(scanId: String) = "survey/$scanId/select"
    }
    data object NcdSurvey : Routes("survey/{scanId}/ncd") {
        fun createRoute(scanId: String) = "survey/$scanId/ncd"
    }
    data object MaternalSurvey : Routes("survey/{scanId}/maternal") {
        fun createRoute(scanId: String) = "survey/$scanId/maternal"
    }
    data object InfectiousSurvey : Routes("survey/{scanId}/infectious") {
        fun createRoute(scanId: String) = "survey/$scanId/infectious"
    }
    data object MentalHealthSurvey : Routes("survey/{scanId}/mental") {
        fun createRoute(scanId: String) = "survey/$scanId/mental"
    }

    // Referral Management
    data object ReferralList : Routes("referrals")
    data object ReferralDetail : Routes("referrals/{referralId}") {
        fun createRoute(referralId: String) = "referrals/$referralId"
    }

    // Clinical Encounters
    data object ClinicalEncounter : Routes("clinical/{patientId}") {
        fun createRoute(patientId: String) = "clinical/$patientId"
    }
    data object LabResults : Routes("lab/{patientId}") {
        fun createRoute(patientId: String) = "lab/$patientId"
    }
    data object MaternalClinical : Routes("clinical/maternal/{patientId}") {
        fun createRoute(patientId: String) = "clinical/maternal/$patientId"
    }

    // Claims Management
    data object ClaimsList : Routes("claims")
    data object ClaimDetail : Routes("claims/{claimId}") {
        fun createRoute(claimId: String) = "claims/$claimId"
    }

    // BHS/RHU Dashboards
    data object BhsDashboard : Routes("bhs/dashboard")
    data object RhuDashboard : Routes("rhu/dashboard")

    // Settings & Profile
    data object Settings : Routes("settings")
    data object Profile : Routes("profile")
    data object DevMenu : Routes("dev-menu")

    // Bottom Nav Destinations
    companion object {
        val bottomNavRoutes = listOf(
            Home,
            PatientList,
            ReferralList,
            Settings
        )
    }
}
