package com.globaloutcomes.phi.presentation.referral

import com.globaloutcomes.phi.domain.model.Referral
import com.globaloutcomes.phi.domain.model.ReferralStatus

data class ReferralListState(
    val referrals: List<Referral> = emptyList(),
    val filterStatus: ReferralStatus? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
