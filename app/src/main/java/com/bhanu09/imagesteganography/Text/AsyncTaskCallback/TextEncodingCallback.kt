package com.bhanu09.imagesteganography.Text.AsyncTaskCallback

import com.bhanu09.imagesteganography.Text.ImageSteganography

/**
 * This the callback interface for TextEncoding AsyncTask.
 */
interface TextEncodingCallback {

    fun onStartTextEncoding()

    fun onCompleteTextEncoding(result: ImageSteganography?)

    companion object {
        var encodeStatus: String = ""
    }
}
