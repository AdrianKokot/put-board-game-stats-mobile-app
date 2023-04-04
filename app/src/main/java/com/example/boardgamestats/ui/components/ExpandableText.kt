package com.example.boardgamestats.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ExpandableText(text: String, maxLines: Int = 3) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var didOverflow by rememberSaveable { mutableStateOf(false) }
    Column {
        Text(
            text,
            maxLines = if (isExpanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = {
                didOverflow = it.didOverflowHeight
            },
        )
        if (didOverflow || isExpanded) {
            TextButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.align(Alignment.End)) {
                Text(if (isExpanded) "Show less" else "Show more")
            }
        }

    }
}
