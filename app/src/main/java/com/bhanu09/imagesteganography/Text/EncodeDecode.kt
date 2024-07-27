package com.bhanu09.imagesteganography.Text

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.bhanu09.imagesteganography.Utils.Utility
import java.nio.charset.Charset
import java.util.Vector

internal object EncodeDecode {
    private val TAG: String = EncodeDecode::class.java.name

    //start and end message constants
    private const val END_MESSAGE_CONSTANT = "#!@"
    private const val START_MESSAGE_CONSTANT = "@!#"
    private val binary = intArrayOf(16, 8, 0)
    private val andByte = byteArrayOf(0xC0.toByte(), 0x30, 0x0C, 0x03)
    private val toShift = intArrayOf(6, 4, 2, 0)

    /**
     * This method represents the core of 2-bit encoding
     *
     * @return : byte encoded pixel array
     * @parameter : integerPixelArray {The integer RGB array}
     * @parameter : imageColumns {Image width}
     * @parameter : imageRows {Image height}
     * @parameter : messageEncodingStatus {object}
     * @parameter : progressHandler {A handler interface, for the progress bar}
     */
    private fun encodeMessage(
        integerPixelArray: IntArray, imageColumns: Int, imageRows: Int,
        messageEncodingStatus: MessageEncodingStatus, progressHandler: ProgressHandler?
    ): ByteArray {
        //denotes RGB channels
        val channels = 3
        var shiftIndex = 4

        //creating result byte array
        val result = ByteArray(imageRows * imageColumns * channels)
        var resultIndex = 0

        for (row in 0 until imageRows) {
            for (col in 0 until imageColumns) {
                //2D matrix in 1D
                val element = row * imageColumns + col
                var tmp: Byte

                for (channelIndex in 0 until channels) {
                    if (!messageEncodingStatus.isMessageEncoded) {
                        // Shifting integer value by 2 in left and replacing the two least significant digits with the message_byte_array values..
                        tmp =
                            ((((integerPixelArray[element] shr binary[channelIndex]) and 0xFF) and 0xFC) or ((messageEncodingStatus.byteArrayMessage[messageEncodingStatus.currentMessageIndex].toInt() shr toShift[(shiftIndex++) % toShift.size]) and 0x3)).toByte()

                        if (shiftIndex % toShift.size == 0) {
                            messageEncodingStatus.incrementMessageIndex()
                            progressHandler?.increment(1)
                        }

                        if (messageEncodingStatus.currentMessageIndex == messageEncodingStatus.byteArrayMessage.size) {
                            messageEncodingStatus.setMessageEncoded()
                            progressHandler?.finished()
                        }
                    } else {
                        //Simply copy the integer to result array
                        tmp = ((integerPixelArray[element] shr binary[channelIndex]) and 0xFF).toByte()
                    }

                    result[resultIndex++] = tmp
                }
            }
        }

        return result
    }

    /**
     * This method implements the above method on the list of chunk image list.
     *
     * @return : Encoded list of chunk images
     * @parameter : splittedImages {list of chunk images}
     * @parameter : encryptedMessage {string}
     * @parameter : progressHandler {Progress bar handler}
     */
    fun encodeMessage(
        splittedImages: List<Bitmap>,
        encryptedMessage: String, progressHandler: ProgressHandler?
    ): List<Bitmap> {
        //Making result method
        var encryptedMessage = encryptedMessage
        val result: MutableList<Bitmap> = ArrayList(splittedImages.size)

        //Adding start and end message constants to the encrypted message
        encryptedMessage = START_MESSAGE_CONSTANT + encryptedMessage + END_MESSAGE_CONSTANT

        //getting byte array from string
        val byteEncryptedMessage = encryptedMessage.toByteArray(Charset.forName("ISO-8859-1"))

        //Message Encoding Status
        val message = MessageEncodingStatus(byteEncryptedMessage, encryptedMessage)

        //Progress Handler
        progressHandler?.setTotal(byteEncryptedMessage.size)

        //Just a log to get the byte message length
        Log.i(TAG, "Message length ${byteEncryptedMessage.size}")

        for (bitmap in splittedImages) {
            if (!message.isMessageEncoded) {
                //getting bitmap height and width
                val width = bitmap.width
                val height = bitmap.height

                //Making 1D integer pixel array
                val oneD = IntArray(width * height)
                bitmap.getPixels(oneD, 0, width, 0, 0, width, height)

                //getting bitmap density
                val density = bitmap.density

                //encoding image
                val encodedImage = encodeMessage(oneD, width, height, message, progressHandler)

                //converting byte_image_array to integer_array
                val oneDMod = Utility.byteArrayToIntArray(encodedImage)

                //creating bitmap from encrypted_image_array
                val encodedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                encodedBitmap.density = density

                var masterIndex = 0

                //setting pixel values of above bitmap
                for (j in 0 until height) {
                    for (i in 0 until width) {
                        encodedBitmap.setPixel(
                            i, j, Color.argb(
                                0xFF,
                                oneDMod[masterIndex] shr 16 and 0xFF,
                                oneDMod[masterIndex] shr 8 and 0xFF,
                                oneDMod[masterIndex++] and 0xFF
                            )
                        )
                    }
                }

                result.add(encodedBitmap)
            } else {
                //Just add the image chunk to the result
                result.add(bitmap.copy(bitmap.config, false))
            }
        }

        return result
    }

