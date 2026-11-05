package com.group24.atmospheric

import com.group24.atmospheric.util.ImageCodec
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifies the size maths used to shrink profile photos before storing them in Firestore. */
class ImageCodecTest {

    @Test
    fun smallImage_isNotUpscaled() {
        assertEquals(100 to 80, ImageCodec.scaledSize(100, 80))
    }

    @Test
    fun largeLandscape_scalesLongestSideToMax() {
        assertEquals(256 to 128, ImageCodec.scaledSize(2048, 1024))
    }

    @Test
    fun largePortrait_keepsAspectRatio() {
        assertEquals(128 to 256, ImageCodec.scaledSize(1000, 2000))
    }

    @Test
    fun extremeAspectRatio_neverCollapsesToZero() {
        val (w, h) = ImageCodec.scaledSize(10000, 1)
        assertEquals(256, w)
        assertEquals(1, h)
    }
}
