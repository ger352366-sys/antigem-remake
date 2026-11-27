package ai.antigem.data.spotify

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Fetches synced / plain lyrics from lrclib.net (free, no auth, CORS-free).
 *
 * API: GET https://lrclib.net/api/get
 *   ?track_name=<name>&artist_name=<artist>[&album_name=<album>][&duration=<secs>]
 *
 * Response JSON fields used:
 *   syncedLyrics – LRC format: "[mm:ss.xx] text" lines (may be null)
 *   plainLyrics  – fallback plain text (may be null)
 *
 * Cache: in-memory HashMap keyed by "$trackName:$artistName" (cleared on new session).
 */
object SpotifyLyricsRepository {

    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val cache = HashMap<String, List<LyricLine>?>()

    // ── public ──────────────────────────────────────────────────────────────

    /**
     * Returns lyrics for the given track, or null if unavailable.
     * Runs on whatever coroutine context the caller is on – use IO dispatcher.
     */
    fun getLyrics(
        trackName: String,
        artistName: String,
        albumName: String? = null,
        durationSecs: Int? = null
    ): List<LyricLine>? {
        val key = "$trackName:$artistName"
        if (cache.containsKey(key)) return cache[key]

        val url = buildString {
            append("https://lrclib.net/api/get?track_name=")
            append(trackName.urlEncode())
            append("&artist_name=")
            append(artistName.urlEncode())
            if (albumName != null) { append("&album_name="); append(albumName.urlEncode()) }
            if (durationSecs != null) { append("&duration="); append(durationSecs) }
        }

        return try {
            val response = http.newCall(Request.Builder().url(url).build()).execute()
            if (!response.isSuccessful) { cache[key] = null; return null }
            val body = response.body?.string() ?: run { cache[key] = null; return null }
            val json = JSONObject(body)
            val parsed = parseSynced(json.optString("syncedLyrics", ""))
                ?: parsePlain(json.optString("plainLyrics", ""))
            cache[key] = parsed
            parsed
        } catch (_: Exception) {
            null
        }
    }

    fun clearCache() = cache.clear()

    // ── parsing ─────────────────────────────────────────────────────────────

    /** Parses "[mm:ss.xx] text" LRC lines into LyricLine list. */
    private fun parseSynced(raw: String): List<LyricLine>? {
        if (raw.isBlank()) return null
        val lines = raw.lines()
        val result = ArrayList<LyricLine>(lines.size)
        val re = Regex("""^\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)$""")
        var matched = 0
        for (line in lines) {
            val m = re.matchEntire(line.trim()) ?: continue
            val min  = m.groupValues[1].toLongOrNull() ?: continue
            val sec  = m.groupValues[2].toLongOrNull() ?: continue
            val csec = m.groupValues[3].toLong()
            val mult = if (m.groupValues[3].length == 2) 10L else 1L
            val ms   = min * 60_000L + sec * 1_000L + csec * mult
            result += LyricLine(ms, m.groupValues[4].trim())
            matched++
        }
        return if (matched > 0) result else null
    }

    /** Splits plain lyrics into un-timed LyricLine list (timeMs = -1). */
    private fun parsePlain(raw: String): List<LyricLine>? {
        if (raw.isBlank()) return null
        return raw.lines()
            .map { LyricLine(-1L, it) }
            .takeIf { it.isNotEmpty() }
    }

    private fun String.urlEncode(): String =
        java.net.URLEncoder.encode(this, "UTF-8")
}
