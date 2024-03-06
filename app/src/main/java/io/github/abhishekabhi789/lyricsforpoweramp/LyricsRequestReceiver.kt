package io.github.abhishekabhi789.lyricsforpoweramp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import io.github.abhishekabhi789.lyricsforpoweramp.PowerAmpIntentUtils.sendLyricResponse
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
        val track = PowerAmpIntentUtils.makeTrack(intent)
        val powerAmpTimeout = 5000L
        val dummyLyrics = context.getString(R.string.no_lyrics_response).trimMargin()
        Log.i(TAG, "handleLyricsRequest: request for $track")
        val job = GlobalScope.launch(Dispatchers.IO) {
            withTimeoutOrNull(powerAmpTimeout - 1000) {
                LyricsApiHelper.getTopMatchingLyrics(
                    track,
                    onResult = { lyrics ->
                        //if lyrics null; sending a fake lyrics, so that user can open the lyrics search activity for the track.
                        lyrics ?: dummyLyrics
                        sendLyricResponse(context, realId, lyrics)
                    },
                    onFail = {
                        Log.d(TAG, "handleLyricsRequest: no lyrics - $it")
                        sendLyricResponse(context, realId, dummyLyrics)
                    })
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
}