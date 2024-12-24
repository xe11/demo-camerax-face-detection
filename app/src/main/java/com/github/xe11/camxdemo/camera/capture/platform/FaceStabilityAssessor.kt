package com.github.xe11.camxdemo.camera.capture.platform

import android.util.Log
import androidx.camera.core.ImageProxy
import com.github.xe11.camxdemo.BuildConfig
import com.google.mlkit.vision.face.Face
import kotlin.math.absoluteValue

private const val TAG = "FaceParamsAssessor"

private const val STABILITY_THRESHOLD_MILLIS = 1000L

internal class FaceStabilityAssessor(
    private val movementDetectionThresholdPercent: Int = 5,
) {
    private var startX = 0
    private var startY = 0
    private var endX = 0
    private var endY = 0
    private var timestamp = 0L

    fun assessImageIsStill(imageProxy: ImageProxy, face: Face): Boolean {
        val diffScale = (imageProxy.width + imageProxy.height) / 2

        return assessImageIsStill(
            startX = face.boundingBox.left.percentOf(diffScale),
            startY = face.boundingBox.top.percentOf(diffScale),
            endX = face.boundingBox.right.percentOf(diffScale),
            endY = face.boundingBox.bottom.percentOf(diffScale),
            timestamp = imageProxy.imageInfo.timestamp.nanoToMillis(),
        )
    }

    private fun assessImageIsStill(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        timestamp: Long,
    ): Boolean {
        val dXStart = (startX - this.startX).absoluteValue
        val dYStart = (startY - this.startY).absoluteValue
        val dXEnd = (endX - this.endX).absoluteValue
        val dYEnd = (endY - this.endY).absoluteValue

        val isFaceStill = dXStart < movementDetectionThresholdPercent &&
            dYStart < movementDetectionThresholdPercent &&
            dXEnd < movementDetectionThresholdPercent &&
            dYEnd < movementDetectionThresholdPercent

        val dTime = timestamp - this.timestamp

        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "in:: Start: $startX, $startY, End: $endX, $endY" +
                    " \n" +
                    "Delta:: Start: $dXStart, $dYStart, End: $dXEnd, $dYEnd  " +
                    "\n" +
                    " --> still: $isFaceStill time: ${dTime >= STABILITY_THRESHOLD_MILLIS}"
            )
        }

        this.startX = startX
        this.startY = startY
        this.endX = endX
        this.endY = endY

        return if (isFaceStill) {
            dTime >= STABILITY_THRESHOLD_MILLIS
        } else {
            this.timestamp = timestamp
            false
        }
    }

    fun reset() {
        startX = 0
        startY = 0
        endX = 0
        endY = 0
        timestamp = 0
    }
}

private fun Long.nanoToMillis(): Long = this / 1_000_000
