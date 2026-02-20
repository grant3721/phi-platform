package com.globaloutcomes.phi.presentation.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onNavigateBack: () -> Unit,
    onScanComplete: (scanId: String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Handle scan completion
    LaunchedEffect(state.isComplete) {
        if (state.isComplete && state.completedScan != null) {
            onScanComplete(state.completedScan!!.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Scan Patient", style = MaterialTheme.typography.titleLarge)
                        if (state.patientName.isNotBlank()) {
                            Text(
                                state.patientName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !state.isScanning
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Camera Preview Area
                CameraPreviewArea(
                    faceDetected = state.faceDetected,
                    isScanning = state.isScanning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scan Progress Section
                if (state.isScanning) {
                    ScanProgressSection(
                        progress = state.progress,
                        elapsedSeconds = state.elapsedSeconds,
                        totalSeconds = state.totalSeconds,
                        signalQuality = state.signalQuality,
                        guidance = state.guidance
                    )
                } else {
                    // Guidance when not scanning
                    if (state.guidance.isNotBlank()) {
                        GuidanceCard(guidance = state.guidance)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                if (state.isScanning) {
                    Button(
                        onClick = { viewModel.onEvent(ScanEvent.CancelScan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Scan", style = MaterialTheme.typography.titleMedium)
                    }
                } else if (!state.isComplete) {
                    Button(
                        onClick = { viewModel.onEvent(ScanEvent.StartScan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = state.errorMessage == null
                    ) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Scan", style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Error message
                state.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewArea(
    faceDetected: Boolean,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Camera Preview Placeholder
        // TODO: Replace with CameraX Preview when SDK is integrated
        Text(
            "Camera Preview",
            color = Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.titleLarge
        )

        // Face Detection Overlay
        if (faceDetected) {
            FaceDetectionOverlay(isScanning = isScanning)
        }

        // Instructions Overlay (when not scanning)
        if (!isScanning) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Position your face in the frame",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Make sure you are in a well-lit area",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FaceDetectionOverlay(isScanning: Boolean) {
    // Animated pulsing border when face detected
    val infiniteTransition = rememberInfiniteTransition(label = "face_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isScanning) 1f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp)
    ) {
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 40.dp.toPx()

        val color = if (isScanning) {
            Color.Green.copy(alpha = alpha)
        } else {
            Color.White.copy(alpha = alpha)
        }

        // Draw corners of face detection frame
        val rectSize = minOf(size.width, size.height)
        val left = (size.width - rectSize) / 2
        val top = (size.height - rectSize) / 2

        // Top-left corner
        drawLine(
            color = color,
            start = Offset(left, top + cornerLength),
            end = Offset(left, top),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(left, top),
            end = Offset(left + cornerLength, top),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Top-right corner
        drawLine(
            color = color,
            start = Offset(left + rectSize - cornerLength, top),
            end = Offset(left + rectSize, top),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(left + rectSize, top),
            end = Offset(left + rectSize, top + cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Bottom-left corner
        drawLine(
            color = color,
            start = Offset(left, top + rectSize - cornerLength),
            end = Offset(left, top + rectSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(left, top + rectSize),
            end = Offset(left + cornerLength, top + rectSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Bottom-right corner
        drawLine(
            color = color,
            start = Offset(left + rectSize - cornerLength, top + rectSize),
            end = Offset(left + rectSize, top + rectSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(left + rectSize, top + rectSize - cornerLength),
            end = Offset(left + rectSize, top + rectSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ScanProgressSection(
    progress: Float,
    elapsedSeconds: Int,
    totalSeconds: Int,
    signalQuality: String,
    guidance: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(120.dp),
                strokeWidth = 12.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$elapsedSeconds",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Signal Quality
        SignalQualityIndicator(quality = signalQuality)

        Spacer(modifier = Modifier.height(16.dp))

        // Guidance Message
        if (guidance.isNotBlank()) {
            GuidanceCard(guidance = guidance)
        }
    }
}

@Composable
private fun SignalQualityIndicator(quality: String) {
    val (icon, color) = when (quality) {
        "Excellent" -> Icons.Default.SignalCellularAlt to Color(0xFF4CAF50)
        "Good" -> Icons.Default.SignalCellularAlt to Color(0xFF8BC34A)
        "Fair" -> Icons.Default.SignalCellular4Bar to Color(0xFFFFC107)
        "Poor" -> Icons.Default.SignalCellular1Bar to Color(0xFFF44336)
        else -> Icons.Default.SignalCellularConnectedNoInternet0Bar to Color.Gray
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Signal: $quality",
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GuidanceCard(guidance: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                guidance,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
