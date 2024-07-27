package com.bhanu09.imagesteganography

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }

    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navigateToEncode() }) {
                Text("Encode")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navigateToDecode() }) {
                Text("Decode")
            }
        }
    }

    private fun navigateToEncode() {
        startActivity(Intent(this, Encode::class.java))
    }

    private fun navigateToDecode() {
        startActivity(Intent(this, Decode::class.java))
    }
}