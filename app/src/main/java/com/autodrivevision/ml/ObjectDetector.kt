package com.autodrivevision.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class DetectionResult(
    val boundingBox: RectF,
    val label: String,
    val confidence: Float
)

class ObjectDetector(context: Context) {
    private val interpreter: Interpreter
    private val imageProcessor: ImageProcessor
    private val labels: List<String>
    
    companion object {
        private const val MODEL_PATH = "model.tflite"
        private const val LABELS_PATH = "labels.txt"
        private const val INPUT_SIZE = 300
        private const val NUM_DETECTIONS = 10
        private const val CONFIDENCE_THRESHOLD = 0.5f
    }
    
    init {
        // Load the model
        val model = FileUtil.loadMappedFile(context, MODEL_PATH)
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(model, options)
        
        // Create image processor
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f))
            .build()
        
        // Load labels
        labels = FileUtil.loadLabels(context, LABELS_PATH)
    }
    
    fun detect(bitmap: Bitmap): List<DetectionResult> {
        val tensorImage = TensorImage.fromBitmap(bitmap)
        val processedImage = imageProcessor.process(tensorImage)
        
        // Prepare output buffers
        val detectionBoxes = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        val detectionClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        val detectionScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        val numDetections = FloatArray(1)
        
        // Run inference
        interpreter.runForMultipleInputsOutputs(
            arrayOf(processedImage.buffer),
            mapOf(
                0 to detectionBoxes,
                1 to detectionClasses,
                2 to detectionScores,
                3 to numDetections
            )
        )
        
        // Process results
        val results = mutableListOf<DetectionResult>()
        val numDetectionsInt = numDetections[0].toInt()
        
        for (i in 0 until numDetectionsInt) {
            val confidence = detectionScores[0][i]
            if (confidence > CONFIDENCE_THRESHOLD) {
                val labelIndex = detectionClasses[0][i].toInt()
                if (labelIndex < labels.size) {
                    val box = detectionBoxes[0][i]
                    results.add(
                        DetectionResult(
                            boundingBox = RectF(box[1], box[0], box[3], box[2]),
                            label = labels[labelIndex],
                            confidence = confidence
                        )
                    )
                }
            }
        }
        
        return results
    }
    
    fun close() {
        interpreter.close()
    }
} 