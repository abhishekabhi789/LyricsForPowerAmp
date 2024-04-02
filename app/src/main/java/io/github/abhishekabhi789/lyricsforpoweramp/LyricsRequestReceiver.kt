package io.github.abhishekabhi789.lyricsforpoweramp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import io.github.abhishekabhi789.lyricsforpoweramp.PowerAmpIntentUtils.sendLyricResponse
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        val isStream = intent.getIntExtra(
            PowerampAPI.Track.FILE_TYPE,
            PowerampAPI.Track.FileType.TYPE_UNKNOWN
        ) == PowerampAPI.Track.FileType.TYPE_STREAM
        val track = PowerAmpIntentUtils.makeTrack(context, intent)
        val powerAmpTimeout = 5000L
        val dummyLyric = context.getString(R.string.no_lyrics_response).trimMargin()
        Log.i(TAG, "handleLyricsRequest: request for $track")
        val job = CoroutineScope(Dispatchers.IO).launch {
            withTimeoutOrNull(powerAmpTimeout - 1000) {
                if (isStream) {
                    LyricsApiHelper.getLyricsForTrack(track, onResult = {
                        val lyrics = it.first()
                        sendLyricResponse(context, realId, lyrics)
                    }, onError = {
                        Log.d(TAG, "handleLyricsRequest: failed - $it")
                    })
                } else {
                    LyricsApiHelper.getTopMatchingLyrics(
                        track,
                        onResult = { lyrics ->
                            sendLyricResponse(context, realId, lyrics)
                        },
                        onFail = {
                            Log.d(TAG, "handleLyricsRequest: failed - $it")
                            val dummyLyrics = Lyrics(
                                trackName = "",
                                artistName = "",
                                albumName = "",
                                plainLyrics = dummyLyric,
                                syncedLyrics = null,
                                duration = 0,
                            )
                            sendLyricResponse(context, realId, lyrics = dummyLyrics)
                        })
                }
            }
        }
        if (job.isCancelled) {
            Log.d(TAG, "handleLyricsRequest: timeout cancelled")
            sendLyricResponse(context, realId, lyrics = null)
        }
        if (job.isCompleted) {
            Log.d(TAG, "handleLyricsRequest: request process completed")
        }
    }
}