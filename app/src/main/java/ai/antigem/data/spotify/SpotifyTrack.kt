package ai.antigem.data.spotify

/**
 * Snapshot of the currently-playing Spotify track.
 * Populated from the com.spotify.music.metadatachanged / playbackstatechanged broadcasts
 * and from the Android MediaController for position polling.
 */
data class SpotifyTrack(
    val id: String,
    val name: String,
    val artist: String,
    val album: String,
    val albumArtUri: String?,      // content:// or http URI – loaded async by coil
    val durationMs: Long,
    val progressMs: Long,
    val isPlaying: Boolean
)

/**
 * One line of synced / plain lyrics.
 * [timeMs] = -1 for plain (un-timed) lines.
 */
data class LyricLine(
    val timeMs: Long,
    val text: String
)
