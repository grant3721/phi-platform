package com.globaloutcomes.phi.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.globaloutcomes.phi.presentation.theme.*

/**
 * GO Risk Badge - Normal/Elevated/High-Risk Indicator
 * Used to display patient risk assessment throughout the app
 */

enum class RiskLevel {
    NORMAL,
    ELEVATED,
    HIGH_RISK
}

@Composable
fun GoRiskBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    size: RiskBadgeSize = RiskBadgeSize.MEDIUM
) {
    val (backgroundColor, textColor, label, icon) = when (riskLevel) {
        RiskLevel.NORMAL -> RiskBadgeStyle(
            backgroundColor = GoGreenLight,
            textColor = GoGreenDark,
            label = "Normal",
            icon = Icons.Default.CheckCircle
        )
        RiskLevel.ELEVATED -> RiskBadgeStyle(
            backgroundColor = GoAmberLight,
            textColor = Color(0xFF7C2D12), // Amber dark
            label = "Elevated",
            icon = Icons.Default.Warning
        )
        RiskLevel.HIGH_RISK -> RiskBadgeStyle(
            backgroundColor = GoRedLight,
            textColor = Color(0xFF7F1D1D), // Red dark
            label = "High-Risk",
            icon = Icons.Default.Warning
        )
    }

    val (horizontalPadding, verticalPadding, textStyle, iconSize) = when (size) {
        RiskBadgeSize.SMALL -> BadgeDimensions(
            horizontalPadding = 8.dp,
            verticalPadding = 4.dp,
            textStyle = MaterialTheme.typography.labelSmall,
            iconSize = 12.dp
        )
        RiskBadgeSize.MEDIUM -> BadgeDimensions(
            horizontalPadding = 12.dp,
            verticalPadding = 6.dp,
            textStyle = MaterialTheme.typography.labelMedium,
            iconSize = 16.dp
        )
        RiskBadgeSize.LARGE -> BadgeDimensions(
            horizontalPadding = 16.dp,
            verticalPadding = 8.dp,
            textStyle = MaterialTheme.typography.labelLarge,
            iconSize = 20.dp
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIcon) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = label,
            style = textStyle,
            color = textColor
        )
    }
}

/**
 * Risk Badge Size Options
 */
enum class RiskBadgeSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * Internal data classes for badge styling
 */
private data class RiskBadgeStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val label: String,
    val icon: ImageVector
)

private data class BadgeDimensions(
    val horizontalPadding: androidx.compose.ui.unit.Dp,
    val verticalPadding: androidx.compose.ui.unit.Dp,
    val textStyle: androidx.compose.ui.text.TextStyle,
    val iconSize: androidx.compose.ui.unit.Dp
)

/**
 * Helper function to determine risk level from flags
 */
fun getRiskLevelFromFlags(riskFlags: List<String>?): RiskLevel {
    return when {
        riskFlags.isNullOrEmpty() -> RiskLevel.NORMAL
        riskFlags.any { it.contains("crisis") || it.contains("hypoxemia") } -> RiskLevel.HIGH_RISK
        else -> RiskLevel.ELEVATED
    }
}

/**
 * Helper function to determine risk level from high-risk flag
 */
fun getRiskLevelFromFlag(highRiskFlag: Boolean): RiskLevel {
    return if (highRiskFlag) RiskLevel.HIGH_RISK else RiskLevel.NORMAL
}
