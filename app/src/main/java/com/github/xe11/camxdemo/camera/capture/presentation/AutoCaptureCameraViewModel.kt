package com.github.xe11.camxdemo.camera.capture.presentation

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.xe11.camxdemo.camera.capture.data.CapturedImageSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "AutoCaptureCameraViewModel"

private const val IMAGES_TO_CAPTURE = 5

internal class AutoCaptureCameraViewModel(
    private val capturedImageSaver: CapturedImageSaver,
) : ViewModel() {

    sealed interface State {
        data object RequestPermissions : State
        data object PermissionsError : State
        data object CameraCapture : State
        data class Done(val resultDir: File) : State
    }

    sealed interface PermissionsResult {
        data object Granted : PermissionsResult
        data object Denied : PermissionsResult
        data object DeniedNeverAskAgain : PermissionsResult
    }

    private val bitmaps = MutableSharedFlow<Bitmap>()

    private val permissionsGranted = MutableStateFlow<PermissionsResult?>(null)

    private val _cameraSelector = MutableStateFlow(CameraSelector.DEFAULT_FRONT_CAMERA)
    val cameraSelector: StateFlow<CameraSelector> = _cameraSelector

    private val imagesCaptured = MutableStateFlow(false)

    val state: Flow<State> =
        combine(
            permissionsGranted,
            imagesCaptured,
        ) { permissions, captured ->
            when (permissions) {
                null -> State.RequestPermissions
                PermissionsResult.Denied -> State.PermissionsError
                PermissionsResult.DeniedNeverAskAgain -> State.PermissionsError
                PermissionsResult.Granted -> {
                    if (captured) {
                        State.Done(capturedImageSaver.directory())
                    } else {
                        State.CameraCapture
                    }
                }
            }
        }
            .distinctUntilChanged()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            capturedImageSaver.prepareGalleryDirectory()

            bitmaps
                .buffer(capacity = IMAGES_TO_CAPTURE)
                .map { bitmap -> capturedImageSaver.saveImage(bitmap) }
                .filterNotNull()
                .take(IMAGES_TO_CAPTURE)
                .collect {}

            imagesCaptured.value = true
        }
    }

    fun onPermissionsResult(permissionsResult: PermissionsResult) {
        viewModelScope.launch {
            permissionsGranted.value = permissionsResult
        }
    }

    fun onImageCaptured(bitmap: Bitmap) {
        Log.w(TAG, "onImageCaptured: $bitmap")
        viewModelScope.launch {
            bitmaps.emit(bitmap)
        }
    }

    fun onSwitchCameraClicked() {
        _cameraSelector.value = if (_cameraSelector.value == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }
}
