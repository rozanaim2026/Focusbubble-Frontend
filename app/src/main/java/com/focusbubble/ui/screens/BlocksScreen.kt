package com.focusbubble.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.focusbubble.ui.utils.getIconUri
import com.focusbubble.data.entities.BlockedApp
import com.focusbubble.ui.viewmodel.BlockedAppsViewModel
import com.focusbubble.ui.utils.UserSession

@Composable
fun BlocksScreen(
    onBackClick: () -> Unit,
    viewModel: BlockedAppsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val blockedApps by viewModel.blockedApps.collectAsState()

    // Sync from backend when screen opens
    LaunchedEffect(Unit) {
        val userId = UserSession.getUserId(context)
        if (userId != -1) {
            viewModel.syncBlocksFromBackend(userId)
        }
    }

    ScreenWithBack(title = "Blocked Apps", onBackClick = onBackClick) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            if (blockedApps.isEmpty()) {
                Text("No blocked apps", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(blockedApps) { app: BlockedApp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = app.getIconUri(context),
                                        contentDescription = app.appName,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(end = 8.dp)
                                    )

                                    Text("${app.appName} (${app.durationMinutes} mins)")
                                }

                                IconButton(onClick = { viewModel.deleteApp(app) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}