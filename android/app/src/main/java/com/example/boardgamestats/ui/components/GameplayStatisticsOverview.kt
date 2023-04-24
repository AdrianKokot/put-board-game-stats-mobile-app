package com.example.boardgamestats.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.boardgamestats.models.BoardGameStats
import com.example.boardgamestats.models.PlayerStats
import com.example.boardgamestats.utils.toDaysAgo
import com.example.boardgamestats.utils.toTimeString


@Composable
fun GameplayStatisticsOverview(
    stats: BoardGameStats? = null,
    context: Context = LocalContext.current,
    gridTitleTextStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
    gridLabelTextStyle: TextStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
) {
    if (stats == null) {
        return
    }

    val statistics by remember {
        derivedStateOf {
            listOf(
                listOf(
                    "Highest score" to stats.highestScore.toString(),
                    "Avg score" to stats.avgScore.toString(),
                    "Lowest score" to stats.lowestScore.toString()
                ),
                listOf(
                    "Last played" to stats.lastPlay.toDaysAgo(context),
                    "Avg playtime" to if (stats.avgPlaytime > 0) stats.avgPlaytime.toTimeString() else "No data",
                    "Total plays" to stats.playsCount.toString()
                )
            )
        }
    }

    MapGrid(statistics, gridTitleTextStyle, gridLabelTextStyle)
}


@Composable
fun PlayerStatisticsOverview(
    stats: PlayerStats? = null,
    gridTitleTextStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
    gridLabelTextStyle: TextStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
) {
    if (stats == null) {
        return
    }

    val statistics by remember {
        derivedStateOf {
            listOf(
                listOf(
                    "Highest score" to stats.highestScore.toString(),
                    "Play count" to stats.playCount.toString(),
                    "Win count" to stats.winCount.toString()
                )
            )
        }
    }
    Column {
        Text(
            stats.name,
            style = MaterialTheme.typography.titleLarge.copy(color = gridTitleTextStyle.color),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        MapGrid(statistics, gridTitleTextStyle, gridLabelTextStyle)
    }
}

@Composable
fun MapGrid(
    grid: List<List<Pair<String, String>>>,
    gridTitleTextStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
    gridLabelTextStyle: TextStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = gridTitleTextStyle.lineHeight.value.dp)) {
        grid.forEach { list ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = gridTitleTextStyle.lineHeight.value.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                list.forEach { pair ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            pair.second,
                            style = gridTitleTextStyle
                        )
                        Text(
                            pair.first,
                            style = gridLabelTextStyle
                        )
                    }
                }
            }
        }
    }
}