package com.github.xe11.camxdemo.camera.capture.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.xe11.camxdemo.camera.capture.data.CapturedImageSaver
import com.github.xe11.camxdemo.camera.capture.presentation.AutoCaptureCameraViewModel

class AutoCaptureCameraViewModelFactory internal constructor(
    private val context: AutoCaptureCameraActivity,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AutoCaptureCameraViewModel(CapturedImageSaver(context.applicationContext)) as T
    }
}
