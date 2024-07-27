package com.bhanu09.imagesteganography.Text

import android.graphics.Bitmap
import android.util.Log
import com.bhanu09.imagesteganography.Utils.Crypto
import com.bhanu09.imagesteganography.Utils.Utility

/**
 * This main class of the text steganography
 */
class ImageSteganography {
    @JvmField
    var message: String
    @JvmField
    var secret_key: String
    @JvmField
    var encrypted_message: String
    @JvmField
    var image: Bitmap
    @JvmField
    var encoded_image: Bitmap
    var encrypted_zip: ByteArray
    var isEncoded: Boolean
    var isDecoded: Boolean
    var isSecretKeyWrong: Boolean

    constructor() {
        this.isEncoded = false
        this.isDecoded = false
        this.isSecretKeyWrong = true
        this.message = ""
        this.secret_key = ""
        this.encrypted_message = ""
        this.image = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        this.encoded_image = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        this.encrypted_zip = ByteArray(0)
    }

    constructor(message: String, secret_key: String, image: Bitmap) {
        this.message = message
        this.secret_key = convertKeyTo128bit(secret_key)
        this.image = image

        /*try {
            this.encrypted_zip = Zipping.compress(message);
        } catch (Exception e) {
            e.printStackTrace();
        } */
        this.encrypted_zip = message.toByteArray()
        this.encrypted_message = encryptMessage(message, this.secret_key)

        this.isEncoded = false
        this.isDecoded = false
        this.isSecretKeyWrong = true

        this.encoded_image = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
    }

    constructor(secret_key: String, image: Bitmap) {
        this.secret_key = convertKeyTo128bit(secret_key)
        this.image = image

        this.isEncoded = false
        this.isDecoded = false
        this.isSecretKeyWrong = true

        this.message = ""
        this.encrypted_message = ""
        this.encoded_image = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        this.encrypted_zip = ByteArray(0)
    }

    companion object {
        //Tag for Log
        private val TAG: String = ImageSteganography::class.java.name

        private fun encryptMessage(message: String?, secret_key: String): String {
            Log.d(TAG, "Message : $message")

            var encrypted_message = ""
            if (message != null) {
                if (!Utility.isStringEmpty(secret_key)) {
                    try {
                        encrypted_message = Crypto.encryptMessage(message, secret_key)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    encrypted_message = message
                }
            }

            Log.d(TAG, "Encrypted_message : $encrypted_message")

            return encrypted_message
        }

        @JvmStatic
        fun decryptMessage(message: String?, secret_key: String?): String {
            var decrypted_message = ""
            if (message != null) {
                if (!Utility.isStringEmpty(secret_key)) {
                    try {
                        decrypted_message = Crypto.decryptMessage(message, secret_key)
                    } catch (e: Exception) {
                        Log.d(TAG, "Error : " + e.message + " , may be due to wrong key.")
                    }
                } else {
                    decrypted_message = message
                }
            }

            return decrypted_message
        }

        private fun convertKeyTo128bit(secret_key: String): String {
            var result = StringBuilder(secret_key)

            if (secret_key.length <= 16) {
                for (i in 0 until (16 - secret_key.length)) {
                    result.append("#")
                }
            } else {
                result = StringBuilder(result.substring(0, 15))
            }

            Log.d(TAG, "Secret Key Length : " + result.toString().toByteArray().size)

            return result.toString()
        }
    }
}
