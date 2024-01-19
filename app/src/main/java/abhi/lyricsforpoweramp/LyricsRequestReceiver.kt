package abhi.lyricsforpoweramp

import abhi.lyricsforpoweramp.PowerAmpIntentUtils.sendLyricResponse
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

        val lyrics: String?
        runBlocking {
            lyrics = LyricsApiHelper().getTopMatchingLyrics(track)
        }
        sendLyricResponse(context, realId, lyrics)
    }
}