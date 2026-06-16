package me.algosketch.timelog.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector

fun String.toMaterialIcon(): ImageVector = when (this) {
    "play_arrow" -> Icons.Default.PlayArrow
    "free_breakfast" -> Icons.Default.FreeBreakfast
    "edit_note" -> Icons.Default.EditNote
    "menu_book" -> Icons.Default.MenuBook
    "lightbulb" -> Icons.Default.Lightbulb
    "track_changes" -> Icons.Default.TrackChanges
    "directions_run" -> Icons.Default.DirectionsRun
    "palette" -> Icons.Default.Palette
    "laptop" -> Icons.Default.Laptop
    "music_note" -> Icons.Default.MusicNote
    "bolt" -> Icons.Default.Bolt
    "star" -> Icons.Default.Star
    "stop" -> Icons.Default.Stop
    else -> Icons.Default.PlayArrow
}
