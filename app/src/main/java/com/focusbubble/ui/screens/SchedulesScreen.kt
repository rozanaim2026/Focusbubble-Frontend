package com.focusbubble.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focusbubble.data.model.Schedule
import com.focusbubble.ui.utils.UserSession
import com.focusbubble.ui.viewmodel.ScheduleViewModel

@Composable
fun SchedulesScreen(
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val schedules by viewModel.schedules.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val successMessage by viewModel.successMessage.observeAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    val userId = UserSession.getUserId(context)

    // Load schedules when screen opens
    LaunchedEffect(userId) {
        if (userId > 0) {
            viewModel.loadSchedules(userId)
        }
    }

    // Show error toast
    LaunchedEffect(error) {
        error?.let { errorMsg: String ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // Show success toast
    LaunchedEffect(successMessage) {
        successMessage?.let { msg: String ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }

    ScreenWithBack(
        title = "Schedules",
        onBackClick = onBackClick
    ) { modifier ->
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Empty state or list
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (schedules.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "No schedules yet",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap + to create your first schedule",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(schedules) { schedule: Schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                onDelete = {
                                    viewModel.deleteSchedule(userId, schedule.id)
                                }
                            )
                        }
                    }
                }
            }

            // Floating Action Button
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Schedule")
            }
        }
    }

    // Create Schedule Dialog
    if (showCreateDialog) {
        CreateScheduleDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { label, duration, apps ->
                viewModel.createSchedule(userId, label, duration, apps)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: Schedule,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.label,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${schedule.durationMinutes} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (schedule.apps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${schedule.apps.size} apps blocked",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (schedule.isActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CreateScheduleDialog(
    onDismiss: () -> Unit,
    onCreate: (label: String, durationMinutes: Int, apps: List<String>) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("25") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Schedule") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Schedule Name") },
                    placeholder = { Text("e.g., Morning Focus") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") },
                    placeholder = { Text("25") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Note: You can add blocked apps later from the Blocked Apps screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (label.isNotBlank() && duration.toIntOrNull() != null) {
                        onCreate(label, duration.toInt(), emptyList())
                    }
                },
                enabled = label.isNotBlank() && duration.toIntOrNull() != null
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
