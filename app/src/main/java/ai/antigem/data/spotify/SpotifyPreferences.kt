package ai.antigem.data.spotify

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.spotifyDataStore by preferencesDataStore(name = "spotify_prefs")

/**
 * DataStore-backed preferences for Spotify integration.
 * No auth tokens stored: the broadcast approach needs no OAuth.
 */
object SpotifyPreferences {

    private object Keys {
        val SHOW_LYRICS_BY_DEFAULT = booleanPreferencesKey("show_lyrics_by_default")
        val LYRICS_OPACITY        = stringPreferencesKey("lyrics_opacity")       // "0.75"
        val SHOW_PLAYER_BAR       = booleanPreferencesKey("show_player_bar")
    }

    // ── read ────────────────────────────────────────────────────────────────

    fun showLyricsByDefault(ctx: Context): Flow<Boolean> =
        ctx.spotifyDataStore.data.map { it[Keys.SHOW_LYRICS_BY_DEFAULT] ?: false }

    fun showPlayerBar(ctx: Context): Flow<Boolean> =
        ctx.spotifyDataStore.data.map { it[Keys.SHOW_PLAYER_BAR] ?: true }

    // ── write ───────────────────────────────────────────────────────────────

    suspend fun setShowLyricsByDefault(ctx: Context, value: Boolean) {
        ctx.spotifyDataStore.edit { it[Keys.SHOW_LYRICS_BY_DEFAULT] = value }
    }

    suspend fun setShowPlayerBar(ctx: Context, value: Boolean) {
        ctx.spotifyDataStore.edit { it[Keys.SHOW_PLAYER_BAR] = value }
    }
}
