package com.github.xe11.camxdemo.camera.capture.platform

import androidx.camera.core.ImageProxy

fun Int.percentOf(scale: Int): Int = this * 100 / scale

val ImageProxy.widthRotationAdjusted: Int
    get() = if (imageInfo.rotationDegrees % 180 == 0) width else height

val ImageProxy.heightRotationAdjusted: Int
    get() = if (imageInfo.rotationDegrees % 180 == 0) height else width
