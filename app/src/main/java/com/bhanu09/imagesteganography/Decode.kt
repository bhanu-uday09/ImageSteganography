package com.bhanu09.imagesteganography

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.bhanu09.imagesteganography.Text.AsyncTaskCallback.TextDecodingCallback
import com.bhanu09.imagesteganography.Text.ImageSteganography
import com.bhanu09.imagesteganography.Text.TextDecoding
import java.io.IOException

class Decode : ComponentActivity(), TextDecodingCallback {

    private var originalImage: Bitmap? by mutableStateOf(null)
    private var filepath: Uri? by mutableStateOf(null)
    private var decodeStatus by mutableStateOf("")
    private var message by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DecodeScreen()
        }
    }

    @Composable
    fun DecodeScreen() {
        var secretKey by remember { mutableStateOf("") }

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

            Button(onClick = {
                filepath?.let {
                    val imageSteganography =
                        originalImage?.let { it1 -> ImageSteganography(secretKey, it1) }
                    val textDecoding = TextDecoding(this@Decode, this@Decode)
                    textDecoding.execute(imageSteganography)
                }
            }) {
                Text("Decode")
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                decorationBox = { innerTextField ->
                    if (message.isEmpty()) {
                        Text("Your message")
                    } else {
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(decodeStatus)
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

    override fun onStartTextEncoding() {
//        TODO("Not yet implemented")
    }

    override fun onCompleteTextEncoding(result: ImageSteganography?) {
        result?.let {
            if (!it.isDecoded) {
                decodeStatus = "No message found"
            } else {
                if (!it.isSecretKeyWrong) {
                    decodeStatus = "Decoded"
                    message = it.message ?: ""
                } else {
                    decodeStatus = "Wrong secret key"
                }
            }
        } ?: run {
            decodeStatus = "Select Image First"
        }
    }
}
