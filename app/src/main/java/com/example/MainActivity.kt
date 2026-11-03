package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.StoreWebViewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NetworkMonitor

class MainActivity : ComponentActivity() {
  private lateinit var networkMonitor: NetworkMonitor

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    networkMonitor = NetworkMonitor(applicationContext)

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        StoreWebViewScreen(
          networkMonitor = networkMonitor,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
