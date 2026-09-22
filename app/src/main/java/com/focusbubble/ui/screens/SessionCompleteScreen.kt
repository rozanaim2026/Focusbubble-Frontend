package com.focusbubble.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.R
import com.focusbubble.ui.utils.MotivationalQuotes
import com.focusbubble.ui.utils.QuotePreferences
import kotlin.math.sin
import kotlin.random.Random

// Muted, dark-theme-friendly palette instead of full rainbow confetti — reads
// as celebratory without clashing with the rest of the app's black/white look.
private val ConfettiColors = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFFFD166), // warm gold
    Color(0xFF6EE7B7), // mint
    Color(0xFF7DD3FC), // sky blue
    Color(0xFFFF8FA3)  // soft coral
)

private data class ConfettiParticle(
    val xFraction: Float,
    val startDelay: Float,
    val fallDuration: Float,
    val sizePx: Float,
    val color: Color,
    val rotationSpeed: Float,
    val driftAmplitude: Float,
    val driftFrequency: Float,
    val isCircle: Boolean
)

@Composable
fun SessionCompleteScreen(
    durationSeconds: Int,
    onBackToHome: () -> Unit
) {
    BackHandler(enabled = true) { onBackToHome() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val quote = remember {
        val categories = QuotePreferences.getSelectedCategories(context)
        MotivationalQuotes.random(categories)
    }

    val durationText = remember(durationSeconds) { formatDuration(durationSeconds) }

    // Drives both the confetti fall and the badge's entrance bounce off a single
    // timeline — one LaunchedEffect, no extra coroutines to manage.
    val progress = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0.4f) }

    var showEyebrow by remember { mutableStateOf(false) }
    var showHeadline by remember { mutableStateOf(false) }
    var showStat by remember { mutableStateOf(false) }
    var showQuote by remember { mutableStateOf(false) }
    var showMascot by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    val particles = remember {
        List(36) {
            val rnd = Random(it * 1337 + 7)
            ConfettiParticle(
                xFraction = rnd.nextFloat(),
                startDelay = rnd.nextFloat() * 0.25f,
                fallDuration = 0.55f + rnd.nextFloat() * 0.35f,
                sizePx = 6f + rnd.nextFloat() * 8f,
                color = ConfettiColors[rnd.nextInt(ConfettiColors.size)],
                rotationSpeed = 1.5f + rnd.nextFloat() * 3f,
                driftAmplitude = 12f + rnd.nextFloat() * 24f,
                driftFrequency = 1f + rnd.nextFloat() * 2f,
                isCircle = rnd.nextBoolean()
            )
        }
    }

    LaunchedEffect(Unit) {
        badgeScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(2800, easing = LinearEasing))
    }
    LaunchedEffect(Unit) {
        showEyebrow = true
        kotlinx.coroutines.delay(150)
        showHeadline = true
        kotlinx.coroutines.delay(200)
        showStat = true
        kotlinx.coroutines.delay(250)
        showQuote = true
        kotlinx.coroutines.delay(200)
        showMascot = true
        kotlinx.coroutines.delay(200)
        showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Confetti layer — drawn behind the content, driven purely by `progress`.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val p = progress.value
            particles.forEach { particle ->
                if (p < particle.startDelay) return@forEach
                val localT = ((p - particle.startDelay) / particle.fallDuration).coerceIn(0f, 1f)
                val alpha = when {
                    localT <= 0f -> 0f
                    localT >= 0.92f -> ((1f - localT) / 0.08f).coerceIn(0f, 1f)
                    else -> 1f
                }
                if (alpha <= 0f) return@forEach

                val y = localT * (size.height + particle.sizePx * 2) - particle.sizePx
                val drift = sin(localT * Math.PI.toFloat() * 2f * particle.driftFrequency) * particle.driftAmplitude
                val x = particle.xFraction * size.width + drift
                val rotationDeg = localT * 360f * particle.rotationSpeed

                rotate(degrees = rotationDeg, pivot = Offset(x, y)) {
                    if (particle.isCircle) {
                        drawCircle(
                            color = particle.color.copy(alpha = alpha),
                            radius = particle.sizePx / 2f,
                            center = Offset(x, y)
                        )
                    } else {
                        drawRect(
                            color = particle.color.copy(alpha = alpha),
                            topLeft = Offset(x - particle.sizePx / 2f, y - particle.sizePx / 4f),
                            size = androidx.compose.ui.geometry.Size(particle.sizePx, particle.sizePx / 2f)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glow badge with bounce-in checkmark
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .scale(badgeScale.value)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = showEyebrow,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }
            ) {
                Text(
                    text = "SESSION COMPLETE",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = showHeadline,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 }
            ) {
                Text(
                    text = "Congratulations! 👏🏻🎉",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visible = showStat,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
            ) {
                // A proper "hero stat" card instead of small gray subtext — this
                // is the number the person actually opened the screen to see.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(vertical = 22.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = durationText,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "of focused time — no distractions, no excuses",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (quote.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedVisibility(
                    visible = showQuote,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
                ) {
                    Text(
                        text = "\u201C$quote\u201D",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = showMascot,
                enter = fadeIn(tween(400))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bubbly_icon),
                    contentDescription = "Focus Mascot",
                    modifier = Modifier.size(96.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 2 }
            ) {
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text("Back to Home", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return when {
        minutes == 0 -> "$seconds second${if (seconds == 1) "" else "s"}"
        seconds == 0 -> "$minutes minute${if (minutes == 1) "" else "s"}"
        else -> "$minutes minute${if (minutes == 1) "" else "s"} $seconds second${if (seconds == 1) "" else "s"}"
    }
}