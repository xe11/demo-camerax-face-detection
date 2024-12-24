package com.github.xe11.camxdemo.camera.capture.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.github.xe11.camxdemo.camera.capture.platform.FaceAnalyzer
import com.github.xe11.camxdemo.camera.capture.platform.FaceBoundsEmitter
import com.github.xe11.camxdemo.camera.capture.platform.FaceCaptor
import com.github.xe11.camxdemo.camera.capture.presentation.AutoCaptureCameraViewModel
import com.github.xe11.camxdemo.camera.capture.presentation.AutoCaptureCameraViewModel.PermissionsResult
import com.github.xe11.camxdemo.camera.capture.presentation.AutoCaptureCameraViewModel.State
import com.github.xe11.camxdemo.camera.gallery.ui.CameraGalleryActivity
import com.github.xe11.camxdemo.utils.ui.collectLifecycleAware
import com.github.xe11.camxdemo.databinding.ActivityAutoCaptureCameraBinding
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "AutoCaptureCamera"

internal class AutoCaptureCameraActivity : AppCompatActivity() {

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var binding: ActivityAutoCaptureCameraBinding
    private lateinit var preview: Preview
    private lateinit var faceAnalyzer: FaceAnalyzer

    private val viewModel: AutoCaptureCameraViewModel by viewModels { AutoCaptureCameraViewModelFactory(context = this) }

    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionsResult = when {
                permissions.all { it.value } -> PermissionsResult.Granted
                permissions.any { !shouldShowRequestPermissionRationale(it.key) } -> PermissionsResult.DeniedNeverAskAgain
                else -> PermissionsResult.Denied
            }
            viewModel.onPermissionsResult(permissionsResult)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutoCaptureCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding)

        preview = Preview.Builder().build().also { preview ->
            preview.surfaceProvider = binding.previewView.surfaceProvider
        }

        val faceBoundsEmitter = FaceBoundsEmitter(analysisExecutor)
        val faceCaptor = FaceCaptor(analysisExecutor)
        faceAnalyzer = FaceAnalyzer(
            analysisExecutor,
            listOf(
                faceBoundsEmitter,
                faceCaptor,
            )
        )

        faceBoundsEmitter.faceBounds.collectLifecycleAware(this) { bounds ->
            binding.faceOverlayView.updateFaceBounds(bounds)
        }

        faceCaptor.bitmaps.collectLifecycleAware(this) { imageFile ->
            Log.d(TAG, "faceAnalyzer: Images: $imageFile")
            viewModel.onImageCaptured(imageFile)
        }

        binding.switchCameraButton.setOnClickListener {
            viewModel.onSwitchCameraClicked()
        }

        viewModel.state.collectLifecycleAware(this) { state ->
            Log.d(TAG, "State: $state")
            when (state) {
                is State.RequestPermissions -> requestPermissions()
                is State.PermissionsError -> showPermissionError()
                is State.CameraCapture -> startCamera()
                is State.Done -> navigateToNextScreen(state.resultDir)
            }
        }
    }

    private fun startCamera() {
        val activity = this
        lifecycleScope.launch {
            try {
                val cameraProvider = ProcessCameraProvider.awaitInstance(activity)

                viewModel.cameraSelector.collectLifecycleAware(activity) { cameraSelector ->
                    bindCameraUseCases(cameraProvider, cameraSelector)
                }
            } catch (e: Exception) {
                Toast.makeText(activity, "Failed to init Camera.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun bindCameraUseCases(
        cameraProvider: ProcessCameraProvider,
        cameraSelector: CameraSelector,
    ) {
        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { imageAnalysis ->
                imageAnalysis.setAnalyzer(cameraExecutor, faceAnalyzer)
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner = this,
                cameraSelector = cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    private fun navigateToNextScreen(mediaDir: File) {
        startActivity(CameraGalleryActivity.getIntent(context = this, mediaDir))
        finish()
    }

    private fun requestPermissions() {
        requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun showPermissionError() {
        Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupEdgeToEdge(binding: ActivityAutoCaptureCameraBinding) {
        enableEdgeToEdge()
        setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
                bottomMargin = insets.bottom
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        analysisExecutor.shutdown()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
