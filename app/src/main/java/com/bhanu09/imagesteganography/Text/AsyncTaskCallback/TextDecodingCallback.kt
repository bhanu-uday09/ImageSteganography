package com.bhanu09.imagesteganography.Text.AsyncTaskCallback

import com.bhanu09.imagesteganography.Text.ImageSteganography

/**
 * This the callback interface for TextDecoding AsyncTask.
 */
interface TextDecodingCallback {

    fun onStartTextEncoding()

    fun onCompleteTextEncoding(result: ImageSteganography?)
}
