package abhi.lyricsforpoweramp

import abhi.lyricsforpoweramp.model.Lyric
import abhi.lyricsforpoweramp.model.Track
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder

/**Helper to interacts with LRCLIB*/
class LyricsApiHelper {
    private val TAG = javaClass.simpleName
    private val API_BASE_URL = "https://lrclib.net/api/"

    private suspend fun makeApiRequest(params: String): String? {
        Log.d(TAG, "makeApiRequest: $params")
        return try {
            withContext(Dispatchers.IO) {
                val url = API_BASE_URL + params
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")
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
        } catch (e: MalformedURLException) {
            Log.e(TAG, "Malformed URL: $params", e)
            null
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception during network request: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected Exception during network request: ${e.message}", e)
            null
        }
    }

    /**Performs a search to get the best matching lyrics for the track.
     * @see <a href="https://lrclib.net/docs#:~:text=tranxuanthang/lrcget).-,Get%20lyrics%20with%20a%20track%27s%20signature,-GET">
     *     LRCLIB#Get lyrics with a track's signature</a>*/
    suspend fun getTopMatchingLyrics(track: Track): String? {
        val requestParams = buildString {
            append("get?")
            if (!track.trackName.isNullOrEmpty()) append("track_name=${encode(track.trackName!!)}")
            if (!track.artistName.isNullOrEmpty()) append("&artist_name=${encode(track.artistName!!)}")
            if (!track.albumName.isNullOrEmpty()) append("&album_name=${encode(track.albumName!!)}")
            if (track.duration != null && track.duration!! > 0) append("&duration=${track.duration}")
        }
        val response = makeApiRequest(requestParams)
        return if (!response.isNullOrEmpty()) {
            val result = Gson().fromJson(response, Lyric::class.java)
            result.syncedLyrics ?: result.plainLyrics
        } else null
    }

    /** Performs search for the given input.
     * @see <a href="https://lrclib.net/docs#:~:text=s%20example%20response.-,Search%20for%20lyrics%20records,-GET">
     *     LRCLIB#Search for lyrics records</a>*/
    suspend fun getLyricsForTrack(query: Any): List<Lyric>? {
        val requestParams: String = when (query) {
            is String -> buildString { append("search?q=${encode(query)}") }
            is Track ->
                buildString {
                    append("search?")
                    append("track_name=${encode(query.trackName!!)}") //This can't be empty
                    if (!query.artistName.isNullOrEmpty()) append("&artist_name=${encode(query.artistName!!)}")
                    if (!query.albumName.isNullOrEmpty()) append("&album_name=${encode(query.albumName!!)}")
                }
            else -> {
                Log.e(TAG, "Invalid query type: $query")
                return null
            }
        }
        Log.d(TAG, "getLyricsForTrack: $requestParams")
        val searchResponse = makeApiRequest(requestParams)
        if (searchResponse.isNullOrEmpty()) {
            Log.e(TAG, "searchTrackInfo: No search result for $query")
            return null
        }
        val validLyrics = parseSearchResponse(searchResponse)
        return if (validLyrics.isNullOrEmpty()) {
            Log.e(TAG, "searchTrackInfo: failed to parseJson $searchResponse")
            null
        } else validLyrics
    }

    /**Converts JSON response into List of [Lyric].
     * Ensures either plain or synced lyrics present in each list items.*/
    private fun parseSearchResponse(searchResponse: String?): List<Lyric>? {
        val results: Array<Lyric>? = Gson().fromJson(searchResponse, Array<Lyric>::class.java)
        return results?.filter { it.plainLyrics != null || it.syncedLyrics != null }
            ?.toList()
    }


    private fun encode(text: String): String {
        return URLEncoder.encode(text, "UTF-8")
    }
}