package com.globaloutcomes.phi.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.globaloutcomes.phi.presentation.theme.GoGreen
import com.globaloutcomes.phi.presentation.theme.GoRed

/**
 * GO Button Component - Primary/Secondary/Danger Variants
 * Standard button with GO branding and minimum 48dp touch target
 */

enum class GoButtonStyle {
    PRIMARY,
    SECONDARY,
    DANGER
}

@Composable
fun GoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GoButtonStyle = GoButtonStyle.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    when (style) {
        GoButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(56.dp),
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        GoButtonStyle.SECONDARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(56.dp),
                enabled = enabled && !loading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GoGreen
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = GoGreen,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        GoButtonStyle.DANGER -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(56.dp),
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoRed,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * GO Text Button - For less prominent actions
 */
@Composable
fun GoTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = GoGreen
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
