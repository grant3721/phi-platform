package com.globaloutcomes.phi.presentation.referral.list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Referral List Screen - Placeholder
 * Full implementation in Week 6 (Phase 8)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralListScreen(
    onNavigateToReferralDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Referrals") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Referrals - Coming Soon",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
