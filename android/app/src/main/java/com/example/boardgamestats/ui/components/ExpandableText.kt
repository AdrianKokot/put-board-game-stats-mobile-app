package com.example.boardgamestats.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 3,
    style: TextStyle = LocalTextStyle.current
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var didOverflow by rememberSaveable { mutableStateOf(false) }

    Text(
        text,
        maxLines = if (isExpanded) Int.MAX_VALUE else maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = {
            didOverflow = it.didOverflowHeight
        },
        modifier = modifier.animateContentSize().clickable(enabled = didOverflow || isExpanded) { isExpanded = !isExpanded },
        style = style
    )
}
