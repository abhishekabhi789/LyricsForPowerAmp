package abhi.lyricsforpoweramp

import abhi.lyricsforpoweramp.model.Track
import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPIHelper
import java.io.File

/**
 * Contains functions helping to send and receive data with PowerAmp
 */
object PowerAmpIntentUtils {

    private val TAG = javaClass.simpleName

    /**
     * Makes a [Track] for the intent passed by PowerAmp
     * @param intent received from PowerAmp
     * @return an instance of [Track]
     */
    fun makeTrack(intent: Intent): Track {
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
        return Track(processTrackName(title!!), artist, album, duration, null)
    }

    private fun processTrackName(trackName: String): String {
        var name = File(trackName).nameWithoutExtension
        //removing unnecessary words from title
        name = name.replace(Regex("ost", RegexOption.IGNORE_CASE), "")
        name = name.replace(Regex("\\d+[-_.\\s]?kbps", RegexOption.IGNORE_CASE), "")
        name = name.trim()
        return name
    }

    /***
     * Sends the prepared lyric data to PowerAmp.
     * @param context required to send intent
     * @param realId to identify the track to attach the lyric
     * @param lyrics lyrics to be returned
     * @return a [Boolean] representing success status
     */
    fun sendLyricResponse(
        context: Context?,
        realId: Long,
        lyrics: String?
    ): Boolean {
        val intent = Intent(PowerampAPI.Lyrics.ACTION_UPDATE_LYRICS)
        val infoLine: String? =
            if ((realId and 0x1L) == 0L) "Lyrics powered by Lyrics for PowerAmp" else null
        intent.putExtra(PowerampAPI.EXTRA_ID, realId)
        intent.putExtra(PowerampAPI.Lyrics.EXTRA_LYRICS, lyrics)
        intent.putExtra(PowerampAPI.Lyrics.EXTRA_INFO_LINE, infoLine)
        try {
            PowerampAPIHelper.sendPAIntent(context, intent)
            Log.i(TAG, "sendLyricResponse: Success")
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }
}