package ai.antigem.ui.spotify

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ai.antigem.data.spotify.LyricLine
import ai.antigem.data.spotify.SpotifyLyricsRepository
import ai.antigem.data.spotify.SpotifyRemoteManager
import ai.antigem.data.spotify.SpotifyTrack
import ai.antigem.i18n.L

/**
 * Semi-transparent lyrics overlay shown above the message list when lyrics are toggled on.
 * Synced lyrics scroll automatically to highlight the current line.
 * Plain (un-timed) lyrics are shown as a static list.
 */
@Composable
fun SpotifyLyricsOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by SpotifyRemoteManager.playerState.collectAsState()
    val posMs       by SpotifyRemoteManager.positionMs.collectAsState()

    val track = playerState

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp, max = 320.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.72f),
                        Color.Black.copy(alpha = 0.55f)
                    )
                )
            )
    ) {
        if (track == null) {
            Text(
                L.spotify.player.noTrack,
                color = Color.White.copy(0.6f),
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LyricsContent(track = track, posMs = posMs)
        }

        // close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp)
        ) {
            Icon(Icons.Outlined.Close, L.spotify.lyrics.hideLyrics, tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
        }

        // "Now playing" label top-left
        if (track != null) {
            Text(
                "${L.spotify.player.nowPlaying}: ${track.name}",
                fontSize = 10.sp,
                color = Color.White.copy(0.5f),
                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 10.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LyricsContent(track: SpotifyTrack, posMs: Long) {
    var lyrics by remember(track.name, track.artist) { mutableStateOf<List<LyricLine>?>(null) }
    var loading by remember(track.name, track.artist) { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // fetch lyrics when track changes
    LaunchedEffect(track.name, track.artist) {
        loading = true
        lyrics = null
        val result = withContext(Dispatchers.IO) {
            SpotifyLyricsRepository.getLyrics(
                trackName  = track.name,
                artistName = track.artist,
                albumName  = track.album.ifBlank { null },
                durationSecs = if (track.durationMs > 0) (track.durationMs / 1000).toInt() else null
            )
        }
        lyrics = result
        loading = false
    }

    when {
        loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = SpotifyGreen,
                        strokeWidth = 2.dp
                    )
                    Text(L.spotify.lyrics.loading, color = Color.White.copy(0.7f), fontSize = 13.sp)
                }
            }
        }
        lyrics == null || lyrics!!.isEmpty() -> {
            Box(Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.TopCenter) {
                Text(L.spotify.lyrics.noLyrics, color = Color.White.copy(0.5f), fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        else -> {
            SyncedLyricsList(lines = lyrics!!, posMs = posMs)
        }
    }
}

@Composable
private fun SyncedLyricsList(lines: List<LyricLine>, posMs: Long) {
    val isSynced = lines.any { it.timeMs >= 0L }

    // current line index: last line whose timeMs <= posMs
    val currentIndex = remember(posMs, lines) {
        if (!isSynced) return@remember -1
        var idx = 0
        for (i in lines.indices) {
            if (lines[i].timeMs <= posMs) idx = i else break
        }
        idx
    }

    val listState = rememberLazyListState()

    // auto-scroll to current line
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && currentIndex < lines.size) {
            listState.animateScrollToItem(currentIndex.coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 28.dp, bottom = 8.dp, start = 12.dp, end = 40.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(lines) { index, line ->
            val isCurrent = isSynced && index == currentIndex
            val isPast    = isSynced && index < currentIndex

            Text(
                text       = line.text.ifBlank { " " },
                fontSize   = if (isCurrent) 15.sp else 13.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color      = when {
                    isCurrent -> SpotifyGreen
                    isPast    -> Color.White.copy(0.45f)
                    else      -> Color.White.copy(0.72f)
                },
                lineHeight = 20.sp,
                textAlign  = TextAlign.Start,
                modifier   = Modifier.fillMaxWidth()
            )
        }
    }
}
