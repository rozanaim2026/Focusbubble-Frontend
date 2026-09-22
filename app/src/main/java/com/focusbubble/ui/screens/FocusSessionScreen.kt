package com.focusbubble.ui.screens

import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.core.content.ContextCompat
import com.focusbubble.R
import com.focusbubble.data.repository.SessionRepository
import com.focusbubble.service.BlockerService
import com.focusbubble.service.SessionStateManager
import com.focusbubble.ui.components.QuoteMascot
import com.focusbubble.ui.sheets.PauseOptionsSheet
import com.focusbubble.ui.utils.UserSession
import com.focusbubble.ui.viewmodel.FocusStatsViewModel
import kotlinx.coroutines.launch
import android.util.Log
import androidx.activity.compose.BackHandler
import kotlin.math.roundToInt

@Composable
fun FocusSessionScreen(
    durationMinutes: Int,
    focusStatsViewModel: FocusStatsViewModel,
    onStop: () -> Unit,
    // Called instead of onStop() specifically for a NATURAL finish (timer ran out
    // on its own) — this is what actually gets the congratulations screen on
    // screen reliably. Previously, both this screen's own poll loop AND a
    // separate top-level broadcast receiver in MainActivity/AppNavHost reacted
    // independently to the same "session finished" signal: this screen's poll
    // loop unconditionally called onStop() (popping straight back to Dashboard)
    // the instant it noticed the session was no longer active, while the other
    // receiver was separately trying to navigate to the celebration screen. That
    // was a race — and the poll loop, checking every 500ms with no gating logic,
    // almost always won, silently dropping the user on Dashboard before the
    // celebration navigation had a chance to land. Now, whichever piece of code
    // notices the finish FIRST (this screen, if it's the one currently mounted)
    // owns the transition directly and atomically, instead of hoping a separate
    // receiver wins a race against it.
    onSessionComplete: (Int) -> Unit,
    onFocusSessionsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val totalSeconds = durationMinutes * 60
    // Seeded from SessionStateManager so the very first frame already matches
    // whatever BlockerService (the real timer) has recorded.
    var timeLeft by remember {
        mutableStateOf(
            (SessionStateManager.getRemainingTime(context) / 1000L).toInt()
                .takeIf { it > 0 } ?: totalSeconds
        )
    }
    var isPaused by remember { mutableStateOf(SessionStateManager.isPaused(context)) }
    var showPauseSheet by remember { mutableStateOf(false) }
    var sessionId by remember { mutableStateOf<Int?>(null) }

    var hasHandledFinish by remember { mutableStateOf(false) }

    /**
     * Single shared handler for "the session is no longer active and this screen
     * hasn't dealt with that yet" — called from both the poll loop and the
     * broadcast receiver below so the natural-finish-vs-manual-stop decision
     * lives in exactly one place instead of being duplicated (and potentially
     * drifting out of sync) in two.
     *
     * doEndSession() (manual Stop) sets hasHandledFinish = true BEFORE this could
     * ever run, so this function only ever actually does something for a genuine
     * natural finish — hasPendingCelebration() will reliably be true in that case
     * (BlockerService.handleSessionFinished() sets it before broadcasting), and we
     * navigate straight to the celebration screen instead of just popping back.
     */
    fun handleSessionEndedIfNeeded() {
        if (hasHandledFinish) return
        hasHandledFinish = true

        // No local Room insert or weekly-time crediting here — this function
        // only ever runs for a NATURAL finish (a manual Stop is handled
        // entirely separately, in doEndSession() below, which sets
        // hasHandledFinish = true before this could ever run for that case).
        // BlockerService.handleSessionFinished() already inserts the correct
        // COMPLETED row and credits weekly time the instant the session
        // actually finishes — this used to ALSO insert a second, incorrectly
        // STOPPED-labeled row for the exact same session right here, which is
        // exactly why every natural finish was showing up twice in
        // focus_sessions (once correctly as COMPLETED, once wrongly as
        // STOPPED), and part of why the celebration screen was unreliable —
        // see the matching fix in MainActivity.kt's SessionFinishedReceiver
        // for the other half of that.

        sessionId?.let { id ->
            scope.launch {
                try {
                    SessionRepository().stopSession(id)
                } catch (e: Exception) {
                    Log.e("FocusSession", "❌ Error stopping session on backend", e)
                }
            }
        }

        if (SessionStateManager.hasPendingCelebration(context)) {
            val seconds = SessionStateManager.getPendingCelebrationSeconds(context)
            SessionStateManager.clearPendingCelebration(context)
            Log.d("FocusSession", "🎉 Natural finish detected — showing celebration ($seconds s)")
            onSessionComplete(seconds)
        } else {
            // No pending celebration recorded — shouldn't normally happen for a
            // natural finish, but fail safe rather than getting stuck.
            onStop()
        }
    }

    // ✅ Primary sync mechanism: poll the persisted session state directly instead of
    // relying solely on broadcasts. Broadcasts were proving unreliable (missed/delayed
    // implicit broadcasts), leaving the UI frozen even though BlockerService's own
    // timer was ticking correctly underneath. Polling the same SharedPreferences-backed
    // state that BlockerService writes to every second guarantees this screen can never
    // drift from reality — same trick used for the floating widget and blocked screen.
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)

            val active = SessionStateManager.isSessionActive(context)
            if (!active) {
                handleSessionEndedIfNeeded()
                break
            }

            val paused = SessionStateManager.isPaused(context)
            isPaused = paused
            val remainingMs = SessionStateManager.getRemainingTime(context)
            timeLeft = (remainingMs / 1000L).toInt()
        }
    }

    // Whenever the session becomes paused (from ANY source), surface the options sheet.
    LaunchedEffect(isPaused) {
        showPauseSheet = isPaused
    }

    // Kept as a secondary/faster path — when broadcasts do arrive, the UI updates
    // instantly instead of waiting for the next poll tick.
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    SessionStateManager.ACTION_UPDATE_TIMER -> {
                        val remainingMs = intent.getLongExtra(SessionStateManager.EXTRA_REMAINING_TIME, 0L)
                        timeLeft = (remainingMs / 1000L).toInt()
                    }
                    SessionStateManager.ACTION_SESSION_PAUSED -> {
                        isPaused = true
                        Log.d("FocusSession", "⏸️ Received PAUSE broadcast - UI updated")
                    }
                    SessionStateManager.ACTION_SESSION_RESUMED -> {
                        isPaused = false
                        Log.d("FocusSession", "▶️ Received RESUME broadcast - UI updated")
                    }
                    SessionStateManager.ACTION_SESSION_FINISHED -> {
                        Log.d("FocusSession", "✅ Received FINISH broadcast - wrapping up session")
                        handleSessionEndedIfNeeded()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(SessionStateManager.ACTION_SESSION_PAUSED)
            addAction(SessionStateManager.ACTION_SESSION_RESUMED)
            addAction(SessionStateManager.ACTION_UPDATE_TIMER)
            addAction(SessionStateManager.ACTION_SESSION_FINISHED)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Create session on backend and START BLOCKER SERVICE — but only for a genuine
    // fresh start. If a session is already active (e.g. the app process was killed
    // and relaunched while a session was running), we're restoring, not starting.
    LaunchedEffect(Unit) {
        val userId = UserSession.getUserId(context)
        val alreadyActive = SessionStateManager.isSessionActive(context)

        if (alreadyActive) {
            val restoredId = SessionStateManager.getSessionId(context)
            sessionId = restoredId.takeIf { it != -1 }
            Log.d("FocusSession", "🔄 Restored existing session (id=$sessionId) — not starting a new one")
            return@LaunchedEffect
        }

        Log.d("FocusSession", "🚀 Starting BlockerService with duration: $durationMinutes minutes")

        try {
            val serviceIntent = Intent(context, BlockerService::class.java).apply {
                putExtra("DURATION_MINUTES", durationMinutes)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
            Log.d("FocusSession", "✅ BlockerService started!")
        } catch (e: SecurityException) {
            Log.e("FocusSession", "❌ SecurityException starting service: ${e.message}", e)
            Toast.makeText(context, "Permission denied. Please grant notification permission.", Toast.LENGTH_LONG).show()
        } catch (e: IllegalStateException) {
            Log.e("FocusSession", "❌ IllegalStateException starting service: ${e.message}", e)
            Toast.makeText(context, "Cannot start service. Please restart the app.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("FocusSession", "❌ Failed to start service: ${e.message}", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        if (userId != -1) {
            try {
                val sessionRepo = SessionRepository()
                val response = sessionRepo.startSession(userId, null, durationMinutes)

                if (response.isSuccessful) {
                    sessionId = response.body()?.id
                    sessionId?.let { SessionStateManager.setSessionId(context, it) }
                    Log.d("FocusSession", "✅ Session created on backend: ID=$sessionId")
                } else {
                    Log.e("FocusSession", "❌ Failed to create session: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FocusSession", "❌ Error creating session", e)
            }
        }
    }

    fun doResume() {
        isPaused = false
        showPauseSheet = false
        SessionStateManager.resumeSession(context)
        sessionId?.let { id ->
            scope.launch {
                try {
                    SessionRepository().resumeSession(id)
                    Log.d("FocusSession", "▶️ Session resumed")
                } catch (e: Exception) {
                    Log.e("FocusSession", "❌ Error resuming", e)
                }
            }
        }
    }

    fun doPause() {
        isPaused = true
        SessionStateManager.pauseSession(context)
        sessionId?.let { id ->
            scope.launch {
                try {
                    SessionRepository().pauseSession(id)
                    Log.d("FocusSession", "⏸️ Session paused")
                } catch (e: Exception) {
                    Log.e("FocusSession", "❌ Error pausing", e)
                }
            }
        }
    }

    fun doEndSession() {
        Log.d("FocusSession", "🛑 User ended session")
        hasHandledFinish = true
        val serviceIntent = Intent(context, BlockerService::class.java)
        context.stopService(serviceIntent)

        // Deliberately NOT crediting any time here. A manual Stop means the user
        // did not complete the focus session they set — only a natural finish
        // (handled in BlockerService.handleSessionFinished(), which is the ONLY
        // place that calls WeeklyStatsManager.creditFocusTime now) should ever
        // count toward the weekly total. This used to call
        // focusStatsViewModel.addFocusTime(elapsedSeconds) here, crediting
        // partial time the moment Stop was pressed — that's exactly the "counts
        // even when I stop early" bug.

        sessionId?.let { id ->
            scope.launch {
                try {
                    SessionRepository().stopSession(id)
                    Log.d("FocusSession", "✅ Session stopped")
                } catch (e: Exception) {
                    Log.e("FocusSession", "❌ Error stopping session", e)
                }
            }
        }
        showPauseSheet = false
        onStop()
    }

    // While the session is active, back should exit to the mobile home screen —
    // not navigate to the dashboard, and not do nothing. The session (and its
    // floating timer) keeps running exactly as if the user pressed Home.
    BackHandler(enabled = true) {
        (context as? android.app.Activity)?.moveTaskToBack(true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

            // Circle timer — thinner ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .drawBehind {
                        val sweep = 360f * (timeLeft.toFloat() / totalSeconds.coerceAtLeast(1))
                        drawArc(
                            color = Color.White,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
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

            // Mascot — tap for a motivational quote
            QuoteMascot(size = 120.dp)

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { doEndSession() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
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
                    onClick = { if (isPaused) doResume() else doPause() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
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

        if (showPauseSheet) {
            PauseOptionsSheet(
                remainingTimeText = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                onResume = { doResume() },
                onEmergencyUse = {
                    showPauseSheet = false
                    Toast.makeText(
                        context,
                        "Emergency access enabled. Your session stays paused — reopen FocusBubble anytime to resume.",
                        Toast.LENGTH_LONG
                    ).show()
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(homeIntent)
                },
                onEndSession = { doEndSession() },
                onDismiss = { showPauseSheet = false }
            )
        }
    }
}