package ai.antigem.ui.spotify

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ai.antigem.data.spotify.SpotifyRemoteManager
import ai.antigem.data.spotify.SpotifyRemoteManager.ConnectionState
import ai.antigem.i18n.L

/** Spotify brand green. */
val SpotifyGreen = Color(0xFF1DB954)

/**
 * Persistent compact player bar shown above the input area when Spotify is active.
 * Reads [SpotifyRemoteManager.playerState] and [SpotifyRemoteManager.positionMs].
 * Hidden when no track has been seen yet (IDLE + null).
 */
@Composable
fun SpotifyPlayerBar(
    lyricsVisible: Boolean,
    onToggleLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerState by SpotifyRemoteManager.playerState.collectAsState()
    val connState   by SpotifyRemoteManager.connectionState.collectAsState()
    val posMs       by SpotifyRemoteManager.positionMs.collectAsState()

    // ── position ticker — fires every 500 ms while playing ──────────────────
    LaunchedEffect(playerState?.isPlaying) {
        while (isActive) {
            delay(500L)
            SpotifyRemoteManager.tickPosition()
        }
    }

    val track   = playerState
    val visible = connState == ConnectionState.ACTIVE || track != null

    AnimatedVisibility(
        visible = visible,
        enter   = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit    = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        modifier = modifier
    ) {
        val surface   = MaterialTheme.colorScheme.surfaceContainer
        val onSurface = MaterialTheme.colorScheme.onSurface

        Surface(
            tonalElevation   = 2.dp,
            shadowElevation  = 4.dp,
            color            = surface.copy(alpha = 0.97f),
            modifier         = Modifier.fillMaxWidth()
        ) {
            if (track == null) {
                // Spotify not yet playing → "open Spotify" row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { SpotifyRemoteManager.openSpotify(context) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.MusicNote, null, tint = SpotifyGreen, modifier = Modifier.size(20.dp))
                    Text(L.spotify.player.connectFirst, fontSize = 13.sp, color = onSurface.copy(0.7f))
                }
                return@Surface
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // ── top row: art + info + controls ─────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // album art
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SpotifyGreen.copy(alpha = 0.15f))
                    ) {
                        if (track.albumArtUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(track.albumArtUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Filled.MusicNote, null,
                                tint = SpotifyGreen,
                                modifier = Modifier.align(Alignment.Center).size(22.dp)
                            )
                        }
                    }

                    // track name + artist
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            track.name,
                            fontSize    = 13.sp,
                            fontWeight  = FontWeight.SemiBold,
                            maxLines    = 1,
                            overflow    = TextOverflow.Ellipsis,
                            color       = onSurface
                        )
                        Text(
                            track.artist,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color    = onSurface.copy(0.6f)
                        )
                    }

                    // ⏮  control
                    IconButton(onClick = { SpotifyRemoteManager.skipPrev() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.SkipPrevious, L.spotify.player.skipPrev, tint = onSurface, modifier = Modifier.size(20.dp))
                    }

                    // ▶/⏸ control
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen)
                            .clickable { SpotifyRemoteManager.playPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (track.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            if (track.isPlaying) L.spotify.player.pause else L.spotify.player.play,
                            tint     = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // ⏭  control
                    IconButton(onClick = { SpotifyRemoteManager.skipNext() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.SkipNext, L.spotify.player.skipNext, tint = onSurface, modifier = Modifier.size(20.dp))
                    }

                    // 📄 lyrics toggle
                    IconButton(onClick = onToggleLyrics, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.Lyrics,
                            if (lyricsVisible) L.spotify.lyrics.hideLyrics else L.spotify.lyrics.showLyrics,
                            tint     = if (lyricsVisible) SpotifyGreen else onSurface.copy(0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // ── progress bar ───────────────────────────────────────────
                if (track.durationMs > 0L) {
                    val progress = (posMs.toFloat() / track.durationMs.toFloat()).coerceIn(0f, 1f)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress    = { progress },
                        modifier    = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
                        color       = SpotifyGreen,
                        trackColor  = SpotifyGreen.copy(0.2f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatMs(posMs), fontSize = 9.sp, color = onSurface.copy(0.4f))
                        Text(formatMs(track.durationMs), fontSize = 9.sp, color = onSurface.copy(0.4f))
                    }
                }
            }
        }
    }
}

// ── private helpers ───────────────────────────────────────────────────────────

private fun formatMs(ms: Long): String {
    val s = ms / 1000L
    return "%d:%02d".format(s / 60, s % 60)
}
