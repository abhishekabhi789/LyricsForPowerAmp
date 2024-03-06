package io.github.abhishekabhi789.lyricsforpoweramp

import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPIHelper
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
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
        val name = File(trackName).nameWithoutExtension.apply {
            //removing unnecessary words from title
            replace(Regex("ost", RegexOption.IGNORE_CASE), "")
            replace(Regex("\\d+[-_.\\s]?kbps", RegexOption.IGNORE_CASE), "")
            replace("-", " ")
            trim()
        }
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
        context: Context,
        realId: Long,
        lyrics: String?
    ): Boolean {
        val infoLine: String? =
            if ((realId and 0x1L) == 0L) context.getString(R.string.response_footer_text) else null
        val intent = Intent(PowerampAPI.Lyrics.ACTION_UPDATE_LYRICS).apply {
            putExtra(PowerampAPI.EXTRA_ID, realId)
            putExtra(PowerampAPI.Lyrics.EXTRA_LYRICS, lyrics)
            putExtra(PowerampAPI.Lyrics.EXTRA_INFO_LINE, infoLine)
        }

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