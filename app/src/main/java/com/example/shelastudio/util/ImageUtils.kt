package com.example.shelastudio.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Utilities for handling images captured or picked by the user.
 *
 * Pipeline:
 *   Uri -> Bitmap -> rotate (EXIF) -> scale -> JPEG compress -> Base64
 */
object ImageUtils {

    private const val TAG = "ImageUtils"
    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 65
    private const val MAX_BASE64_SIZE_KB = 700

    /**
     * Loads an image from a Uri, corrects orientation, scales down, compresses,
     * and returns a Base64 string suitable for Firestore.
     */
    fun uriToCompressedBase64(context: Context, uri: Uri): String? {
        return try {
            Log.d(TAG, "Loading image from $uri")

            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return null
            val rawBytes = inputStream.readBytes()
            inputStream.close()
            Log.d(TAG, "Raw image size: ${rawBytes.size / 1024} KB")

            val bitmap = decodeScaledBitmap(rawBytes) ?: return null
            val rotation = readExifRotation(context, uri)
            val rotated = if (rotation != 0f) rotateBitmap(bitmap, rotation) else bitmap

            val compressed = compressToJpeg(rotated)
            Log.d(TAG, "Compressed size: ${compressed.size / 1024} KB")

            val base64 = Base64.encodeToString(compressed, Base64.NO_WRAP)
            val base64Kb = base64.length / 1024
            Log.d(TAG, "Base64 size: $base64Kb KB")

            if (base64Kb > MAX_BASE64_SIZE_KB) {
                Log.w(TAG, "Base64 exceeds limit, recompressing at quality 45")
                val smaller = compressToJpeg(rotated, quality = 45)
                return Base64.encodeToString(smaller, Base64.NO_WRAP)
            }

            base64
        } catch (e: Exception) {
            Log.e(TAG, "uriToCompressedBase64 failed", e)
            null
        }
    }

    private fun decodeScaledBitmap(bytes: ByteArray): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        val width = options.outWidth
        val height = options.outHeight
        Log.d(TAG, "Original dimensions: ${width}x$height")

        var sampleSize = 1
        while (width / sampleSize > MAX_DIMENSION || height / sampleSize > MAX_DIMENSION) {
            sampleSize *= 2
        }
        Log.d(TAG, "Using inSampleSize = $sampleSize")

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
    }

    private fun readExifRotation(context: Context, uri: Uri): Float {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (e: Exception) {
            Log.w(TAG, "Could not read EXIF orientation", e)
            0f
        }
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return source
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )
        if (rotated != source) source.recycle()
        return rotated
    }

    private fun compressToJpeg(bitmap: Bitmap, quality: Int = JPEG_QUALITY): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        return output.toByteArray()
    }

    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "base64ToBitmap failed", e)
            null
        }
    }
}