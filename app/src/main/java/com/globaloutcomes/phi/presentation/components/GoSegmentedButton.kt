package com.globaloutcomes.phi.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.globaloutcomes.phi.presentation.theme.GoGreen

/**
 * GO Segmented Button - Yes/No/Unknown Selector
 * Used for binary and tri-state questions in surveys and forms
 * Provides haptic-like feedback through Material 3 state layers
 */

enum class GoSegmentedOption {
    YES,
    NO,
    UNKNOWN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoSegmentedButton(
    selected: GoSegmentedOption?,
    onSelectionChange: (GoSegmentedOption) -> Unit,
    modifier: Modifier = Modifier,
    options: List<GoSegmentedOption> = listOf(
        GoSegmentedOption.YES,
        GoSegmentedOption.NO,
        GoSegmentedOption.UNKNOWN
    ),
    labels: Map<GoSegmentedOption, String> = mapOf(
        GoSegmentedOption.YES to "Yes",
        GoSegmentedOption.NO to "No",
        GoSegmentedOption.UNKNOWN to "Unknown"
    )
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selected == option,
                onClick = { onSelectionChange(option) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = GoGreen,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = labels[option] ?: option.name,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Two-Option Segmented Button (Yes/No only)
 */
@Composable
fun GoBinarySegmentedButton(
    selected: Boolean?,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    yesLabel: String = "Yes",
    noLabel: String = "No"
) {
    val segmentedOption = when (selected) {
        true -> GoSegmentedOption.YES
        false -> GoSegmentedOption.NO
        null -> null
    }

    GoSegmentedButton(
        selected = segmentedOption,
        onSelectionChange = { option ->
            onSelectionChange(option == GoSegmentedOption.YES)
        },
        modifier = modifier,
        options = listOf(GoSegmentedOption.YES, GoSegmentedOption.NO),
        labels = mapOf(
            GoSegmentedOption.YES to yesLabel,
            GoSegmentedOption.NO to noLabel
        )
    )
}

/**
 * Custom Segmented Button for arbitrary string options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoCustomSegmentedButton(
    options: List<String>,
    selectedIndex: Int?,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onSelectionChange(index) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = GoGreen,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
