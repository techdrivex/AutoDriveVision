package com.autodrivevision.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autodrivevision.camera.CameraPreview
import com.autodrivevision.ml.DetectionResult
import com.autodrivevision.viewmodel.DetectionState
import com.autodrivevision.viewmodel.DetectionViewModel

@Composable
fun CameraScreen(
    viewModel: DetectionViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val detectionState by viewModel.detectionState.collectAsState()
    val isDetectionEnabled by viewModel.isDetectionEnabled.collectAsState()
    val detectionStats by viewModel.detectionStats.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            context = LocalLifecycleOwner.current.lifecycle,
            onFrameAvailable = { bitmap ->
                viewModel.detectObjects(bitmap)
            }
        )
        
        // Detection overlay
        when (val state = detectionState) {
            is DetectionState.Processing -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            is DetectionState.Success -> {
                DetectionOverlay(results = state.results)
            }
            is DetectionState.Error -> {
                ErrorDialog(
                    message = state.message,
                    onDismiss = { viewModel.resetState() }
                )
            }
            DetectionState.Idle -> {}
        }
        
        // Statistics panel
        StatisticsPanel(
            stats = detectionStats,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        
        // Toggle button
        FloatingActionButton(
            onClick = { viewModel.toggleDetection() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            containerColor = if (isDetectionEnabled) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.error
        ) {
            Icon(
                imageVector = if (isDetectionEnabled) Icons.Default.Stop
                            else Icons.Default.PlayArrow,
                contentDescription = if (isDetectionEnabled) "Stop Detection"
                                   else "Start Detection"
            )
        }
    }
}

@Composable
fun DetectionOverlay(results: List<DetectionResult>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        results.forEach { result ->
            val rect = result.boundingBox
            drawRect(
                color = Color.Red,
                topLeft = androidx.compose.ui.geometry.Offset(rect.left, rect.top),
                size = androidx.compose.ui.geometry.Size(rect.width(), rect.height()),
                style = Stroke(width = 2.dp.toPx())
            )
            
            // Draw label and confidence
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "${result.label} (${(result.confidence * 100).toInt()}%)",
                    rect.left,
                    rect.top - 10,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.RED
                        textSize = 30f
                    }
                )
            }
        }
    }
}

@Composable
fun StatisticsPanel(
    stats: DetectionStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Detection Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Total Detections: ${stats.totalDetections}")
            Text("Average Confidence: ${(stats.averageConfidence * 100).toInt()}%")
            
            if (stats.detectionsByClass.isNotEmpty()) {
                Text("By Class:", fontWeight = FontWeight.Bold)
                stats.detectionsByClass.forEach { (label, count) ->
                    Text("$label: $count")
                }
            }
        }
    }
}

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
} 