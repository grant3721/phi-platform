package com.globaloutcomes.phi.presentation.scan

import com.globaloutcomes.phi.domain.model.Scan

data class ScanState(
    val patientId: String = "",
    val patientName: String = "",
    val isScanning: Boolean = false,
    val progress: Float = 0f,
    val elapsedSeconds: Int = 0,
    val totalSeconds: Int = 50,
    val signalQuality: String = "",
    val guidance: String = "",
    val faceDetected: Boolean = false,
    val errorMessage: String? = null,
    val completedScan: Scan? = null,
    val isComplete: Boolean = false
)
