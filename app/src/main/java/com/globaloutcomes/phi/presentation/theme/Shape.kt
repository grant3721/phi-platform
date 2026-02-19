package com.globaloutcomes.phi.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Component shapes for Global Outcomes PHI Platform
val Shapes = Shapes(
    // Small components: chips, badges
    small = RoundedCornerShape(8.dp),

    // Medium components: cards, text fields
    medium = RoundedCornerShape(12.dp),

    // Large components: dialogs, bottom sheets
    large = RoundedCornerShape(16.dp)
)
