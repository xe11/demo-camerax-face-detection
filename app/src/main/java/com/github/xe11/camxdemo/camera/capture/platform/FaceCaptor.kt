package com.github.xe11.camxdemo.camera.capture.platform

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService

private const val TAG = "FaceCaptor"

internal class FaceCaptor(
    executor: ExecutorService,
) : FaceAnalyzerListener {

    private val scope: CoroutineScope = CoroutineScope(executor.asCoroutineDispatcher())

    private val faceParamsAssessor = FaceParamsAssessor()

    private val bitmapsFlow = MutableSharedFlow<Bitmap>()
    val bitmaps: Flow<Bitmap> = bitmapsFlow.asSharedFlow()

    override fun onFrameReceived(imageProxy: ImageProxy, faces: MutableList<Face>) {
        if (bitmapsFlow.subscriptionCount.value <= 0) {
            return
        }

        val accepted = faceParamsAssessor.isAcceptableImage(imageProxy, faces)
        if (accepted) {
            handleAcceptedImage(imageProxy)
        }
    }

    private fun handleAcceptedImage(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val bitmap: Bitmap
        try {
            bitmap = imageProxy.toBitmap()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert image to bitmap", e)
            return
        }

        scope.launch {
            val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees)
            bitmap.recycle()
            bitmapsFlow.emit(rotatedBitmap)
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
