package com.github.xe11.camxdemo.camera.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

private val ACCEPTABLE_FILE_EXTENSIONS = listOf("jpg", "jpeg", "png")

internal class CameraGalleryViewModel(
    private val sourceDirectory: File,
) : ViewModel() {

    private val _images = MutableStateFlow<List<File>>(emptyList())
    val images: StateFlow<List<File>> = _images

    init {
        viewModelScope.launch {
            val imageFiles: List<File> = getImageFiles(sourceDirectory)

            _images.value = imageFiles
        }
    }
}

private fun getImageFiles(directory: File): List<File> {
    return if (directory.exists() && directory.isDirectory) {
        directory
            .listFiles { file -> file.extension in ACCEPTABLE_FILE_EXTENSIONS }
            ?.toList()
            ?: emptyList()
    } else {
        emptyList()
    }
}
