package io.github.abhishekabhi789.lyricsforpoweramp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.HttpClient
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.LrclibApiHelper
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.NotificationHelper
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.PowerampApiHelper
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.PowerampApiHelper.sendLyricResponse
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class LyricsRequestReceiver : BroadcastReceiver() {

    private var realId = PowerampAPI.NO_ID
    private lateinit var mContext: Context
    private lateinit var mLrclibApiHelper: LrclibApiHelper
    private lateinit var mNotificationHelper: NotificationHelper
    private lateinit var mTrack: Track

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            mContext = context
            mLrclibApiHelper = LrclibApiHelper(HttpClient.okHttpClient)
            when (intent.action) {
                PowerampAPI.Lyrics.ACTION_NEED_LYRICS -> {
                    mNotificationHelper = NotificationHelper(context)
                    handleLyricsRequest(intent)
                }
            }
        }
    }

    private fun handleLyricsRequest(
        intent: Intent,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        notify(content = mContext.getString(R.string.preparing_search_track))
        realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, PowerampAPI.NO_ID)
        mTrack = PowerampApiHelper.makeTrack(mContext, intent)
        Log.i(TAG, "handleLyricsRequest: request for $mTrack")

        val job = CoroutineScope(dispatcher).launch {
            withTimeoutOrNull(POWERAMP_LYRICS_REQUEST_WAIT_TIMEOUT) {
                getLyrics(
                    track = mTrack,
                    dispatcher = dispatcher,
                    onSuccess = {
                        notify(mContext.getString(R.string.sending_lyrics))
                        sendLyrics(it).also { success ->
                            if (success) mNotificationHelper.cancelNotification()
                            else suggestManualSearch()
                        }
                    },
                    onError = {
                        notify(mContext.getString(it.errMsg) + " ${it.moreInfo}")
                        Log.e(TAG, "handleLyricsRequest: $it")
                        suggestManualSearch()
                    },
                )
            }
        }

        job.invokeOnCompletion {
            if (job.isCancelled) {
                notify(mContext.getString(R.string.timeout_cancelled))
                Log.i(TAG, "handleLyricsRequest: timeout cancelled")
                sendLyricResponse(mContext, realId, lyrics = null)
            }
            Log.i(TAG, "handleLyricsRequest: network request completed")
        }
    }

    private suspend fun getLyrics(
        track: Track,
        dispatcher: CoroutineDispatcher,
        onSuccess: (Lyrics) -> Unit,
        onError: (LrclibApiHelper.Error) -> Unit
    ) {
        val useFallbackMethod = AppPreference.getSearchIfGetFailed(mContext)
        Log.i(TAG, "getLyrics: fallback to search permitted- $useFallbackMethod")
        notify(content = mContext.getString(R.string.performing_get_method))
        mLrclibApiHelper.getLyricsForTracks(
            track = track,
            dispatcher = dispatcher,
            onResult = onSuccess,
            onError = { error ->
                Log.e(TAG, "getLyrics: get request failed $error")
                if (useFallbackMethod && error == LrclibApiHelper.Error.NO_RESULTS) {
                    notify(mContext.getString(R.string.performing_search_method))
                    Log.i(TAG, "getLyrics: trying with search method")
                    CoroutineScope(dispatcher).launch {
                        mLrclibApiHelper.searchLyricsForTrack(
                            query = track,
                            dispatcher = dispatcher,
                            onResult = { onSuccess(it.first()) },
                            onError = onError
                        )
                    }
                } else {
                    Log.e(TAG, "getLyrics: no results, fallback not possible")
                    onError(error)
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
        notify(mContext.getString(R.string.notification_manual_search_suggestion))
    }

    private fun notify(content: String) {
        val titleString = mContext.getString(R.string.request_handling_notification_title)
        if (::mTrack.isInitialized) {
            val (title, subText) = Pair(
                "$titleString - ${mTrack.trackName}",
                "${mContext.getString(R.string.track)}: ${mTrack.trackName}"
            )
            mNotificationHelper.makeNotification(title, content, subText, mTrack)
        } else {
            val (title, subText) = Pair(titleString, null)
            mNotificationHelper.makeNotification(title, content, subText)
        }
    }

    companion object {
        private const val TAG = "LyricsRequestReceiver"
        const val MANUAL_SEARCH_ACTION =
            "io.github.abhishekabhi789.lyricsforpoweramp.MANUAL_SEARCH_ACTION"
        const val POWERAMP_LYRICS_REQUEST_WAIT_TIMEOUT = 30_000L
    }
}
