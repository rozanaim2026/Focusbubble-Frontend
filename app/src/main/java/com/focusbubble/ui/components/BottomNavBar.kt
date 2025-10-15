package com.focusbubble.ui.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = Color.Transparent
    ) {
        NavigationBarItem(
            selected = selectedTab == "Schedules",
            onClick = { onTabSelected("Schedules") },
            icon = {
                Icon(
                    Icons.Filled.AccessTime,
                    contentDescription = "Schedules",
                    tint = Color.White
                )
            },
            label = {
                Text(
                    "Schedules",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == "Chats",
            onClick = { onTabSelected("Chats") },
            icon = {
                Icon(
                    Icons.Filled.Chat,
                    contentDescription = "Chats",
                    tint = Color.White
                )
            },
            label = {
                Text(
                    "Chats",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == "Blocks",
            onClick = { onTabSelected("Blocks") },
            icon = {
                Icon(
                    Icons.Filled.Block,
                    contentDescription = "Blocks",
                    tint = Color.White
                )
            },
            label = {
                Text(
                    "Blocks",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == "Quotes",
            onClick = { onTabSelected("Quotes") },
            icon = {
                Icon(
                    Icons.Filled.Message,
                    contentDescription = "Quotes",
                    tint = Color.White
                )
            },
            label = {
                Text(
                    "Quotes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
    }
}
