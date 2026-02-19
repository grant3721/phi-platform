package com.globaloutcomes.phi.presentation.scan

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.globaloutcomes.phi.domain.scan.ScanSessionState
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoProgressRing
import com.globaloutcomes.phi.presentation.components.ButtonVariant
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Scan Screen - Health vitals scanning with camera
 * Features:
 * - CameraX preview with face detection overlay
 * - 30-second scan with progress ring
 * - Real-time guidance messages
 * - Integration with ScanEngine (stub or real SDK)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    patientId: String,
    onNavigateToResult: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Navigate to result when scan complete
    LaunchedEffect(state.scanComplete) {
        if (state.scanComplete && state.completedScanId != null) {
            onNavigateToResult(state.completedScanId!!)
        }
    }

    // Request camera permission on first launch
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Health Scan")
                        state.patient?.let {
                            Text(
                                text = it.fullName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !state.scanInProgress
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    LoadingState()
                }
                state.error != null -> {
                    ErrorState(
                        error = state.error!!,
                        onDismiss = viewModel::dismissError,
                        onRetry = { viewModel.loadPatient() }
                    )
                }
                !cameraPermissionState.status.isGranted -> {
                    CameraPermissionRequired(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
                else -> {
                    ScanContent(
                        state = state,
                        onStartScan = viewModel::startScan,
                        onStopScan = viewModel::stopScan
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    error: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            GoButton(
                text = "Retry",
                onClick = onRetry,
                variant = ButtonVariant.PRIMARY
            )
        }
    }
}

@Composable
private fun CameraPermissionRequired(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Camera Access Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "The health scan requires camera access to measure vital signs from your face.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            GoButton(
                text = "Grant Camera Permission",
                onClick = onRequestPermission,
                variant = ButtonVariant.PRIMARY,
                icon = Icons.Filled.CheckCircle
            )
        }
    }
}

@Composable
private fun ScanContent(
    state: ScanScreenState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Face Detection Overlay (Green oval guide)
        FaceDetectionOverlay(
            sessionState = state.sessionState,
            modifier = Modifier.fillMaxSize()
        )

        // Scan Status UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: Guidance Messages
            ScanGuidanceCard(sessionState = state.sessionState)

            Spacer(modifier = Modifier.weight(1f))

            // Center: Progress Ring (during scan)
            if (state.scanInProgress && state.sessionState is ScanSessionState.Measuring) {
                ScanProgressIndicator(
                    sessionState = state.sessionState as ScanSessionState.Measuring
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom: Start/Stop Button
            ScanControlButton(
                scanInProgress = state.scanInProgress,
                sessionState = state.sessionState,
                onStartScan = onStartScan,
                onStopScan = onStopScan
            )
        }
    }
}

@Composable
private fun FaceDetectionOverlay(
    sessionState: ScanSessionState?,
    modifier: Modifier = Modifier
) {
    val isValid = (sessionState as? ScanSessionState.ImageValidity)?.isValid == true

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2.5f
        val ovalWidth = size.width * 0.6f
        val ovalHeight = size.height * 0.45f

        // Draw face guide oval
        drawOval(
            color = if (isValid) Color.Green else Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(
                centerX - ovalWidth / 2,
                centerY - ovalHeight / 2
            ),
            size = androidx.compose.ui.geometry.Size(ovalWidth, ovalHeight),
            style = Stroke(width = 4f)
        )
    }
}

@Composable
private fun ScanGuidanceCard(
    sessionState: ScanSessionState?
) {
    val (icon, message, color) = when (sessionState) {
        is ScanSessionState.Initializing -> {
            Triple(Icons.Filled.Info, "Initializing camera...", MaterialTheme.colorScheme.primary)
        }
        is ScanSessionState.ImageValidity -> {
            if (sessionState.isValid) {
                Triple(Icons.Filled.CheckCircle, "Perfect! Hold still", Color.Green)
            } else {
                Triple(Icons.Filled.Warning, sessionState.guidance ?: "Position your face", Color(0xFFFFA500))
            }
        }
        is ScanSessionState.Measuring -> {
            Triple(Icons.Filled.Check, sessionState.currentVital ?: "Measuring...", MaterialTheme.colorScheme.primary)
        }
        else -> {
            Triple(Icons.Filled.Info, "Position your face in the oval", MaterialTheme.colorScheme.onSurface)
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScanProgressIndicator(
    sessionState: ScanSessionState.Measuring
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circular Progress Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            GoProgressRing(
                progress = sessionState.progress,
                size = 200.dp,
                strokeWidth = 12.dp
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${sessionState.timeRemaining}s",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Current vital sign being measured
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = sessionState.currentVital ?: "Measuring...",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun ScanControlButton(
    scanInProgress: Boolean,
    sessionState: ScanSessionState?,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    val canStartScan = !scanInProgress &&
            (sessionState == null || sessionState is ScanSessionState.ImageValidity)

    if (scanInProgress) {
        GoButton(
            text = "Stop Scan",
            onClick = onStopScan,
            variant = ButtonVariant.DANGER,
            icon = Icons.Filled.Close,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
    } else {
        GoButton(
            text = "Start Scan",
            onClick = onStartScan,
            enabled = canStartScan,
            variant = ButtonVariant.PRIMARY,
            icon = Icons.Filled.PlayArrow,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
    }
}
