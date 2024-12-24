package com.github.xe11.camxdemo.camera.capture.platform

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import com.github.xe11.camxdemo.BuildConfig
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService

private const val TAG = "FaceBoundsEmitter"

internal class FaceBoundsEmitter(
    executor: ExecutorService,
) : FaceAnalyzerListener {
    private val scope: CoroutineScope = CoroutineScope(executor.asCoroutineDispatcher())

    private val faceBoundsFlow = MutableSharedFlow<List<Rect>>()
    val faceBounds: SharedFlow<List<Rect>> = faceBoundsFlow.asSharedFlow()

    override fun onFrameReceived(imageProxy: ImageProxy, faces: MutableList<Face>) {
        emitFaceBounds(imageProxy, faces)
    }

    private fun emitFaceBounds(
        imageProxy: ImageProxy,
        faces: MutableList<Face>,
    ) {
        val faceBounds = faces.map { face ->
            faceRect(face, imageProxy)
        }

        scope.launch {
            faceBoundsFlow.emit(faceBounds)
        }
    }

    private fun faceRect(
        face: Face,
        imageProxy: ImageProxy
    ): Rect {
        val boundingBox = face.boundingBox

        val imageHeight: Int = imageProxy.heightRotationAdjusted
        val imageWidth: Int = imageProxy.widthRotationAdjusted

        val left = boundingBox.left.percentOf(imageWidth)
        val top = boundingBox.top.percentOf(imageHeight)
        val right = boundingBox.right.percentOf(imageWidth)
        val bottom = boundingBox.bottom.percentOf(imageHeight)

        val rect = Rect(left, top, right, bottom)

        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "face.boundingBox: w${face.boundingBox.width()} h${face.boundingBox.height()}  @ W${imageWidth} H${imageHeight} a${imageProxy.imageInfo.rotationDegrees}  -->  w${rect.width()} h${rect.height()}"
            )
        }
        return rect
    }
}
