package io.github.abhishekabhi789.lyricsforpoweramp

import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPIHelper
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference.FILTER
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
    fun makeTrack(context: Context, intent: Intent): Track {
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
        val duration: Int? = (durationMs / 1000).let { if (it == 0) null else it }
        return Track(
            trackName = processField(context, FILTER.TITLE_FILTER, title),
            artistName = processField(context, FILTER.ARTISTS_FILTER, artist),
            albumName = processField(context, FILTER.ALBUM_FILTER, album),
            duration = duration,
            realId = realId,
            lyrics = null
        )
    }

    /**
     * Corresponding filter words will be removed from the value.
     */
    private fun processField(context: Context, field: FILTER, value: String?): String? {
        //sometimes filename will be in the title, this nameWithoutExtension works somehow even with non filename value
        val fieldValue = if (field == FILTER.TITLE_FILTER && !value.isNullOrEmpty())
            File(value).nameWithoutExtension else value
        val filter = AppPreference.getFilter(context, field)?.lines()
        return filter?.fold(fieldValue) { cleanedValue, filterItem ->
            cleanedValue?.replace(Regex(filterItem, RegexOption.IGNORE_CASE), "")
        } ?: fieldValue
    }

    /**
     * Sends the prepared lyric data to PowerAmp.
     * @param context required to send intent
     * @param realId to identify the track to attach the lyric
     * @param lyrics lyrics to be returned
     * @return [Boolean] representing success status
     */
    fun sendLyricResponse(
        context: Context,
        realId: Long,
        lyrics: Lyrics?,
    ): Boolean {
        val infoLine = makeInfoLine(context, lyrics)
        val intent = Intent(PowerampAPI.Lyrics.ACTION_UPDATE_LYRICS).apply {
            putExtra(PowerampAPI.EXTRA_ID, realId)
            putExtra(PowerampAPI.Lyrics.EXTRA_LYRICS, lyrics?.syncedLyrics ?: lyrics?.plainLyrics)
            putExtra(PowerampAPI.Lyrics.EXTRA_INFO_LINE, infoLine)
        }

        try {
            val status = PowerampAPIHelper.sendPAIntent(context, intent)
            Log.i(TAG, "sendLyricResponse: Success $status")
            return status
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    private fun makeInfoLine(context: Context, lyrics: Lyrics?): String {
        return buildString {
            if (!lyrics?.trackName.isNullOrEmpty()) {
                appendLine("${context.getString(R.string.input_track_title_label)}: ${lyrics?.trackName}")
                appendLine("${context.getString(R.string.input_track_artists_label)}: ${lyrics?.artistName}")
                appendLine("${context.getString(R.string.input_track_album_label)}: ${lyrics?.albumName}")
                appendLine()
            }
            appendLine(context.getString(R.string.response_footer_text))
        }
    }
}
