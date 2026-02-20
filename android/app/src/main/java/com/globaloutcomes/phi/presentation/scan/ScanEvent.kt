package com.globaloutcomes.phi.presentation.scan

sealed class ScanEvent {
    object StartScan : ScanEvent()
    object CancelScan : ScanEvent()
    object ClearError : ScanEvent()
    object NavigateBack : ScanEvent()
}