    /**
     * This is the decoding method of 2-bit encoding.
     *
     * @return : Void
     * @parameter : bytePixelArray {The byte array image}
     * @parameter : imageColumns {Image width}
     * @parameter : imageRows {Image height}
     * @parameter : messageDecodingStatus {object}
     */
    private fun decodeMessage(
        bytePixelArray: ByteArray, imageColumns: Int,
        imageRows: Int, messageDecodingStatus: MessageDecodingStatus
    ) {
        //encrypted message
        val byteEncryptedMessage = Vector<Byte>()
        var shiftIndex = 4
        var tmp: Byte = 0x00

        for (aBytePixelArray in bytePixelArray) {
            //get last two bits from byte_pixel_array
            tmp = (tmp.toInt() or ((aBytePixelArray.toInt() shl toShift[shiftIndex % toShift.size]) and andByte[shiftIndex++ % toShift.size].toInt())).toByte()

            if (shiftIndex % toShift.size == 0) {
                //adding temp byte value
                byteEncryptedMessage.addElement(tmp)

                //converting byte value to string
                val nonso = byteArrayOf(byteEncryptedMessage.lastElement())
                val str = String(nonso, Charset.forName("ISO-8859-1"))

                if (messageDecodingStatus.message.endsWith(END_MESSAGE_CONSTANT)) {
                    Log.i("TEST", "Decoding ended")

                    //fixing ISO-8859-1 decoding
                    val temp = ByteArray(byteEncryptedMessage.size)

                    for (index in temp.indices) temp[index] = byteEncryptedMessage[index]

                    val stra = String(temp, Charset.forName("ISO-8859-1"))

                    messageDecodingStatus.message = stra.substring(0, stra.length - 1)

                    //end fixing
                    messageDecodingStatus.setEnded()
                    break
                } else {
                    //just add the decoded message to the original message
                    messageDecodingStatus.message += str

                    //If there was no message there and only start and end message constant was there
                    if (messageDecodingStatus.message.length == START_MESSAGE_CONSTANT.length && START_MESSAGE_CONSTANT != messageDecodingStatus.message) {
                        messageDecodingStatus.message = ""
                        messageDecodingStatus.setEnded()
                        break
                    }
                }

                tmp = 0x00
            }
        }

        if (!Utility.isStringEmpty(messageDecodingStatus.message)) {
            //removing start and end constants from message
            try {
                messageDecodingStatus.message = messageDecodingStatus.message.substring(
                    START_MESSAGE_CONSTANT.length, messageDecodingStatus.message.length - END_MESSAGE_CONSTANT.length
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * This method takes the list of encoded chunk images and decodes it.
     *
     * @return : encrypted message {String}
     * @parameter : encodedImages {list of encode chunk images}
     */
    fun decodeMessage(encodedImages: List<Bitmap>): String {
        //Creating object
        val messageDecodingStatus = MessageDecodingStatus()

        for (bit in encodedImages) {
            val pixels = IntArray(bit.width * bit.height)
            bit.getPixels(pixels, 0, bit.width, 0, 0, bit.width, bit.height)
            val b = Utility.convertArray(pixels)
            decodeMessage(b, bit.width, bit.height, messageDecodingStatus)

            if (messageDecodingStatus.isEnded) break
        }

        return messageDecodingStatus.message
    }

    /**
     * Calculate the numbers of pixel needed
     *
     * @return : The number of pixels {integer}
     * @parameter : message {Message to encode}
     */
    fun numberOfPixelForMessage(message: String?): Int {
        var result = -1
        if (message != null) {
            var messageWithConstants = START_MESSAGE_CONSTANT + message + END_MESSAGE_CONSTANT
            result = messageWithConstants.toByteArray(Charset.forName("ISO-8859-1")).size * 4 / 3
        }
        return result
    }

    //Progress handler class
    interface ProgressHandler {
        fun setTotal(tot: Int)
        fun increment(inc: Int)
        fun finished()
    }

    private class MessageDecodingStatus {
        var message: String = ""
        var isEnded: Boolean = false
            private set

        fun setEnded() {
            this.isEnded = true
        }
    }

    private class MessageEncodingStatus(val byteArrayMessage: ByteArray, val message: String) {
        var isMessageEncoded: Boolean = false
            private set
        var currentMessageIndex: Int = 0

        fun incrementMessageIndex() {
            currentMessageIndex++
        }

        fun setMessageEncoded() {
            this.isMessageEncoded = true
        }
    }
}
