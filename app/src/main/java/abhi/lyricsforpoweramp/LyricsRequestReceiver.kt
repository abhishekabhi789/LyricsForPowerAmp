package abhi.lyricsforpoweramp

import abhi.lyricsforpoweramp.PowerAmpIntentUtils.sendLyricResponse
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import kotlinx.coroutines.runBlocking

class LyricsRequestReceiver : BroadcastReceiver() {
    private val TAG = javaClass.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            PowerampAPI.Lyrics.ACTION_NEED_LYRICS -> handleLyricsRequest(context, intent)
        }
    }

    private fun handleLyricsRequest(context: Context?, intent: Intent) {
        val realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, PowerampAPI.NO_ID)
        val track = PowerAmpIntentUtils.makeTrack(intent)
        Log.i(TAG, "handleLyricsRequest: request for $track")
        runBlocking {
            LyricsApiHelper.getTopMatchingLyrics(
                track,
                onResult = { lyrics ->
                    //if lyrics null; sending a fake lyrics, so that user can open the lyrics search activity for the track.
                    lyrics ?: """Couldn't find lyrics for this track.\n
                            | Try adjusting the search parameters.""".trimMargin()
                    sendLyricResponse(context, realId, lyrics)
                },
                onFail = {/*Do Nothing*/ })
        }

    }
}