package com.focusbubble.ui.overlay

import android.content.Context
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
import androidx.compose.ui.platform.ComposeView
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
import com.focusbubble.ui.theme.FocusBubbleTheme

/**
 * Factory to create Compose-based overlay view that matches FocusSessionScreen design
 * Shows blocked app with circular timer
 * Includes lifecycle support for Service context
 */
object BlockOverlayViewFactory {
    
    fun create(
        context: Context,
        appName: String,
        remainingTimeSeconds: Int,
        totalDurationSeconds: Int,
        onContinue: () -> Unit,
        onEmergency: () -> Unit
    ): ComposeView {
        // Create lifecycle owner for Service context
        val lifecycleOwner = object : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
            private val lifecycleRegistry = LifecycleRegistry(this)
            private val store = ViewModelStore()
            private val savedStateRegistryController = SavedStateRegistryController.create(this)
            
            override val lifecycle: Lifecycle
                get() = lifecycleRegistry
                
            override val viewModelStore: ViewModelStore
                get() = store
                
            override val savedStateRegistry: SavedStateRegistry
                get() = savedStateRegistryController.savedStateRegistry
            
            init {
                savedStateRegistryController.performRestore(null)
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            }
            
            fun destroy() {
                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                store.clear()
            }
        }
        
        return ComposeView(context).apply {
            // Set up view tree owners for Compose
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            
            // Use DisposeOnDetachedFromWindow strategy
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            
            setContent {
                FocusBubbleTheme {
                    BlockOverlayContent(
                        appName = appName,
                        remainingTimeSeconds = remainingTimeSeconds,
                        totalDurationSeconds = totalDurationSeconds,
                        onContinueFocus = onContinue,
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
    onContinueFocus: () -> Unit,
    onEmergencyUse: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)) // Dark background
    ) {
        // Background pattern/gradient (optional)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2D2D2D),
                            Color(0xFF1A1A1A)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // App blocked message
            Text(
                text = "$appName is blocked",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFFCCCC)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Focus Mode Active
            Text(
                text = "Focus Mode Active!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Circular timer (matching FocusSessionScreen)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .drawBehind {
                        // Background circle
                        drawCircle(
                            color = Color(0xFF333333),
                            style = Stroke(width = 8.dp.toPx())
                        )
                        
                        // Progress arc
                        val progress = if (totalDurationSeconds > 0) {
                            remainingTimeSeconds.toFloat() / totalDurationSeconds.toFloat()
                        } else {
                            1f
                        }
                        val sweep = 360f * progress
                        
                        drawArc(
                            color = Color(0xFF4CAF50), // Green progress
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
            ) {
                // Time display
                Text(
                    text = String.format(
                        "%02d:%02d",
                        remainingTimeSeconds / 60,
                        remainingTimeSeconds % 60
                    ),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // "remaining" text
            Text(
                text = "remaining",
                fontSize = 16.sp,
                color = Color(0xFFAAAAAA)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Mascot
            Image(
                painter = painterResource(id = R.drawable.bubbly_icon),
                contentDescription = "Focus Mascot",
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Action buttons (matching FocusSessionScreen layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Continue with Focus button
                Button(
                    onClick = onContinueFocus,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Continue Focus",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Emergency Use button
                Button(
                    onClick = onEmergencyUse,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722), // Red/Orange
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Emergency",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Helpful message
            Text(
                text = "Stay focused on your goals! 🎯",
                fontSize = 14.sp,
                color = Color(0xFF888888)
            )
        }
    }
}
