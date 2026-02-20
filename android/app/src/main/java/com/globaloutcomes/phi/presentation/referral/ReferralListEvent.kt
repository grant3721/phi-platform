package com.globaloutcomes.phi.presentation.referral

import com.globaloutcomes.phi.domain.model.ReferralStatus

sealed class ReferralListEvent {
    object LoadReferrals : ReferralListEvent()
    data class OnReferralClick(val referralId: String) : ReferralListEvent()
    data class FilterByStatus(val status: ReferralStatus?) : ReferralListEvent()
    object Refresh : ReferralListEvent()
    object ClearError : ReferralListEvent()
}
