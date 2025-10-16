package com.focusbubble

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusbubble.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class TestConnectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestConnectionScreen()
        }
    }
}

@Composable
fun TestConnectionScreen() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("Tap button to test connection") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Backend Connection Test",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Show current URL
        Text(
            text = "Testing URL:",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "http://192.168.31.161:8000/health",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    result = testConnection()
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Testing..." else "Test Connection")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = result,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

suspend fun testConnection(): String = withContext(Dispatchers.IO) {
    try {
        Log.d("ConnectionTest", "🔵 Starting connection test...")
        
        // Test 1: Direct HTTP connection
        val url = URL("http://192.168.31.161:8000/health")
        Log.d("ConnectionTest", "🌐 URL: $url")
        
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.requestMethod = "GET"
        
        Log.d("ConnectionTest", "📡 Connecting...")
        connection.connect()
        
        val responseCode = connection.responseCode
        Log.d("ConnectionTest", "📊 Response code: $responseCode")
        
        if (responseCode == 200) {
            val response = connection.inputStream.bufferedReader().readText()
            Log.d("ConnectionTest", "✅ Response: $response")
            "✅ SUCCESS!\n\nResponse code: $responseCode\n\nBody: $response"
        } else {
            "❌ FAILED\n\nResponse code: $responseCode"
        }
    } catch (e: java.net.ConnectException) {
        Log.e("ConnectionTest", "❌ Connection refused", e)
        "❌ CONNECTION REFUSED\n\nYour phone cannot reach the backend.\n\nChecklist:\n• Same WiFi network?\n• Mac IP: 192.168.31.161\n• Backend running on Mac?"
    } catch (e: java.net.SocketTimeoutException) {
        Log.e("ConnectionTest", "❌ Timeout", e)
        "❌ TIMEOUT\n\nConnection timed out.\n\nPossible causes:\n• Wrong IP address\n• Firewall blocking\n• WiFi isolation enabled"
    } catch (e: Exception) {
        Log.e("ConnectionTest", "❌ Error: ${e.javaClass.simpleName}", e)
        "❌ ERROR\n\n${e.javaClass.simpleName}:\n${e.message}"
    }
}
