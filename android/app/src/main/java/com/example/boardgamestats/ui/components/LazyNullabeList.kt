package com.example.boardgamestats.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    placeholderHasImage: Boolean = true,
    emptyListContent: @Composable (LazyItemScope.() -> Unit) = {},
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    LazyColumn(state = state, contentPadding = contentPadding, modifier = modifier.fillMaxSize()) {
        if (list == null) {
            items(5) {
                ListItem(
                    headlineContent = {
                        Text(
                            "",
                            Modifier.fillMaxWidth().padding(bottom = 5.dp)
                                .background(SkeletonAnimatedColor())
                        )
                    },
                    supportingContent = {
                        Text("", Modifier.width(128.dp).background(SkeletonAnimatedColor()))
                    },
                    leadingContent = if (placeholderHasImage) {
                        {


                            Box(
                                Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.extraSmall)
                                    .background(SkeletonAnimatedColor())
                            )
                        }
                    } else null
                )
            }
        } else if (list.isNotEmpty()) {
            items(list, itemContent = itemContent)
        } else {
            item(content = emptyListContent)
        }
    }
}
