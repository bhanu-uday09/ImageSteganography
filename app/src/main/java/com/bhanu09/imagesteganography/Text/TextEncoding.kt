package com.bhanu09.imagesteganography.Text

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log
import com.bhanu09.imagesteganography.Text.AsyncTaskCallback.TextEncodingCallback
import com.bhanu09.imagesteganography.Text.EncodeDecode.ProgressHandler
import com.bhanu09.imagesteganography.Text.EncodeDecode.encodeMessage
import com.bhanu09.imagesteganography.Utils.Utility

/**
 * In this class all those methods in EncodeDecode class are used to encode a secret message in an image.
 * All the tasks will run in the background.
 */
class TextEncoding(
    activity: Activity?, // Callback interface for AsyncTask
    private val callbackInterface: TextEncodingCallback
) : AsyncTask<ImageSteganography, Int, ImageSteganography?>() {

    // Making result object
    private val result = ImageSteganography()
    private var maximumProgress = 0
    private val progressDialog: ProgressDialog? = ProgressDialog(activity)

    // Pre-execution of method
    override fun onPreExecute() {
        super.onPreExecute()

        // Setting parameters of progress dialog
        progressDialog?.apply {
            setMessage("Loading, Please Wait...")
            setTitle("Encoding Message")
            isIndeterminate = false
            setCancelable(false)
            show()
        }
    }

    override fun onPostExecute(imageSteganography: ImageSteganography?) {
        super.onPostExecute(imageSteganography)

        // Dismiss progress dialog
        progressDialog?.dismiss()

        // Sending result to callback interface
        callbackInterface.onCompleteTextEncoding(result)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

        // Updating progress dialog
        values[0]?.let { progressDialog?.incrementProgressBy(it) }
    }

    override fun doInBackground(vararg imageSteganographies: ImageSteganography): ImageSteganography? {
        maximumProgress = 0

        if (imageSteganographies.isNotEmpty()) {
            val textSteganography = imageSteganographies[0]

            // Getting image bitmap
            val bitmap = textSteganography.image ?: return null

            // Getting height and width of the original image
            val originalHeight = bitmap.height
            val originalWidth = bitmap.width

            // Splitting bitmap
            val srcList = Utility.splitImage(bitmap)

            // Encoding encrypted compressed message into image
            val encodedList = encodeMessage(
                srcList,
                textSteganography.encrypted_message,
                object : ProgressHandler {
                    // Progress Handler
                    override fun setTotal(tot: Int) {
                        maximumProgress = tot
                        progressDialog?.max = maximumProgress
                        Log.d(TAG, "Total Length: $tot")
                    }

                    override fun increment(inc: Int) {
                        publishProgress(inc)
                    }

                    override fun finished() {
                        Log.d(TAG, "Message Encoding end....")
                        progressDialog?.isIndeterminate = true
                    }
                })

            // Free memory
            for (bitm in srcList) bitm.recycle()

            // Java Garbage collector
            System.gc()

            // Merging the split encoded image
            val srcEncoded = Utility.mergeImage(encodedList, originalHeight, originalWidth)

            // Setting encoded image to result
            result.encoded_image = srcEncoded
            result.isEncoded = true
        }

        return result
    }

    companion object {
        // Tag for Log
        private val TAG: String = TextEncoding::class.java.name
    }
}
