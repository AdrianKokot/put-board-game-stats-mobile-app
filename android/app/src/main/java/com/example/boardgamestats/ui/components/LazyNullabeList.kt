package com.example.boardgamestats.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.boardgamestats.ui.animations.SkeletonAnimatedColor


@Composable
fun <T> LazyNullableList(
    list: List<T>? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    LazyColumn(contentPadding = contentPadding, modifier = Modifier.fillMaxHeight()) {
        if (list == null) {
            items(5) {
                ListItem(
                    headlineContent = {
                        Text("", Modifier.width(160.dp).padding(bottom = 2.dp).background(SkeletonAnimatedColor()))
                    },
                    supportingContent = {
                        Text("", Modifier.width(128.dp).background(SkeletonAnimatedColor()))
                    },
                    leadingContent = {
                        Box(
                            Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.extraSmall)
                                .background(SkeletonAnimatedColor())
                        )
                    }
                )
            }
        } else {
            items(list, itemContent = itemContent)
        }
    }
}
