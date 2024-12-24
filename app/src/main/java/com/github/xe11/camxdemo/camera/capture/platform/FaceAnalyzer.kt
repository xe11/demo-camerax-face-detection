package com.github.xe11.camxdemo.camera.capture.platform

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService

private const val TAG = "FaceAnalyzer"

internal class FaceAnalyzer(
    private val executor: ExecutorService,
    private val listeners: List<FaceAnalyzerListener>,
) : ImageAnalysis.Analyzer {

    private val faceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setExecutor(executor)
            .build()
        FaceDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()

            return
        }

        val inputImage: InputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces: MutableList<Face> ->
                listeners.forEach { listener ->
                    listener.onFrameReceived(imageProxy, faces)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

interface FaceAnalyzerListener {

    fun onFrameReceived(
        imageProxy: ImageProxy,
        faces: MutableList<Face>,
    )
}
