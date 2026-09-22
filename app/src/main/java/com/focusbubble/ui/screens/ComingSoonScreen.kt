package com.focusbubble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ComingSoonScreen(title: String, onBackClick: () -> Unit) {
    ScreenWithBack(title = title, onBackClick = onBackClick) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Coming soon", color = Color.White.copy(alpha = 0.6f))
        }
    }
}
