package com.github.xe11.camxdemo.camera.gallery.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.github.xe11.camxdemo.camera.gallery.presentation.CameraGalleryViewModel
import com.github.xe11.camxdemo.utils.ui.collectLifecycleAware
import com.github.xe11.camxdemo.databinding.ActivityCameraGalleryBinding
import java.io.File

internal class CameraGalleryActivity : AppCompatActivity() {

    private lateinit var sourceDirectory: File

    private lateinit var binding: ActivityCameraGalleryBinding
    private lateinit var adapter: ImagePagerAdapter

    private val viewModel: CameraGalleryViewModel
        by viewModels { CameraGalleryViewModelFactory(sourceDirectory) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val directory = intent.sourceDirectory
        if (directory == null) {
            finish()

            return
        }
        sourceDirectory = directory

        initViews()
        initViewModel()
    }

    private fun initViews() {
        binding = ActivityCameraGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding)

        adapter = ImagePagerAdapter()
        binding.viewPager.adapter = adapter
    }

    private fun initViewModel() {
        viewModel.images.collectLifecycleAware(this) { images ->
            adapter.submitList(images)
        }
    }

    private fun setupEdgeToEdge(binding: ActivityCameraGalleryBinding) {
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

    private val Intent.sourceDirectory: File?
        get() {
            val directoryPath = getStringExtra(EXTRA_DIRECTORY_PATH)
                .takeIf { path -> !path.isNullOrEmpty() }

            return directoryPath?.let {
                File(directoryPath)
                    .takeIf { file -> file.exists() && file.isDirectory }
            }
        }

    companion object {
        private const val EXTRA_DIRECTORY_PATH = "DIRECTORY_PATH"

        fun getIntent(context: Context, directory: File): Intent {
            return Intent(context, CameraGalleryActivity::class.java).apply {
                putExtra(EXTRA_DIRECTORY_PATH, directory.absolutePath)
            }
        }
    }
}
