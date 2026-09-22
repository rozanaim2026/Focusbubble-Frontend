package com.focusbubble.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.ui.viewmodel.BlockedAppsViewModel
import com.focusbubble.ui.viewmodel.FocusStatsViewModel

/**
 * Card-free dashboard content — timer, blocked-apps preview, and edit button
 * float directly on the background image, matching the reference app's look.
 * No profile circle here anymore — profile lives in the account screen (hamburger menu).
 */
@Composable
fun ProfileFocusCard(
    userName: String?,
    focusStatsViewModel: FocusStatsViewModel,
    blockedAppsViewModel: BlockedAppsViewModel,
    onEditClick: () -> Unit
) {
    val weeklyTime by focusStatsViewModel.weeklyFocusTime.collectAsState()
    val blockedAppsUi by blockedAppsViewModel.blockedAppsUi.collectAsState()

    // Soft drop shadow so white text stays legible over bright parts of the background
    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.55f),
        blurRadius = 18f
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---------- Weekly Focus Time ----------
        Text(
            text = String.format("%02d:%02d", weeklyTime / 60, weeklyTime % 60),
            color = Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayLarge.copy(shadow = textShadow)
        )
        Text(
            text = "TIME FOCUSED",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.6.sp,
            style = MaterialTheme.typography.labelMedium.copy(shadow = textShadow)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ---------- Blocked Apps Preview ----------
        if (blockedAppsUi.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                com.focusbubble.ui.components.StackedAppIcons(
                    icons = blockedAppsUi.take(3).mapNotNull { it.iconBitmap }
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${blockedAppsUi.size} apps blocked",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow)
                )
            }
        } else {
            Text(
                text = "No apps blocked",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ---------- Edit — ghost pill, no solid fill, so it stays part of the background ----------
        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier.height(36.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}
