package com.example.boardgamestats.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.boardgamestats.ui.animations.SkeletonAnimatedColor

@Composable
fun ListItemWithAsyncImage(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable() (() -> Unit)? = null,
    supportingContent: @Composable() (() -> Unit)? = null,
    trailingContent: @Composable() (() -> Unit)? = null,
    model: Any? = null,
    contentDescription: String? = null
) {
    ListItem(
        headlineContent,
        modifier,
        overlineContent, supportingContent,
        leadingContent = {
            SubcomposeAsyncImage(
                modifier = Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.extraSmall),
                model = model,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(Modifier.matchParentSize().background(SkeletonAnimatedColor()))
                }
            )
        },
        trailingContent
    )
}
