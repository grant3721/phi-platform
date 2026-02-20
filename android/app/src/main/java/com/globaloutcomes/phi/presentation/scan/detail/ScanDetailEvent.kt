package com.globaloutcomes.phi.presentation.scan.detail

import com.globaloutcomes.phi.domain.model.BiomarkerCategory

sealed class ScanDetailEvent {
    data class LoadScan(val scanId: String) : ScanDetailEvent()
    data class SelectCategory(val category: BiomarkerCategory) : ScanDetailEvent()
    object ShareScan : ScanDetailEvent()
    object DismissShareDialog : ScanDetailEvent()
    object NavigateBack : ScanDetailEvent()
    object CompareToPrevious : ScanDetailEvent()
}
