package io.github.abhishekabhi789.lyricsforpoweramp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import io.github.abhishekabhi789.lyricsforpoweramp.PowerAmpIntentUtils.sendLyricResponse
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class LyricsRequestReceiver : BroadcastReceiver() {
    private val TAG = javaClass.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            PowerampAPI.Lyrics.ACTION_NEED_LYRICS ->
                if (context != null) handleLyricsRequest(context, intent)
        }
    }

    private fun handleLyricsRequest(context: Context, intent: Intent) {
        val realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, PowerampAPI.NO_ID)
        val isStream =
            intent.getIntExtra(
                PowerampAPI.Track.FILE_TYPE,
                PowerampAPI.Track.FileType.TYPE_UNKNOWN
            ) == PowerampAPI.Track.FileType.TYPE_STREAM
        val track = PowerAmpIntentUtils.makeTrack(intent).apply {
            if (isStream) {
                //removing album name and duration since some streams don't provide correct value
                this.albumName = null
                this.duration = null
            }
        }
        val powerAmpTimeout = 5000L
        val dummyLyrics = context.getString(R.string.no_lyrics_response).trimMargin()
        Log.i(TAG, "handleLyricsRequest: request for $track")
        val job = GlobalScope.launch(Dispatchers.IO) {
            withTimeoutOrNull(powerAmpTimeout - 1000) {
                if (isStream) {
                    LyricsApiHelper.getLyricsForTrack(track, onResult = {
                        val lyrics = it.first()
                        val lyric: String? = lyrics.syncedLyrics ?: lyrics.plainLyrics
                        sendLyricResponse(context, realId, lyric, makeInfoLine(context, lyrics))
                    }, onError = {
                        Log.d(TAG, "handleLyricsRequest: failed - $it")
                    })
                } else {
                    LyricsApiHelper.getTopMatchingLyrics(
                        track,
                        onResult = { lyrics ->
                            val lyric = lyrics.syncedLyrics ?: lyrics.plainLyrics ?: dummyLyrics
                            //sending a fake lyrics, so that user can open the lyrics search activity for the track.
                            sendLyricResponse(context, realId, lyric, makeInfoLine(context, lyrics))
                        },
                        onFail = {
                            Log.d(TAG, "handleLyricsRequest: failed - $it")
                            sendLyricResponse(context, realId, dummyLyrics)
                        })
                }
            }
        }
        if (job.isCancelled) {
            Log.d(TAG, "handleLyricsRequest: timeout cancelled")
            sendLyricResponse(context, realId, dummyLyrics)
        }
        if (job.isCompleted) {
            Log.d(TAG, "handleLyricsRequest: request process completed")
        }
    }

    private fun makeInfoLine(context: Context, lyrics: Lyric): String {
        return buildString {
            appendLine("${context.getString(R.string.track_title)}: ${lyrics.trackName}")
            appendLine("${context.getString(R.string.artists)}: ${lyrics.artistName}")
            appendLine("${context.getString(R.string.album_name)}: ${lyrics.albumName}")
            appendLine()
            appendLine(context.getString(R.string.response_footer_text))
        }
    }
}