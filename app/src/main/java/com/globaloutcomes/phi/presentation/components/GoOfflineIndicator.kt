package com.globaloutcomes.phi.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.globaloutcomes.phi.presentation.theme.GoAmber
import com.globaloutcomes.phi.presentation.theme.GoGray
import com.globaloutcomes.phi.presentation.theme.GoGreen

/**
 * GO Offline Indicator - Sync Status Chip
 * Shows current sync status: Synced / Pending / Offline / Syncing
 */

enum class SyncStatus {
    SYNCED,
    PENDING,
    OFFLINE,
    SYNCING
}

@Composable
fun GoOfflineIndicator(
    syncStatus: SyncStatus,
    pendingCount: Int = 0,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor, icon, label) = when (syncStatus) {
        SyncStatus.SYNCED -> SyncIndicatorStyle(
            backgroundColor = GoGreen.copy(alpha = 0.1f),
            contentColor = GoGreen,
            icon = Icons.Default.CloudDone,
            label = "Synced"
        )
        SyncStatus.PENDING -> SyncIndicatorStyle(
            backgroundColor = GoAmber.copy(alpha = 0.1f),
            contentColor = GoAmber,
            icon = Icons.Default.CloudSync,
            label = if (pendingCount > 0) "$pendingCount Pending" else "Pending"
        )
        SyncStatus.OFFLINE -> SyncIndicatorStyle(
            backgroundColor = GoGray.copy(alpha = 0.1f),
            contentColor = GoGray,
            icon = Icons.Default.CloudOff,
            label = "Offline"
        )
        SyncStatus.SYNCING -> SyncIndicatorStyle(
            backgroundColor = GoGreen.copy(alpha = 0.1f),
            contentColor = GoGreen,
            icon = Icons.Default.CloudSync,
            label = "Syncing..."
        )
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = backgroundColor,
        label = "background"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = contentColor,
        label = "content"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(animatedBackgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = animatedContentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = animatedContentColor
        )

        if (pendingCount > 0 && syncStatus == SyncStatus.PENDING) {
            Spacer(modifier = Modifier.width(4.dp))
            Badge(
                containerColor = GoAmber,
                contentColor = Color.White
            ) {
                Text(
                    text = pendingCount.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Internal data class for sync indicator styling
 */
private data class SyncIndicatorStyle(
    val backgroundColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val label: String
)

/**
 * Compact sync status icon (no label)
 */
@Composable
fun GoSyncStatusIcon(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (syncStatus) {
        SyncStatus.SYNCED -> Icons.Default.CloudDone to GoGreen
        SyncStatus.PENDING -> Icons.Default.CloudSync to GoAmber
        SyncStatus.OFFLINE -> Icons.Default.CloudOff to GoGray
        SyncStatus.SYNCING -> Icons.Default.CloudSync to GoGreen
    }

    Icon(
        imageVector = icon,
        contentDescription = syncStatus.name,
        tint = tint,
        modifier = modifier.size(24.dp)
    )
}
