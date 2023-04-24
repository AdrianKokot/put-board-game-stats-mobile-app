package com.example.boardgamestats.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun SectionTitle(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    title: String,
    onArrowClick: (() -> Unit)? = null
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            title,
            style = textStyle
        )

        if (onArrowClick != null) {
            IconButton(
                onClick = onArrowClick,
                modifier = Modifier.size(textStyle.lineHeight.value.dp),
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null,
                    modifier = Modifier.size(textStyle.fontSize.value.dp)
                )
            }
        }
    }
}