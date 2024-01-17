package abhi.lyricsforpoweramp

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LyricsHelper {
    private val TAG = javaClass.simpleName
    private val API_BASE_URL = "https://lrclib.net/api/"

    private suspend fun makeApiRequest(params: String): String? {
        Log.d(TAG, "makeApiRequest: params: $params")
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

    suspend fun getLyrics(
        trackName: String,
        artistName: String?,
        albumName: String?,
        duration: Int?
    ): String? {
        val track = Track(trackName, artistName, albumName, duration, null)
        return getLyricsForTrack(track)?.syncedLyrics
    }

    private suspend fun getLyricsForTrack(track: Track): Track? {
        val requestParams = buildString {
            append("search?")
            append("q=${encode(processTrackName(track.trackName))}")
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
        val matchingTrack = parseSearchResponse(searchResponse)
        return if (matchingTrack == null) {
            Log.e(TAG, "searchTrackInfo: failed to parseJson $searchResponse")
            null
        } else matchingTrack
    }


    private fun parseSearchResponse(searchResponse: String?): Track? {
        val results: Array<Track> = Gson().fromJson(searchResponse, Array<Track>::class.java)
        return if (results.isNotEmpty()) results[0] else null
    }

    private fun encode(text: String): String {
        return URLEncoder.encode(text)
    }

    private fun processTrackName(trackName: String): String {
        var name = File(trackName).nameWithoutExtension
        //removing unnecessary words from title
        name = name.replace("OST", "")
        name = name.trim()
        return name
    }
}