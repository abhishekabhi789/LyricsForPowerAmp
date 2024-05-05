package io.github.abhishekabhi789.lyricsforpoweramp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import io.github.abhishekabhi789.lyricsforpoweramp.PowerAmpIntentUtils.sendLyricResponse
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.properties.Delegates

class LyricsRequestReceiver : BroadcastReceiver() {
    private val TAG = javaClass.simpleName
    private var realId by Delegates.notNull<Long>()
    private lateinit var context: Context
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            PowerampAPI.Lyrics.ACTION_NEED_LYRICS ->
                if (context != null) handleLyricsRequest(context, intent)
        }
    }

    private fun handleLyricsRequest(context: Context, intent: Intent) {
        this.context = context
        realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, PowerampAPI.NO_ID)
        val isStream = intent.getIntExtra(
            PowerampAPI.Track.FILE_TYPE,
            PowerampAPI.Track.FileType.TYPE_UNKNOWN
        ) == PowerampAPI.Track.FileType.TYPE_STREAM
        val track = PowerAmpIntentUtils.makeTrack(context, intent)
        val powerAmpTimeout = 5000L
        Log.i(TAG, "handleLyricsRequest: request for $track")
        val job = CoroutineScope(Dispatchers.IO).launch {
            withTimeoutOrNull(powerAmpTimeout) {
                getLyrics(context, track, onError = {
                    Log.e(TAG, "handleLyricsRequest: $it")
                    sendLyrics(isStream, null)
                }, onSuccess = { sendLyrics(isStream, it) })
            }
        }

        job.invokeOnCompletion {
            if (job.isCancelled) {
                Log.d(TAG, "handleLyricsRequest: timeout cancelled")
                sendLyricResponse(context, realId, lyrics = null)
            }
            Log.i(TAG, "handleLyricsRequest: network request completed")
        }
    }

    private suspend fun getLyrics(
        context: Context,
        track: Track,
        onSuccess: (Lyrics) -> Unit,
        onError: (String) -> Unit
    ) {
        val useFallbackMethod = AppPreference.getSearchIfGetFailed(context)
        Log.i(TAG, "getLyrics: fallback to search permitted- $useFallbackMethod")
        LyricsApiHelper.getLyricsForTracks(
            track = track,
            onResult = onSuccess,
            onFail = { errMsg ->
                if (useFallbackMethod) {
                    Log.i(TAG, "getLyrics: trying with search method")
                    CoroutineScope(Dispatchers.IO).launch {
                        LyricsApiHelper.searchLyricsForTrack(
                            query = track,
                            onResult = { onSuccess(it.first()) },
                            onError = {
                                onError("searchLyricsForTrack: failed - $it")
                            }
                        )
                    }
                } else {
                    Log.e(TAG, "getLyrics: no results, fallback not permitted")
                    onError("getLyricsForTracks: failed - $errMsg")
                }
            })
    }

    private fun sendLyrics(isStream: Boolean, lyrics: Lyrics?): Boolean {
        if (lyrics == null) {
            val sendDummy = if (isStream) AppPreference.getDummyForStreams(context)
            else AppPreference.getDummyForTracks(context)
            if (sendDummy) {
                val sent = sendLyricResponse(context, realId, getDummyLyrics(context))
                Log.i(TAG, "sendLyrics: dummyLyrics sent : $sent ")
                return sent
            }
            Log.i(TAG, "sendLyrics: dummyLyrics won't be send")
            return false
        } else {
            val sent = sendLyricResponse(context, realId, lyrics)
            Log.i(TAG, "sendLyrics: lyrics sent : $sent")
            return sent
        }
    }

    private fun getDummyLyrics(context: Context): Lyrics {
        val dummyLyric = context.getString(R.string.no_lyrics_response).trimMargin()
        return Lyrics(
            trackName = "",
            artistName = "",
            albumName = "",
            plainLyrics = dummyLyric,
            syncedLyrics = null,
            duration = 0,
        )
    }
}
