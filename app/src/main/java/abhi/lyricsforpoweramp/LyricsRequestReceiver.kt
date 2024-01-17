package abhi.lyricsforpoweramp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPIHelper
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
        val title = intent.getStringExtra(PowerampAPI.Track.TITLE)
        if (realId == PowerampAPI.NO_ID || title.isNullOrEmpty()) {
            Log.e(
                TAG,
                buildString {
                    append("onReceive: Failed to receive details.")
                    append(" | realId: $realId")
                    append(" | title: $title")
                },
            )
        }
        Log.i(TAG, "onReceive: request received for $title")
        val album = intent.getStringExtra(PowerampAPI.Track.ALBUM)
        val artist = intent.getStringExtra(PowerampAPI.Track.ARTIST)
        val durationMs = intent.getIntExtra(PowerampAPI.Track.DURATION_MS, 0)
        val duration = durationMs / 1000
        val infoLine: String? =
            if ((realId and 0x1L) == 0L) "Lyrics powered by LyricsForPowerAmp (realId=$realId)" else null
        val lyrics: String?
        runBlocking {
            lyrics = LyricsHelper().getLyrics(title!!, artist, album, duration)
        }
        if (lyrics != null) sendLyricResponse(context, realId, lyrics, infoLine)
    }

    private fun sendLyricResponse(
        context: Context?,
        realId: Long,
        lyrics: String,
        infoLine: String?
    ): Boolean {
        val intent = Intent(PowerampAPI.Lyrics.ACTION_UPDATE_LYRICS)
        intent.putExtra(PowerampAPI.EXTRA_ID, realId)
        intent.putExtra(PowerampAPI.Lyrics.EXTRA_LYRICS, lyrics)
        intent.putExtra(PowerampAPI.Lyrics.EXTRA_INFO_LINE, infoLine)
        try {
            PowerampAPIHelper.sendPAIntent(context, intent)
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

}