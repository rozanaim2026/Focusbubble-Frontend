package com.focusbubble.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.ui.utils.QuotePreferences
import com.focusbubble.ui.sheets.QuotesMultiSelectSheet

@Composable
fun QuotesScreen(
    onBackClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showQuotesSheet by remember { mutableStateOf(false) }
    var selectedQuotes by remember { mutableStateOf(QuotePreferences.getSelectedCategories(context)) }
    ScreenWithBack(
        title = "Quotes",
        onBackClick = onBackClick
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Choose Your Quote Categories",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select categories to receive motivational quotes during your focus sessions",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { showQuotesSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (selectedQuotes.isEmpty()) {
                        "Select Quote Categories"
                    } else {
                        "Selected: ${selectedQuotes.size} categories"
                    },
                    fontSize = 16.sp
                )
            }

            if (selectedQuotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Selected Categories:",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                selectedQuotes.forEach { quote ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = quote,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showQuotesSheet) {
        QuotesMultiSelectSheet(
            selectedQuotes = selectedQuotes,
            onDismiss = { showQuotesSheet = false },
            onConfirm = { quotes ->
                selectedQuotes = quotes
                QuotePreferences.setSelectedCategories(context, quotes)
                showQuotesSheet = false
            }
        )
    }
}
