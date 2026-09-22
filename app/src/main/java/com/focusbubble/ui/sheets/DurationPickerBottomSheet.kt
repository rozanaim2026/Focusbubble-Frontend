package com.focusbubble.ui.sheets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private val AccentBlue = Color(0xFF3D8DFF)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DurationPickerBottomSheet(
    initialHours: Int,
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val hourRange = (0..11).toList()
    val minuteRange = (0..59).toList()
    val visibleCount = 5

    val hoursState = rememberLazyListState(initialHours)
    val minutesState = rememberLazyListState(initialMinutes)

    var selectedHour by remember { mutableStateOf(initialHours) }
    var selectedMinute by remember { mutableStateOf(initialMinutes) }

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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "How long do you want to focus?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                PickerLazyColumn(
                    items = hourRange,
                    state = hoursState,
                    visibleCount = visibleCount,
                    label = { "$it" },
                    onSelectedChanged = { selectedHour = it }
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "hours",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(Modifier.width(24.dp))

                PickerLazyColumn(
                    items = minuteRange,
                    state = minutesState,
                    visibleCount = visibleCount,
                    label = { "$it" },
                    onSelectedChanged = { selectedMinute = it }
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "mins",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { onConfirm(selectedHour, selectedMinute) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(vertical = 0.dp)
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PickerLazyColumn(
    items: List<Int>,
    state: LazyListState,
    visibleCount: Int,
    label: (Int) -> String,
    onSelectedChanged: (Int) -> Unit
) {
    val itemHeightDp = 50
    val padding = (visibleCount / 2) * itemHeightDp

    // Precisely find whichever item's center is closest to the viewport's center —
    // recomputed continuously during scroll, not just on settle. Using plain
    // firstVisibleItemIndex made the highlighted value change a beat before the
    // item was actually centered, which read as slightly jumpy/imprecise.
    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }?.index ?: 0
        }
    }

    LaunchedEffect(centeredIndex) {
        onSelectedChanged(centeredIndex.coerceIn(items.indices))
    }

    LazyColumn(
        state = state,
        modifier = Modifier
            .height((visibleCount * itemHeightDp).dp)
            .width(80.dp)
            .background(Color.Transparent),
        contentPadding = PaddingValues(vertical = padding.dp),
        flingBehavior = rememberSnapFlingBehavior(state)
    ) {
        itemsIndexed(items) { idx, item ->
            val isSelected = idx == centeredIndex

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeightDp.dp)
                    .background(
                        if (isSelected) AccentBlue else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label(item),
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                    fontSize = if (isSelected) 20.sp else 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
