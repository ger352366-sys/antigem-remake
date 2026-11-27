package ai.antigem.ui.spotify

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ai.antigem.data.spotify.SpotifyPreferences
import ai.antigem.data.spotify.SpotifyRemoteManager
import ai.antigem.data.spotify.SpotifyRemoteManager.ConnectionState
import ai.antigem.i18n.L

/**
 * Spotify settings card rendered inside the Settings dialog (SPOTIFY section).
 * Shows: open/connect button, current track info if active, lyrics-by-default toggle,
 * player-bar toggle, and a note about lrclib.net + "requires Spotify app" footnote.
 */
@Composable
fun SpotifySettingsCard() {
    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()
    val connState  by SpotifyRemoteManager.connectionState.collectAsState()
    val playerState by SpotifyRemoteManager.playerState.collectAsState()

    val showLyricsDefault by SpotifyPreferences.showLyricsByDefault(context).collectAsState(initial = false)
    val showPlayerBar     by SpotifyPreferences.showPlayerBar(context).collectAsState(initial = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SpotifyGreen.copy(0.12f), SpotifyGreen.copy(0.04f))
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── header ──────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SpotifyGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MusicNote, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column {
                Text("Spotify", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(L.spotify.settings.about, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), lineHeight = 16.sp)
            }
        }

        HorizontalDivider(color = SpotifyGreen.copy(0.15f))

        // ── connection status ────────────────────────────────────────────────
        val isActive = connState == ConnectionState.ACTIVE
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    if (isActive) L.spotify.auth.connected else L.spotify.settings.notConnected,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = if (isActive) SpotifyGreen else MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
                if (isActive && playerState != null) {
                    Text(
                        "${playerState!!.name} · ${playerState!!.artist}",
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            }
            Button(
                onClick = { SpotifyRemoteManager.openSpotify(context) },
                colors  = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                shape   = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Outlined.OpenInNew, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text(L.spotify.settings.connect, fontSize = 13.sp, color = Color.White)
            }
        }

        HorizontalDivider(color = SpotifyGreen.copy(0.10f))

        // ── toggles ──────────────────────────────────────────────────────────
        SpotifyToggleRow(
            label   = L.spotify.settings.lyricsDefault,
            checked = showLyricsDefault,
            onCheckedChange = { v -> scope.launch { SpotifyPreferences.setShowLyricsByDefault(context, v) } }
        )
        SpotifyToggleRow(
            label   = L.spotify.settings.showPlayerBar,
            checked = showPlayerBar,
            onCheckedChange = { v -> scope.launch { SpotifyPreferences.setShowPlayerBar(context, v) } }
        )

        HorizontalDivider(color = SpotifyGreen.copy(0.10f))

        // ── footnotes ────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(L.spotify.settings.lyricsSource, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.45f))
            Text(L.spotify.settings.requiresApp,  fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.45f))
        }
    }
}

@Composable
private fun SpotifyToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SpotifyGreen)
        )
    }
}
