package com.focusbubble.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.R
import com.focusbubble.data.repository.SessionRepository
import com.focusbubble.ui.utils.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun FocusSessionScreen(
    durationMinutes: Int,
    onStop: () -> Unit,
    onFinish: () -> Unit,
    onFocusSessionsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var timeLeft by remember { mutableStateOf(durationMinutes * 60) }
    var isPaused by remember { mutableStateOf(false) }
    var sessionId by remember { mutableStateOf<Int?>(null) }

    // Create session on backend when screen opens
    LaunchedEffect(Unit) {
        val userId = UserSession.getUserId(context)
        if (userId != -1) {
            try {
                val sessionRepo = SessionRepository()
                val response = sessionRepo.startSession(userId, null, durationMinutes)

                if (response.isSuccessful) {
                    sessionId = response.body()?.id
                    Log.d("FocusSession", "✅ Session created on backend: ID=$sessionId")
                } else {
                    Log.e("FocusSession", "❌ Failed to create session: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FocusSession", "❌ Error creating session", e)
            }
        }
    }

    // Countdown logic
    LaunchedEffect(timeLeft, isPaused) {
        if (!isPaused && timeLeft > 0) {
            delay(1000L)
            timeLeft--
            if (timeLeft == 0) {
                // Stop session on backend
                sessionId?.let { id ->
                    scope.launch {
                        try {
                            val sessionRepo = SessionRepository()
                            sessionRepo.stopSession(id)
                            Log.d("FocusSession", "✅ Session stopped on backend")
                        } catch (e: Exception) {
                            Log.e("FocusSession", "❌ Error stopping session", e)
                        }
                    }
                }
                onFinish()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.dashboard_bg),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            // Circle timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .drawBehind {
                        val sweep = 360f * (timeLeft.toFloat() / (durationMinutes * 60))
                        drawArc(
                            color = Color.White,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
            ) {
                Text(
                    text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mascot
            Image(
                painter = painterResource(id = R.drawable.bubbly_icon),
                contentDescription = "Mascot",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        // Stop session on backend
                        sessionId?.let { id ->
                            scope.launch {
                                try {
                                    val sessionRepo = SessionRepository()
                                    sessionRepo.stopSession(id)
                                    Log.d("FocusSession", "✅ Session stopped")
                                } catch (e: Exception) {
                                    Log.e("FocusSession", "❌ Error stopping session", e)
                                }
                            }
                        }
                        onStop()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Stop focusing")
                }

                Button(
                    onClick = {
                        isPaused = !isPaused
                        // Pause/Resume on backend
                        sessionId?.let { id ->
                            scope.launch {
                                try {
                                    val sessionRepo = SessionRepository()
                                    if (isPaused) {
                                        sessionRepo.pauseSession(id)
                                        Log.d("FocusSession", "⏸️ Session paused")
                                    } else {
                                        sessionRepo.resumeSession(id)
                                        Log.d("FocusSession", "▶️ Session resumed")
                                    }
                                } catch (e: Exception) {
                                    Log.e("FocusSession", "❌ Error pausing/resuming", e)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(if (isPaused) "Resume" else "Pause")
                }
            }
        }
    }
}