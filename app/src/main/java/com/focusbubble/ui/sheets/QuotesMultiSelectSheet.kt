// QuotesMultiSelectSheet.kt
package com.focusbubble.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesMultiSelectSheet(
    selectedQuotes: Set<String>,  // Immutable set here
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val allQuotes = listOf(
        "Motivational", "Focus", "Productivity", "Mindfulness",
        "Inspirational", "Breakup Motivational", "Mother Love"
    )

    val selectedState = remember(selectedQuotes) { mutableStateOf(selectedQuotes.toMutableSet()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Select Quote Categories", fontSize = 18.sp, color = Color.White)
            Spacer(Modifier.height(12.dp))

            // weight(1f) — list takes exactly the remaining space, Confirm sticks
            // to the bottom naturally, same pattern as Select Apps to Block.
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(allQuotes) { quote ->
                        val isSelected = selectedState.value.contains(quote)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quote,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    val updated = selectedState.value.toMutableSet()
                                    if (checked) updated.add(quote) else updated.remove(quote)
                                    selectedState.value = updated
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF3D8DFF),
                                    checkedTrackColor = Color(0xFF0D47A1)
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onConfirm(selectedState.value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Confirm", fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
