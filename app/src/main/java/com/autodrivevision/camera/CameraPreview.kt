package com.autodrivevision.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    private val onFrameAvailable: (Bitmap) -> Unit
) : PreviewView(context) {
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var lastFrameTime = 0L
    private val frameInterval = TimeUnit.SECONDS.toNanos(1) / 15 // 15 FPS
    
    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        startCamera(lifecycleOwner)
    }
    
    private fun startCamera(lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(surfaceProvider)
                }
            
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    private fun processImage(imageProxy: ImageProxy) {
        val currentTime = System.nanoTime()
        if (currentTime - lastFrameTime >= frameInterval) {
            try {
                val bitmap = imageProxy.toBitmap()
                onFrameAvailable(bitmap)
                lastFrameTime = currentTime
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process image", e)
            }
        }
        imageProxy.close()
    }
    
    fun cleanup() {
        cameraExecutor.shutdown()
    }
    
    companion object {
        private const val TAG = "CameraPreview"
    }
} 