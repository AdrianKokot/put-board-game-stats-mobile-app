package com.example.boardgamestats.utils

import android.content.Context
import android.text.format.DateFormat
import java.time.Instant
import java.time.Period
import java.time.ZoneId

fun Long.toDaysAgo(context: Context): String {
    val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
    val localDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    val days = Period.between(localDate, now).days
    return when {
        days <= 0 -> "Today"
        days == 1 -> "Yesterday"
        days < 8 -> "$days days ago"
        else -> DateFormat.getDateFormat(context).format(this)
    }
}

fun Long.toTimeString(): String {
    return "${this / 60L}h ${this % 60L}min"
}