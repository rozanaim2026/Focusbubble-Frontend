package com.focusbubble.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.R
import com.focusbubble.ui.utils.MotivationalQuotes
import kotlinx.coroutines.delay

/**
 * Tappable mascot. Each tap shows a different motivational quote in a small
 * bubble above it, which fades in, holds, then fades out on its own.
 */
@Composable
fun QuoteMascot(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 120.dp
) {
    var currentQuote by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Auto-hide the quote after it's had time to be read
    LaunchedEffect(currentQuote) {
        if (currentQuote != null) {
            delay(3200L)
            currentQuote = null
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = currentQuote != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .widthIn(max = 260.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = currentQuote ?: "",
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.bubbly_icon),
            contentDescription = "Mascot — tap for a motivational quote",
            modifier = Modifier
                .size(size)
                .clickable {
                    val categories = com.focusbubble.ui.utils.QuotePreferences.getSelectedCategories(context)
                    currentQuote = MotivationalQuotes.random(categories, previous = currentQuote)
                }
        )
    }
}
