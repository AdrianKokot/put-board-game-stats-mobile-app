package com.example.boardgamestats.ui.animations

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SkeletonAnimatedColor(): Color {
    val infiniteTransition = rememberInfiniteTransition()

    return infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        targetValue = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
        animationSpec = infiniteRepeatable(
            repeatMode = RepeatMode.Reverse,
            animation = tween(
                durationMillis = 1000,
                easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)
            )
        )
    ).value
}