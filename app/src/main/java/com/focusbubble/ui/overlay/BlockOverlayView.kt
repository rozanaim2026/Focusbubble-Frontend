package com.focusbubble.ui.overlay

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.focusbubble.R
import com.focusbubble.service.SessionStateManager
import com.focusbubble.ui.theme.FocusBubbleTheme


object BlockOverlayViewFactory {

    fun create(
        context: Context,
        appName: String,
        remainingTimeSeconds: Int,
        totalDurationSeconds: Int,
        onLeave: () -> Unit,
        onEmergency: () -> Unit
    ): ComposeView {
        val lifecycleOwner =
            object :
                LifecycleOwner,
                ViewModelStoreOwner,
                SavedStateRegistryOwner {

                private val lifecycleRegistry =
                    LifecycleRegistry(this)

                private val store =
                    ViewModelStore()

                private val savedStateRegistryController =
                    SavedStateRegistryController.create(this)

                override val lifecycle: Lifecycle
                    get() = lifecycleRegistry

                override val viewModelStore: ViewModelStore
                    get() = store

                override val savedStateRegistry:
                        SavedStateRegistry
                    get() =
                        savedStateRegistryController
                            .savedStateRegistry

                init {
                    savedStateRegistryController
                        .performRestore(null)

                    lifecycleRegistry.currentState =
                        Lifecycle.State.RESUMED
                }
            }

        return ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setViewCompositionStrategy(
                ViewCompositionStrategy
                    .DisposeOnDetachedFromWindow
            )

            setContent {
                FocusBubbleTheme {
                    BlockOverlayContent(
                        appName = appName,
                        remainingTimeSeconds =
                            remainingTimeSeconds,
                        totalDurationSeconds =
                            totalDurationSeconds,
                        onLeave = onLeave,
                        onEmergencyUse = onEmergency
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockOverlayContent(
    appName: String,
    remainingTimeSeconds: Int,
    totalDurationSeconds: Int,
    onLeave: () -> Unit,
    onEmergencyUse: () -> Unit
) {
    val context =
        LocalContext.current

    var liveRemainingSeconds by remember {
        mutableStateOf(
            remainingTimeSeconds
                .coerceAtLeast(0)
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            liveRemainingSeconds =
                (
                        SessionStateManager
                            .getRemainingTime(context) /
                                1000L
                        )
                    .toInt()
                    .coerceAtLeast(0)

            kotlinx.coroutines.delay(1000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {
            Text(
                text = "$appName is blocked",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Focus mode active",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Box(
                contentAlignment =
                    Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .drawBehind {
                        drawCircle(
                            color =
                                Color.White.copy(
                                    alpha = 0.15f
                                ),
                            style =
                                Stroke(
                                    width = 2.dp.toPx()
                                )
                        )

                        val progress =
                            if (totalDurationSeconds > 0) {
                                liveRemainingSeconds.toFloat() /
                                        totalDurationSeconds
                                            .toFloat()
                            } else {
                                1f
                            }

                        drawArc(
                            color = Color.White,
                            startAngle = -90f,
                            sweepAngle =
                                360f *
                                        progress.coerceIn(
                                            0f,
                                            1f
                                        ),
                            useCenter = false,
                            style =
                                Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                        )
                    }
            ) {
                Text(
                    text = String.format(
                        "%02d:%02d",
                        liveRemainingSeconds / 60,
                        liveRemainingSeconds % 60
                    ),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "remaining",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Image(
                painter =
                    painterResource(
                        id = R.drawable.bubbly_icon
                    ),
                contentDescription = "Focus Mascot",
                modifier = Modifier.size(90.dp)
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Button(
                onClick = {
                    Log.d(
                        "OverlayClick",
                        "LEAVE BUTTON CLICKED"
                    )

                    onLeave()
                },
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(50.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                shape =
                    RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Leave to Home",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {
                    Log.d(
                        "OverlayClick",
                        "EMERGENCY BUTTON CLICKED"
                    )

                    onEmergencyUse()
                },
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(50.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                shape =
                    RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Emergency Use",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}