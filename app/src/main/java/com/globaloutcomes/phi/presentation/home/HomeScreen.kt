package com.globaloutcomes.phi.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard
import com.globaloutcomes.phi.presentation.components.GoProgressRing
import com.globaloutcomes.phi.presentation.components.ButtonVariant
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home Screen - BHW Dashboard
 * Shows: Today's stats, earnings, quick actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNewScan: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToReferrals: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good ${getGreeting()}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = getCurrentDate(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Earnings Card
            EarningsCard(
                scansToday = state.scansToday,
                earningsToday = state.earningsToday,
                dailyCap = 150.0
            )

            // Quick Stats
            QuickStatsCard(
                scansToday = state.scansToday,
                totalPatients = state.totalPatients,
                pendingReferrals = state.pendingReferrals
            )

            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            GoButton(
                text = "New Scan",
                onClick = onNavigateToNewScan,
                variant = ButtonVariant.PRIMARY,
                icon = Icons.Filled.Add,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoButton(
                    text = "My Patients",
                    onClick = onNavigateToPatients,
                    variant = ButtonVariant.SECONDARY,
                    icon = Icons.Filled.Person,
                    modifier = Modifier.weight(1f)
                )

                if (state.pendingReferrals > 0) {
                    GoButton(
                        text = "Referrals (${state.pendingReferrals})",
                        onClick = onNavigateToReferrals,
                        variant = ButtonVariant.DANGER,
                        icon = Icons.Filled.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EarningsCard(
    scansToday: Int,
    earningsToday: Double,
    dailyCap: Double
) {
    val progress = (scansToday / 50f).coerceIn(0f, 1f)
    val isNearCap = scansToday >= 40
    val isAtCap = scansToday >= 50

    GoCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Earnings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₱${String.format("%.2f", earningsToday)} / ₱${String.format("%.2f", dailyCap)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isAtCap -> MaterialTheme.colorScheme.error
                            isNearCap -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                </Column>

                GoProgressRing(
                    progress = progress,
                    size = 80.dp,
                    strokeWidth = 8.dp
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Scans Today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$scansToday / 50",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                </Column>

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₱3.00 per scan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isAtCap) {
                Text(
                    text = "⚠️ Daily cap reached. Additional scans will not earn incentives.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (isNearCap) {
                Text(
                    text = "${50 - scansToday} scans remaining to reach daily cap",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickStatsCard(
    scansToday: Int,
    totalPatients: Int,
    pendingReferrals: Int
) {
    GoCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Filled.Check,
                    value = scansToday.toString(),
                    label = "Scans Today"
                )

                StatItem(
                    icon = Icons.Filled.Person,
                    value = totalPatients.toString(),
                    label = "Total Patients"
                )

                if (pendingReferrals > 0) {
                    StatItem(
                        icon = Icons.Filled.Warning,
                        value = pendingReferrals.toString(),
                        label = "Pending Referrals",
                        isAlert = true
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    isAlert: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }
}

private fun getCurrentDate(): String {
    val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    return formatter.format(Date())
}
