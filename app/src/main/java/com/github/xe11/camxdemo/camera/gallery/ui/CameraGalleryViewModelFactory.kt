package com.github.xe11.camxdemo.camera.gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.xe11.camxdemo.camera.gallery.presentation.CameraGalleryViewModel
import java.io.File

internal class CameraGalleryViewModelFactory(
    private val sourceDirectory: File,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraGalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraGalleryViewModel(sourceDirectory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class $modelClass")
    }
}
