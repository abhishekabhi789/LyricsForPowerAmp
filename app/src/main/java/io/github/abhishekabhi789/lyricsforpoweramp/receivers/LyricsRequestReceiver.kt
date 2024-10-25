package io.github.abhishekabhi789.lyricsforpoweramp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import io.github.abhishekabhi789.App
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.LrclibApiHelper
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.NotificationHelper
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.PowerampApiHelper
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.PowerampApiHelper.sendLyricResponse
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class LyricsRequestReceiver : BroadcastReceiver() {

    private var realId = PowerampAPI.NO_ID
    private lateinit var mContext: Context
    private lateinit var lrclibApiHelper: LrclibApiHelper
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var track: Track

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            this.mContext = context
            val application = context.applicationContext as App
            lrclibApiHelper = application.lrclibApiHelper
            when (intent.action) {
                PowerampAPI.Lyrics.ACTION_NEED_LYRICS -> {
                    notificationHelper = NotificationHelper(context)
                    handleLyricsRequest(intent)
                }
            }
        }
    }

    private fun handleLyricsRequest(intent: Intent) {
        notify(content = mContext.getString(R.string.preparing_search_track))
        realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, PowerampAPI.NO_ID)
        track = PowerampApiHelper.makeTrack(mContext, intent)
        Log.i(TAG, "handleLyricsRequest: request for $track")
        notify(content = mContext.getString(R.string.making_network_requests))
        val job = CoroutineScope(Dispatchers.IO).launch {
            withTimeoutOrNull(POWERAMP_LYRICS_REQUEST_WAIT_TIMEOUT) {
                getLyrics(
                    track = track,
                    onSuccess = {
                        notify(mContext.getString(R.string.sending_lyrics))
                        sendLyrics(it)
                        notificationHelper.cancelNotification()
                    },
                    onError = {
                        notify(mContext.getString(R.string.error) + ": $it")
                        Log.e(TAG, "handleLyricsRequest: $it")
                        suggestManualSearch()
                    },
                )
            }
        }

        job.invokeOnCompletion {
            if (job.isCancelled) {
                notify(mContext.getString(R.string.timeout_cancelled))
                Log.d(TAG, "handleLyricsRequest: timeout cancelled")
                sendLyricResponse(mContext, realId, lyrics = null)
            }
            Log.i(TAG, "handleLyricsRequest: network request completed")
        }
    }

    private suspend fun getLyrics(
        track: Track,
        onSuccess: (Lyrics) -> Unit,
        onError: (String) -> Unit
    ) {
        val useFallbackMethod = AppPreference.getSearchIfGetFailed(mContext)
        Log.i(TAG, "getLyrics: fallback to search permitted- $useFallbackMethod")
        lrclibApiHelper.getLyricsForTracks(
            track = track,
            onResult = onSuccess,
            onError = { errMsg ->
                if (useFallbackMethod) {
                    notify(mContext.getString(R.string.notification_get_failed_trying_search))
                    Log.i(TAG, "getLyrics: trying with search method")
                    CoroutineScope(Dispatchers.IO).launch {
                        lrclibApiHelper.searchLyricsForTrack(
                            query = track,
                            onResult = { onSuccess(it.first()) },
                            onError = { onError("getLyrics: failed - $it") }
                        )
                    }
                } else {
                    Log.e(TAG, "getLyrics: no results, fallback not permitted")
                    onError("getLyrics: failed - $errMsg")
                }
            }
        )
    }

    private fun sendLyrics(lyrics: Lyrics?): Boolean {
        val sent = sendLyricResponse(mContext, realId, lyrics)
        val status = if (sent) R.string.sent else R.string.failed_to_send
        notify("${mContext.getString(R.string.lyrics)} ${mContext.getString(status)}")
        return sent
    }

    private fun suggestManualSearch() {
        notify(mContext.getString(R.string.notification_manual_search_suggestion), track)
    }

    private fun notify(content: String, data: Track? = null) {
        val titleString = mContext.getString(R.string.request_handling_notification_title)
        val (title, subText) = if (::track.isInitialized) {
            Pair(
                "$titleString - ${track.trackName}",
                "${mContext.getString(R.string.track)}: ${track.trackName}"
            )
        } else Pair(titleString, null)
        notificationHelper.makeNotification(title, content, subText, data)
    }

    companion object {
        private const val TAG = "LyricsRequestReceiver"
        const val MANUAL_SEARCH_ACTION =
            "io.github.abhishekabhi789.lyricsforpoweramp.MANUAL_SEARCH_ACTION"
        const val POWERAMP_LYRICS_REQUEST_WAIT_TIMEOUT = 5000L
    }
}
