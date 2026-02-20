package com.globaloutcomes.phi.presentation.home

sealed class HomeEvent {
    object LoadDashboard : HomeEvent()
    object NavigateToNewScan : HomeEvent()
    object NavigateToPatients : HomeEvent()
    object Refresh : HomeEvent()
    object ClearError : HomeEvent()
}
