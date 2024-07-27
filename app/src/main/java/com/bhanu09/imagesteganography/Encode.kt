package com.bhanu09.imagesteganography

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.bhanu09.imagesteganography.Text.AsyncTaskCallback.TextEncodingCallback
import com.bhanu09.imagesteganography.Text.AsyncTaskCallback.TextEncodingCallback.Companion.encodeStatus
import com.bhanu09.imagesteganography.Text.ImageSteganography
import com.bhanu09.imagesteganography.Text.TextEncoding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Encode : ComponentActivity(), TextEncodingCallback {

    private var originalImage: Bitmap? by mutableStateOf(null)
    private var encodedImage: Bitmap? by mutableStateOf(null)
    private var filepath: Uri? by mutableStateOf(null)
    private var imageSteganography: ImageSteganography? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EncodeScreen()
        }
    }

    @Composable
    fun EncodeScreen() {
        var message by remember { mutableStateOf("") }
        var secretKey by remember { mutableStateOf("") }
        var encodeStatus by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            originalImage?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(200.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { chooseImage() }) {
                Text("Choose Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = secretKey,
                onValueChange = { secretKey = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                decorationBox = { innerTextField ->
                    if (secretKey.isEmpty()) {
                        Text("Enter secret key")
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (message.isEmpty()) {
                        Text("Enter message")
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (filepath != null && originalImage != null) {
                    imageSteganography = ImageSteganography(message, secretKey, originalImage!!)
                    val textEncoding = TextEncoding(this@Encode, this@Encode)
                    textEncoding.execute(imageSteganography)
                }
            }) {
                Text("Encode")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { saveEncodedImage() }) {
                Text("Save Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(encodeStatus)
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                filepath = uri
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    originalImage = bitmap
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveEncodedImage() {
        encodedImage?.let { bitmap ->
            val filename = "encoded_image.png"
            val file = File(getExternalFilesDir(null), filename)
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(this, "Image saved: $file", Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Toast.makeText(this, "No encoded image to save", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStartTextEncoding() {
        // Whatever you want to do at the start of text encoding
    }

    override fun onCompleteTextEncoding(result: ImageSteganography?) {
        result?.let {
            if (it.isEncoded) {
                encodedImage = it.encoded_image
                encodeStatus = "Message successfully encoded"
            } else {
                encodeStatus = "Encoding failed"
            }
        } ?: run {
            encodeStatus = "Select an image and enter a message first"
        }
    }
}