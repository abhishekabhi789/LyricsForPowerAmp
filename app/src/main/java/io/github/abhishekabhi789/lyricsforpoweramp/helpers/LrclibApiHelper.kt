package io.github.abhishekabhi789.lyricsforpoweramp.helpers

import android.util.Log
import com.google.gson.Gson
import io.github.abhishekabhi789.lyricsforpoweramp.BuildConfig
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.ui.main.GITHUB_REPO_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder

/**Helper to interacts with LRCLIB*/
object LrclibApiHelper {
    private val TAG = javaClass.simpleName
    private const val API_BASE_URL = "https://lrclib.net/api/"
    private const val CONNECTION_TIMEOUT = 5000
    private const val READ_TIMEOUT = 30000

    private suspend fun makeApiRequest(
        params: String,
        onResult: (String) -> Unit,
        onFail: (String) -> Unit
    ) {
        Log.d(TAG, "makeApiRequest: $params")
        try {
            withContext(Dispatchers.IO) {
                val url = API_BASE_URL + params
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = CONNECTION_TIMEOUT
                connection.readTimeout = READ_TIMEOUT
                connection.requestMethod = "GET"
                try {
                    connection.setRequestProperty(
                        "User-Agent",
                        buildString {
                            append(BuildConfig.APPLICATION_ID)
                            append("-")
                            append(BuildConfig.BUILD_TYPE)
                            append(" ")
                            append(BuildConfig.VERSION_NAME)
                            append(" ")
                            append(GITHUB_REPO_URL)
                        }
                    )
                    connection.setRequestProperty("Content-Type", "application/json")
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "makeApiRequest: already connected", e)
                }
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    onResult(response.toString())
                } else {
                    Log.e(
                        TAG,
                        "makeApiRequest: Network Request Failed, $responseCode ${connection.responseMessage}"
                    )
                    onFail("Request Failed, HTTP $responseCode: ${connection.responseMessage} ")
                }
            }
        } catch (e: MalformedURLException) {
            Log.e(TAG, "Malformed URL: $params", e)
            onFail("Request Failed, Malformed URL: $params")
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception during network request: ${e.message}", e)
            onFail("Request Failed, Network error.")
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "makeApiRequest: timeout exception", e)
            onFail("Request time out")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected Exception during network request: ${e.message}", e)
            onFail("Request Failed, Exception ${e.message}")
        }
    }

    /**Performs a search to get the best matching lyrics for the track.
     * @see <a href="https://lrclib.net/docs#:~:text=tranxuanthang/lrcget).-,Get%20lyrics%20with%20a%20track%27s%20signature,-GET">
     *     LRCLIB#Get lyrics with a track's signature</a>*/
    suspend fun getLyricsForTracks(
        track: Track,
        onResult: (Lyrics) -> Unit,
        onFail: (String) -> Unit
    ) {
        val requestParams = buildString {
            append("get?")
            if (!track.trackName.isNullOrEmpty()) append("track_name=${encode(track.trackName!!)}")
            if (!track.artistName.isNullOrEmpty()) append("&artist_name=${encode(track.artistName!!)}")
            if (!track.albumName.isNullOrEmpty()) append("&album_name=${encode(track.albumName!!)}")
            if (track.duration != null && track.duration!! > 0) append("&duration=${track.duration}")
        }
        makeApiRequest(
            requestParams,
            onResult = { response ->
                val result = Gson().fromJson(response, Lyrics::class.java)
                Log.d(TAG, "getLyricsForTracks: search result ${result.trackName}")
                onResult(result)
            },
            onFail = { error -> onFail(error) })
    }

    /** Performs search for the given input.
     * @param query either the query string or a valid [Track]
     * @see <a href="https://lrclib.net/docs#:~:text=s%20example%20response.-,Search%20for%20lyrics%20records,-GET">
     *     LRCLIB#Search for lyrics records</a>*/
    suspend fun searchLyricsForTrack(
        query: Any,
        onResult: (List<Lyrics>) -> Unit,
        onError: (String) -> Unit
    ) {
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
                val error = "Invalid query type: $query"
                Log.e(TAG, error)
                error
            }
        }
        Log.d(TAG, "searchLyricsForTrack: $requestParams")
        makeApiRequest(
            requestParams,
            onResult = { results ->
                val parsedResponse = parseSearchResponse(results)
                if (!parsedResponse.isNullOrEmpty()) {
                    Log.d(TAG, "searchLyricsForTrack: found ${parsedResponse.size} results")
                    onResult(parsedResponse)
                } else {
                    Log.e(TAG, "searchLyricsForTrack: no result found $results")
                    onError("No result found")
                }
            },
            onFail = { error -> onError(error) })
    }

    /**Converts JSON response into List of [Lyrics].
     * Ensures either plain or synced lyrics present in each list items.*/
    private fun parseSearchResponse(searchResponse: String?): List<Lyrics>? {
        val results: Array<Lyrics>? = Gson().fromJson(searchResponse, Array<Lyrics>::class.java)
        return results?.filter { it.plainLyrics != null || it.syncedLyrics != null }?.toList()
    }

    private fun encode(text: String) = URLEncoder.encode(text, "UTF-8")

}
