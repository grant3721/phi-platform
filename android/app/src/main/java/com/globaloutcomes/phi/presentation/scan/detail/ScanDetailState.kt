package com.globaloutcomes.phi.presentation.scan.detail

import com.globaloutcomes.phi.domain.model.BiomarkerCategory
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Scan

data class ScanDetailState(
    val scan: Scan? = null,
    val patient: Patient? = null,
    val selectedCategory: BiomarkerCategory = BiomarkerCategory.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showShareDialog: Boolean = false
)
