package com.autodrivevision.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autodrivevision.R
import com.autodrivevision.ml.DetectionResult
import com.autodrivevision.ml.ObjectDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class DetectionState {
    object Idle : DetectionState()
    object Processing : DetectionState()
    data class Success(val results: List<DetectionResult>) : DetectionState()
    data class Error(val message: String) : DetectionState()
}

data class DetectionStats(
    val totalDetections: Int = 0,
    val detectionsByClass: Map<String, Int> = emptyMap(),
    val averageConfidence: Float = 0f
)

class DetectionViewModel(application: Application) : AndroidViewModel(application) {
    private val objectDetector: ObjectDetector
    private val _detectionState = MutableStateFlow<DetectionState>(DetectionState.Idle)
    private val _isDetectionEnabled = MutableStateFlow(true)
    private val _detectionStats = MutableStateFlow(DetectionStats())
    private val alertSound: MediaPlayer
    
    val detectionState: StateFlow<DetectionState> = _detectionState
    val isDetectionEnabled: StateFlow<Boolean> = _isDetectionEnabled
    val detectionStats: StateFlow<DetectionStats> = _detectionStats
    
    init {
        try {
            objectDetector = ObjectDetector(application)
            alertSound = MediaPlayer.create(application, R.raw.alert)
        } catch (e: IOException) {
            throw RuntimeException("Failed to initialize object detector", e)
        }
    }
    
    fun toggleDetection() {
        _isDetectionEnabled.value = !_isDetectionEnabled.value
    }
    
    fun detectObjects(bitmap: Bitmap) {
        if (!_isDetectionEnabled.value) return
        
        viewModelScope.launch {
            try {
                _detectionState.value = DetectionState.Processing
                val results = objectDetector.detect(bitmap)
                _detectionState.value = DetectionState.Success(results)
                
                // Update statistics
                updateStats(results)
                
                // Check for important detections and play alert
                checkImportantDetections(results)
            } catch (e: Exception) {
                _detectionState.value = DetectionState.Error("Detection failed: ${e.message}")
            }
        }
    }
    
    private fun updateStats(results: List<DetectionResult>) {
        val newStats = DetectionStats(
            totalDetections = results.size,
            detectionsByClass = results.groupBy { it.label }
                .mapValues { it.value.size },
            averageConfidence = results.map { it.confidence }
                .average()
                .toFloat()
        )
        _detectionStats.value = newStats
    }
    
    private fun checkImportantDetections(results: List<DetectionResult>) {
        val importantClasses = setOf("person", "traffic light", "stop sign")
        val hasImportantDetection = results.any { 
            it.label in importantClasses && it.confidence > 0.7f 
        }
        
        if (hasImportantDetection) {
            playAlert()
        }
    }
    
    private fun playAlert() {
        if (!alertSound.isPlaying) {
            alertSound.start()
        }
    }
    
    fun resetState() {
        _detectionState.value = DetectionState.Idle
        _detectionStats.value = DetectionStats()
    }
    
    override fun onCleared() {
        super.onCleared()
        objectDetector.close()
        alertSound.release()
    }
} 