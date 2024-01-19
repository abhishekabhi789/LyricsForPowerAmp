package abhi.lyricsforpoweramp

import abhi.lyricsforpoweramp.model.Lyric
import abhi.lyricsforpoweramp.model.Track
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LyricsApiHelper {
    private val TAG = javaClass.simpleName
    private val API_BASE_URL = "https://lrclib.net/api/"

    private suspend fun makeApiRequest(params: String): String? {
        return try {
            withContext(Dispatchers.IO) {
                val url = API_BASE_URL + params
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    response.toString()
                } else {
                    Log.e(TAG, "makeGetRequest: Network Request Failed")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during network request: ${e.message}", e)
            null
        }
    }

    suspend fun getTopMatchingLyrics(track: Track): String? {
        return getLyricsForTrack(track)?.get(0)?.syncedLyrics
    }

    suspend fun getLyricsForTrack(track: Track): Array<Lyric>? {
        val requestParams = buildString {
            append("search?")
            append("q=${encode(track.trackName)}")
//            Note: metadata won't be correct all the time. That's why below params are disabled
//            if (track.artistName != null) append("&artist_name=${encode(track.artistName)}")
//            if (track.albumName != null) append("&album_name=${encode(track.albumName)}")
//            if (track.duration != null && track.duration > 0) append("&duration=${track.duration}")
        }
        val searchResponse = makeApiRequest(requestParams)
        if (searchResponse.isNullOrEmpty()) {
            Log.e(TAG, "searchTrackInfo: No search result for $track")
            return null
        }
        val matchingTracks = parseSearchResponse(searchResponse)
        return if (matchingTracks.isNullOrEmpty()) {
            Log.e(TAG, "searchTrackInfo: failed to parseJson $searchResponse")
            null
        } else matchingTracks
    }


    private fun parseSearchResponse(searchResponse: String?): Array<Lyric>? {
        val results: Array<Lyric>? = Gson().fromJson(searchResponse, Array<Lyric>::class.java)
        return results?.filter { it.plainLyrics != null || it.syncedLyrics !=null  }?.toTypedArray<Lyric>()
    }

    private fun encode(text: String): String {
        return URLEncoder.encode(text)
    }
}