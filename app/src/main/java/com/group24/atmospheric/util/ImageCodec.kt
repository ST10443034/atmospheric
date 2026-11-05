package com.group24.atmospheric.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/** Shrinks and encodes images so a profile photo is small enough to live in a Firestore document. */
object ImageCodec {
    /** Longest side of the stored photo, in pixels. */
    const val MAX_SIDE = 256

    /** Returns (width, height) scaled down so the longer side is at most [maxSide]; never upscales. */
    fun scaledSize(width: Int, height: Int, maxSide: Int = MAX_SIDE): Pair<Int, Int> {
        val longest = max(width, height)
        if (longest <= maxSide || longest == 0) return width to height
        val ratio = maxSide.toFloat() / longest
        return (width * ratio).roundToInt().coerceAtLeast(1) to (height * ratio).roundToInt().coerceAtLeast(1)
    }

    /** Scales [bitmap] and returns it as a Base64 JPEG string (about 15-30 KB). */
    fun toBase64Jpeg(bitmap: Bitmap, quality: Int = 80): String {
        val (w, h) = scaledSize(bitmap.width, bitmap.height)
        val scaled = if (w == bitmap.width && h == bitmap.height) bitmap
        else Bitmap.createScaledBitmap(bitmap, w, h, true)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Decodes a picked image without loading the full-resolution bitmap into memory
     * (two passes: read bounds, then decode with a power-of-two sample size).
     */
    fun decodeSampled(open: () -> java.io.InputStream?, maxSide: Int = MAX_SIDE * 2): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open()?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return open()?.use { BitmapFactory.decodeStream(it, null, opts) }
    }

    /** Decodes a Base64 string from [toBase64Jpeg]; returns null if it is empty or corrupt. */
    fun fromBase64(encoded: String): Bitmap? {
        if (encoded.isBlank()) return null
        return try {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
