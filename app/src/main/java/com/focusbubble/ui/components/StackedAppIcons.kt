package com.focusbubble.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Overlapping/stacked app icon row — same style used on the Dashboard, reused
 * here (rather than reimplemented) so both screens stay visually consistent.
 */
@Composable
fun StackedAppIcons(
    icons: List<ImageBitmap>,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 28.dp,
    borderColor: Color = Color.Black
) {
    Row(modifier = modifier) {
        icons.forEachIndexed { index, bitmap ->
            Box(
                modifier = Modifier
                    .zIndex((icons.size - index).toFloat())
                    .offset(x = (-10 * index).dp)
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .border(1.5.dp, borderColor, CircleShape)
                )
            }
        }
    }
}
