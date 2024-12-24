package com.github.xe11.camxdemo.camera.capture.platform

import android.util.Log
import androidx.camera.core.ImageProxy
import com.github.xe11.camxdemo.BuildConfig
import com.google.mlkit.vision.face.Face

private const val TAG = "FaceParamsAssessor"

// TODO [Alexei Laban]: extract hardcoded numbers to constants/parameters
internal class FaceParamsAssessor {

    private val stabilityAssessor = FaceStabilityAssessor()

    fun isAcceptableImage(
        imageProxy: ImageProxy,
        faces: MutableList<Face>,
    ): Boolean {
        if (faces.size != 1) {
            stabilityAssessor.reset()
            // we don't need to save images with multiple faces
            return false
        }

        val face = faces.first()

        return isAcceptableImage(imageProxy, face)
    }

    private fun isAcceptableImage(
        imageProxy: ImageProxy,
        face: Face,
    ): Boolean {
        return noSmile(face) &&
            isEyesOpen(face) &&
            isFaceAngleCorrect(face) &&
            isFacePositioned(imageProxy, face) &&
            stabilityAssessor.assessImageIsStill(imageProxy, face)
    }

    private fun noSmile(face: Face): Boolean {
        val smilingProbability = face.smilingProbability ?: 0f

        return smilingProbability < 0.5f
    }

    private fun isEyesOpen(face: Face): Boolean {
        val eyesOpenThreshold = 0.8f
        val leftEyeOpenProbability = face.leftEyeOpenProbability ?: 1f
        val rightEyeOpenProbability = face.rightEyeOpenProbability ?: 1f

        return leftEyeOpenProbability > eyesOpenThreshold && rightEyeOpenProbability > eyesOpenThreshold
    }

    private fun isFacePositioned(
        imageProxy: ImageProxy,
        face: Face,
    ): Boolean {
        val faceCenterX = face.boundingBox.centerX()
        val faceCenterY = face.boundingBox.centerY()

        val faceCenterXPercent = faceCenterX.percentOf(imageProxy.widthRotationAdjusted)
        val faceCenterYPercent = faceCenterY.percentOf(imageProxy.heightRotationAdjusted)

        val faceWidthPercent = face.boundingBox.width().percentOf(imageProxy.widthRotationAdjusted)
        val faceHeightPercent = face.boundingBox.height().percentOf(imageProxy.heightRotationAdjusted)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Face center: x $faceCenterXPercent, y $faceCenterYPercent")
            Log.d(TAG, "Face size: w $faceWidthPercent, h $faceHeightPercent")
        }

        // TODO [Alexei Laban]: extract hardcoded numbers to constants/parameters
        val isCenteredX = faceCenterXPercent in 45..55
        val isCenteredY = faceCenterYPercent in 45..55
        val isFaceWideEnough = faceWidthPercent > 30

        return isCenteredX && isCenteredY && isFaceWideEnough
    }

    private fun isFaceAngleCorrect(face: Face): Boolean {
        val angleX = face.headEulerAngleX
        val angleY = face.headEulerAngleY
        val angleZ = face.headEulerAngleZ

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Face angles: X $angleX, Y $angleY, Z $angleZ")
        }

        // TODO [Alexei Laban]: extract hardcoded numbers to constants/parameters
        val angleThreshold = 5f
        val isAngleXAcceptable = angleX in -angleThreshold..angleThreshold
        val isAngleYAcceptable = angleY in -angleThreshold..angleThreshold
        val isAngleZAcceptable = angleZ in -angleThreshold..angleThreshold

        return isAngleXAcceptable && isAngleYAcceptable && isAngleZAcceptable
    }
}
