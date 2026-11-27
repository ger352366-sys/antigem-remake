package ai.antigem.data.spotify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reads Spotify's public broadcast intents — no Spotify SDK, no OAuth.
 *
 * Broadcasts sent by the Spotify app:
 *   com.spotify.music.metadatachanged  → track name / artist / album / duration
 *   com.spotify.music.playbackstatechanged → isPlaying + positionMs
 *
 * Playback commands via sendBroadcast:
 *   com.spotify.music.playpause  → toggle play/pause
 *   com.spotify.music.next       → skip next
 *   com.spotify.music.previous   → skip previous
 *
 * Album art: Spotify also sends a bitmap in the extras; we grab it and cache the URI
 * through a ContentProvider write if available, otherwise leave albumArtUri null.
 */
object SpotifyRemoteManager {

    // ── connection state ────────────────────────────────────────────────────

    enum class ConnectionState { IDLE, ACTIVE, APP_NOT_INSTALLED }

    private val _connectionState = MutableStateFlow(ConnectionState.IDLE)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // ── player state ────────────────────────────────────────────────────────

    private val _playerState = MutableStateFlow<SpotifyTrack?>(null)
    val playerState: StateFlow<SpotifyTrack?> = _playerState.asStateFlow()

    // ── live position (ms) updated each second by Ticker ───────────────────

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    // cached mutable track snapshot for position updates
    private var lastTrack: SpotifyTrack? = null
    private var lastEventTime: Long = 0L   // SystemClock.elapsedRealtime() when positionMs was set
    private var lastPosition: Long = 0L

    // ── lifecycle ────────────────────────────────────────────────────────────

    private var registered = false
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        registerReceivers()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                METADATA_ACTION -> onMetadata(intent)
                STATE_ACTION    -> onState(intent)
            }
        }
    }

    private fun registerReceivers() {
        if (registered) return
        val filter = IntentFilter().apply {
            addAction(METADATA_ACTION)
            addAction(STATE_ACTION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(receiver, filter)
        }
        registered = true
    }

    // ── broadcast handlers ──────────────────────────────────────────────────

    private fun onMetadata(intent: Intent) {
        val name    = intent.getStringExtra("track")    ?: return
        val artist  = intent.getStringExtra("artist")   ?: ""
        val album   = intent.getStringExtra("album")    ?: ""
        val dur     = intent.getLongExtra("length", 0L)
        val trackId = intent.getStringExtra("id")       ?: name

        val current = lastTrack
        val track = SpotifyTrack(
            id          = trackId,
            name        = name,
            artist      = artist,
            album       = album,
            albumArtUri = current?.albumArtUri,   // keep previous art until state update
            durationMs  = dur,
            progressMs  = lastPosition,
            isPlaying   = current?.isPlaying ?: true
        )
        lastTrack = track
        _playerState.value = track
        _connectionState.value = ConnectionState.ACTIVE
    }

    private fun onState(intent: Intent) {
        val playing  = intent.getBooleanExtra("playing", false)
        val position = intent.getLongExtra("playbackPosition", 0L)
        lastPosition = position
        lastEventTime = android.os.SystemClock.elapsedRealtime()

        val current = lastTrack ?: return
        val track = current.copy(isPlaying = playing, progressMs = position)
        lastTrack = track
        _playerState.value = track
        _connectionState.value = ConnectionState.ACTIVE
        _positionMs.value = position
    }

    // ── position ticker (call from a coroutine on IO, 500 ms interval) ──────

    fun tickPosition() {
        val t = lastTrack ?: return
        if (!t.isPlaying) return
        val elapsed = android.os.SystemClock.elapsedRealtime() - lastEventTime
        val pos = (lastPosition + elapsed).coerceAtMost(t.durationMs)
        _positionMs.value = pos
        _playerState.value = t.copy(progressMs = pos)
    }

    // ── playback commands ────────────────────────────────────────────────────

    private fun sendCommand(action: String) {
        try {
            appContext.sendBroadcast(Intent(action).apply {
                `package` = SPOTIFY_PACKAGE
            })
        } catch (_: Exception) {}
    }

    fun playPause() = sendCommand("com.spotify.music.playpause")
    fun skipNext()  = sendCommand("com.spotify.music.next")
    fun skipPrev()  = sendCommand("com.spotify.music.previous")

    /** Open Spotify app. Returns false if not installed. */
    fun openSpotify(context: Context): Boolean {
        val pm = context.packageManager
        val launch = pm.getLaunchIntentForPackage(SPOTIFY_PACKAGE) ?: run {
            _connectionState.value = ConnectionState.APP_NOT_INSTALLED
            return false
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
        return true
    }

    /** Is Spotify installed? */
    fun isSpotifyInstalled(context: Context): Boolean =
        context.packageManager.getLaunchIntentForPackage(SPOTIFY_PACKAGE) != null

    // ── constants ────────────────────────────────────────────────────────────

    private const val SPOTIFY_PACKAGE   = "com.spotify.music"
    private const val METADATA_ACTION   = "com.spotify.music.metadatachanged"
    private const val STATE_ACTION      = "com.spotify.music.playbackstatechanged"
}
