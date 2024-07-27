package com.bhanu09.imagesteganography.Text

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log
import com.bhanu09.imagesteganography.Text.AsyncTaskCallback.TextDecodingCallback
import com.bhanu09.imagesteganography.Text.EncodeDecode.decodeMessage
import com.bhanu09.imagesteganography.Text.ImageSteganography.Companion.decryptMessage
import com.bhanu09.imagesteganography.Utils.Utility

/**
 * In this class, all those methods in EncodeDecode class are used to decode secret messages in images.
 * All the tasks will run in the background.
 */
class TextDecoding(activity: Activity?, private val textDecodingCallback: TextDecodingCallback) :
    AsyncTask<ImageSteganography, Void, ImageSteganography?>() {

    private val result: ImageSteganography = ImageSteganography()
    private var progressDialog: ProgressDialog? = ProgressDialog(activity)

    // Setting progress dialog if wanted
    fun setProgressDialog(progressDialog: ProgressDialog?) {
        this.progressDialog = progressDialog
    }

    // Pre-execution of method
    override fun onPreExecute() {
        super.onPreExecute()

        // Setting parameters of progress dialog
        progressDialog?.apply {
            setMessage("Loading, Please Wait...")
            setTitle("Decoding Message")
            isIndeterminate = true
            setCancelable(false)
            show()
        }
    }

    override fun onPostExecute(imageSteganography: ImageSteganography?) {
        super.onPostExecute(imageSteganography)

        // Dismiss progress dialog
        progressDialog?.dismiss()

        // Sending result to callback
        textDecodingCallback.onCompleteTextEncoding(result)
    }

    override fun doInBackground(vararg imageSteganographies: ImageSteganography): ImageSteganography? {
        // If it is not already decoded
        if (imageSteganographies.isNotEmpty()) {
            val imageSteganography = imageSteganographies[0]

            // Getting bitmap image from file
            val bitmap = imageSteganography.image ?: return null

            // Splitting images
            val srcEncodedList = Utility.splitImage(bitmap)

            // Decoding encrypted zipped message
            val decodedMessage = decodeMessage(srcEncodedList)

            Log.d(TAG, "Decoded_Message: $decodedMessage")

            // Text decoded = true
            if (!Utility.isStringEmpty(decodedMessage)) {
                result.isDecoded = true
            }

            // Decrypting the encoded message
            val decryptedMessage = decryptMessage(decodedMessage, imageSteganography.secret_key)
            Log.d(TAG, "Decrypted message: $decryptedMessage")

            // If decryptedMessage is null, it means that the secret key is wrong; otherwise, the secret key is right.
            if (!Utility.isStringEmpty(decryptedMessage)) {
                // Secret key provided is right
                result.isSecretKeyWrong = false

                // Set Results
                result.message = decryptedMessage

                // Free memory
                srcEncodedList.forEach { it.recycle() }

                // Java Garbage Collector
                System.gc()
            }
        }
        return result
    }

    companion object {
        // Tag for Log
        private val TAG: String = TextDecoding::class.java.name
    }
}
