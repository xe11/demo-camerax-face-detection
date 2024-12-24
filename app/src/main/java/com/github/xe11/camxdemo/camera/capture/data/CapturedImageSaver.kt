package com.github.xe11.camxdemo.camera.capture.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "CapturedImageSaver"

private const val TMP_PHOTOS_DIR = "tmp_photos"

internal class CapturedImageSaver(
    context: Context,
) {
    private val tmpDir = File(context.cacheDir, TMP_PHOTOS_DIR)
    private val photosDirName = "session_${System.currentTimeMillis()}"
    private val photosDir = File(tmpDir, photosDirName)

    suspend fun prepareGalleryDirectory(): File = withContext(Dispatchers.IO) {
        tmpDir.apply {
            if (exists()) {
                deleteRecursively()
            }
            mkdirs()
        }
        photosDir.mkdirs()

        photosDir
    }

    suspend fun saveImage(bitmap: Bitmap): File? = withContext(Dispatchers.IO) {
        saveToFile(bitmap)
    }

    private fun saveToFile(bitmap: Bitmap): File? {
        return try {
            val file = generateFileName(photosDir)
            file.outputStream().use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
            }
            bitmap.recycle()

            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image", e)
            null
        }
    }

    private fun generateFileName(parentDir: File): File {
        val name = "image_${System.currentTimeMillis()}_${System.nanoTime()}.jpg"

        return File(parentDir, name)
    }

    fun directory(): File {
        return photosDir
    }
}
