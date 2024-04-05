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
            withTimeoutOrNull(powerAmpTimeout - 1000) {
                if (isStream) {
                    searchForLyrics(track) {
                        Log.d(TAG, "handleLyricsRequest: failed - $it")
                        if (AppPreference.getDummyForStreams(context))
                            sendLyrics(getDummyLyrics(context))
                    }
                } else {
                    if (!track.artistName.isNullOrEmpty() && !track.albumName.isNullOrEmpty()) {
                        LyricsApiHelper.getTopMatchingLyrics(
                            track,
                            onResult = { lyrics -> sendLyrics(lyrics) },
                            onFail = {
                                Log.d(TAG, "getTopMatchingLyrics: failed - $it")
                                launch {
                                    searchForLyrics(track) { errMsg ->
                                        Log.d(TAG, "handleLyricsRequest: search Failed $errMsg")
                                        if (AppPreference.getDummyForTracks(context))
                                            sendLyrics(getDummyLyrics(context))

                                    }
                                }
                            })
                    } else {
                        searchForLyrics(track) {
                            Log.d(TAG, "getLyricsForTrack: failed - $it")
                            sendLyrics(getDummyLyrics(context))
                        }
                    }
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

    private suspend fun searchForLyrics(track: Track, onError: (String) -> Unit) {
        LyricsApiHelper.getLyricsForTrack(track, onResult = {
            val lyrics = it.first()
            sendLyrics(lyrics)
        }, onError = onError)
    }

    private fun sendLyrics(lyrics: Lyrics) {
        sendLyricResponse(context, realId, lyrics)
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