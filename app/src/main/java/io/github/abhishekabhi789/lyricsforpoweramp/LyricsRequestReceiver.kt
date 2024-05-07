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
    private var track: Track? = null
    private lateinit var notificationHelper: NotificationHelper
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            PowerampAPI.Lyrics.ACTION_NEED_LYRICS ->
                if (context != null) {
                    notificationHelper = NotificationHelper(context)
                    handleLyricsRequest(context, intent)
                }
        }
    }

    private fun handleLyricsRequest(context: Context, intent: Intent) {
        this.context = context
        notify(content = context.getString(R.string.preparing_search_track))
        realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, PowerampAPI.NO_ID)
        track = PowerAmpIntentUtils.makeTrack(context, intent)
        val powerAmpTimeout = 5000L
        Log.i(TAG, "handleLyricsRequest: request for $track")
        notify(content = context.getString(R.string.making_network_requests))
        val job = CoroutineScope(Dispatchers.IO).launch {
            withTimeoutOrNull(powerAmpTimeout) {
                getLyrics(context, track!!, onError = {
                    notify(context.getString(R.string.error) + ": $it")
                    Log.e(TAG, "handleLyricsRequest: $it")
                    suggestManualSearch()
                }, onSuccess = {
                    notify(context.getString(R.string.sending_lyrics))
                    sendLyrics(it)
                    notificationHelper.cancelNotification()
                })
            }
        }

        job.invokeOnCompletion {
            if (job.isCancelled) {
                notify(context.getString(R.string.timeout_cancelled))
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
                    notify(context.getString(R.string.notification_get_failed_and_trying_search))
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

    private fun sendLyrics(lyrics: Lyrics?): Boolean {
        val sent = sendLyricResponse(context, realId, lyrics)
        Log.i(TAG, "sendLyrics: lyrics sent : $sent")
        val status = if (sent) R.string.sent else R.string.could_not_sent
        notify("${context.getString(R.string.lyrics)} ${context.getString(status)}") //check possible grammar issues
        return sent

    }

    private fun suggestManualSearch() {
        notify(context.getString(R.string.notification_manual_search_suggestion), track)
    }

    private fun notify(
        content: String,
        data: Track? = null
    ) {
        val titleString = context.getString(R.string.request_handling_notification_title)
        val title =
            if (!track?.trackName.isNullOrEmpty()) "$titleString - ${track?.trackName}" else titleString
        val subText =
            if (track?.trackName.isNullOrEmpty()) "${context.getString(R.string.track)}: ${track?.trackName}" else null
        notificationHelper.makeNotification(title, content, subText, data)
    }

    companion object {
        const val MANUAL_SEARCH_ACTION =
            "io.github.abhishekabhi789.lyricsforpoweramp.MANUAL_SEARCH_ACTION"
    }
}
