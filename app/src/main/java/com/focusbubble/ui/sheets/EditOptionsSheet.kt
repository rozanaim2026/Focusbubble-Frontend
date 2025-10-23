package com.focusbubble.ui.sheets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.ui.viewmodel.BlockedAppsViewModel

// Enum for subsheets
private enum class Subsheet { Duration, BlockApps, Quotes }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOptionsSheet(
    viewModel: BlockedAppsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // --- States ---
    var activeSubsheet by remember { mutableStateOf<Subsheet?>(null) }
    val currentDuration by viewModel.selectedDurationMinutes.collectAsState()
    var selectedQuotes by remember { mutableStateOf<Set<String>>(emptySet()) }

    val durationDisplay = remember(currentDuration) {
        if (currentDuration >= 60) {
            val h = currentDuration / 60
            val m = currentDuration % 60
            if (m == 0) "${h} hr" else "${h} hr ${m} min"
        } else {
            "$currentDuration min"
        }
    }

    // --- Blocked apps icons from blockedAppsUi (already has icons loaded) ---
    val blockedApps by viewModel.blockedApps.collectAsState()
    val blockedAppsUi by viewModel.blockedAppsUi.collectAsState()
    
    // Use icons from blockedAppsUi which already has iconBitmap loaded
    val blockedIcons = remember(blockedAppsUi) {
        blockedAppsUi.mapNotNull { app ->
            app.iconBitmap?.let { bitmap ->
                BitmapPainter(bitmap)
            }
        }
    }

    // --- MAIN SHEET ---
    if (activeSubsheet == null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF1C1C1C),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Edit Session Options", fontSize = 20.sp, color = Color.White)
                Spacer(Modifier.height(12.dp))

                // Duration
                EditRow(
                    label = "Duration",
                    subtitle = durationDisplay,
                    icon = Icons.Filled.Timer
                ) { activeSubsheet = Subsheet.Duration }

                Spacer(Modifier.height(12.dp))

                // Blocked Apps
                EditRowWithIcons(
                    label = if (blockedApps.isEmpty()) "Block Apps" else "Blocked Apps",
                    count = blockedApps.size,
                    icons = blockedIcons.take(3), // Show max 3 icons
                    icon = Icons.Filled.Block
                ) { activeSubsheet = Subsheet.BlockApps }

                Spacer(Modifier.height(12.dp))

                // Quotes
                EditRow(
                    label = "Quotes",
                    subtitle = if (selectedQuotes.isEmpty()) "Select Quotes" else selectedQuotes.joinToString(),
                    icon = Icons.Filled.FormatListBulleted
                ) { activeSubsheet = Subsheet.Quotes }

                Spacer(Modifier.height(20.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }

    // --- SUBSHEET HANDLING ---
    when (activeSubsheet) {
        Subsheet.Duration -> DurationPickerBottomSheet(
            initialHours = currentDuration / 60,
            initialMinutes = currentDuration % 60,
            onDismiss = { activeSubsheet = null },
            onConfirm = { h, m ->
                viewModel.setSelectedDuration(h * 60 + m)
                activeSubsheet = null
            }
        )
        Subsheet.BlockApps -> BlockAppsSheet(
            viewModel = viewModel,
            onDismiss = { activeSubsheet = null }
        )
        Subsheet.Quotes -> QuotesMultiSelectSheet(
            selectedQuotes = selectedQuotes, // Pass directly, no copy
            onDismiss = { activeSubsheet = null },
            onConfirm = { selected ->
                selectedQuotes = selected
                activeSubsheet = null
            }
        )
        null -> Unit
    }
}

// --- Row composable ---
@Composable
fun EditRow(
    label: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(12.dp))
        Text(label, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(subtitle, color = Color.LightGray, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun EditRowWithIcons(
    label: String,
    count: Int = 0,
    icons: List<BitmapPainter>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(12.dp))
        Text(label, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        
        // Show icons if available
        if (icons.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icons.forEach { painter ->
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
                // Show count if there are selected apps
                if (count > 0) {
                    Text(
                        text = "($count)",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
        
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White)
    }
}

// --- Drawable to Bitmap extension ---
fun Drawable.toBitmap(): Bitmap {
    val width = intrinsicWidth.takeIf { it > 0 } ?: 1
    val height = intrinsicHeight.takeIf { it > 0 } ?: 1
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}