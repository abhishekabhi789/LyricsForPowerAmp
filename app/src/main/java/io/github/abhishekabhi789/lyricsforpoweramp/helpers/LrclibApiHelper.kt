package io.github.abhishekabhi789.lyricsforpoweramp.helpers

import android.util.Log
import com.google.gson.Gson
import io.github.abhishekabhi789.lyricsforpoweramp.BuildConfig
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.ui.main.GITHUB_REPO_URL
import kotlinx.coroutines.CoroutineDispatcher
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

    sealed class ApiResult {
        data class Success(val data: String) : ApiResult()
        data class Error(val message: String) : ApiResult()
    }

    private suspend fun makeApiRequest(
        params: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        onComplete: (ApiResult) -> Unit
    ) {
        Log.d(TAG, "makeApiRequest: $params")
        try {
            withContext(dispatcher) {
                val url = API_BASE_URL + params
                val connection = (URL(url).openConnection() as? HttpURLConnection)?.apply {
                    connectTimeout = CONNECTION_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    requestMethod = "GET"
                    try {
                        setRequestProperty(
                            "User-Agent",
                            buildString {
                                append(BuildConfig.APPLICATION_ID)
                                append("-${BuildConfig.BUILD_TYPE}")
                                append(" ${BuildConfig.VERSION_NAME}")
                                append(" $GITHUB_REPO_URL")
                            }
                        )
                        setRequestProperty("Content-Type", "application/json")
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "makeApiRequest: already connected", e)
                    }
                }
                when (val responseCode = connection?.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                            val response = StringBuilder()
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                response.append(line)
                            }
                            onComplete(ApiResult.Success(response.toString()))
                        }
                    }

                    HttpURLConnection.HTTP_NOT_FOUND -> {
                        val errorMsg = connection.responseMessage
                        Log.i(TAG, "makeApiRequest: no result $errorMsg")
                        onComplete(ApiResult.Error(errorMsg))
                    }

                    else -> {
                        val errorMsg = connection?.responseMessage
                        Log.e(TAG, "makeApiRequest: Request Failed, $responseCode: $errorMsg")
                        onComplete(ApiResult.Error("Request Failed, HTTP $responseCode: $errorMsg"))
                    }
                }
            }
        } catch (e: MalformedURLException) {
            Log.e(TAG, "Malformed URL: $params", e)
            onComplete(ApiResult.Error("Request Failed, Malformed URL: $params"))
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception during network request: ${e.message}", e)
            onComplete(ApiResult.Error("Request Failed, Network error."))
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "makeApiRequest: timeout exception", e)
            onComplete(ApiResult.Error("Request time out"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected Exception during network request: ${e.message}", e)
            onComplete(ApiResult.Error("Request Failed, Exception ${e.message}"))
        }
    }

    /**Performs a search to get the best matching lyrics for the track.
     * @see <a href="https://lrclib.net/docs#:~:text=tranxuanthang/lrcget).-,Get%20lyrics%20with%20a%20track%27s%20signature,-GET">
     *     LRCLIB#Get lyrics with a track's signature</a>*/
    suspend fun getLyricsForTracks(
        track: Track,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        onResult: (Lyrics) -> Unit,
        onError: (String) -> Unit
    ) {
        if (track.trackName.isNullOrEmpty()) {
            onError("Track name cannot be empty")
            return
        }
        val requestParams = listOfNotNull(
            "track_name=${encode(track.trackName!!)}",
            track.artistName?.let { "artist_name=${encode(it)}" },
            track.albumName?.let { "album_name=${encode(it)}" },
            track.duration?.takeIf { it > 0 }?.let { "duration=$it" }
        ).joinToString("&", prefix = "get?")
        makeApiRequest(requestParams, dispatcher) { output ->
            when (output) {
                is ApiResult.Error -> onError(output.message)
                is ApiResult.Success -> {
                    val result = Gson().fromJson(output.data, Lyrics::class.java)
                    Log.d(TAG, "getLyricsForTracks: search result ${result.trackName}")
                    onResult(result)
                }
            }
        }
    }

    /** Performs search for the given input.
     * @param query either the query string or a valid [Track]
     * @see <a href="https://lrclib.net/docs#:~:text=s%20example%20response.-,Search%20for%20lyrics%20records,-GET">
     *     LRCLIB#Search for lyrics records</a>*/
    suspend fun searchLyricsForTrack(
        query: Any,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        onResult: (List<Lyrics>) -> Unit,
        onError: (String) -> Unit
    ) {
        val requestParams: String = when (query) {
            is String -> "search?q=${encode(query)}"

            is Track -> {
                if (query.trackName.isNullOrEmpty()) {
                    onError("Track name cannot be empty")
                    return
                }
                listOfNotNull(
                    "track_name=${encode(query.trackName!!)}",
                    query.artistName?.let { "artist_name=${encode(it)}" },
                    query.albumName?.let { "album_name=${encode(it)}" }
                ).joinToString("&", prefix = "search?")
            }

            else -> {
                val error = "Invalid query type"
                Log.e(TAG, "$error $query")
                onError(error)
                return
            }
        }
        Log.d(TAG, "searchLyricsForTrack: $requestParams")
        makeApiRequest(requestParams, dispatcher) { results ->
            when (results) {
                is ApiResult.Success -> {
                    val parsedResponse = parseSearchResponse(results.data)
                    if (!parsedResponse.isNullOrEmpty()) {
                        Log.d(TAG, "searchLyricsForTrack: found ${parsedResponse.size} results")
                        onResult(parsedResponse)
                    } else {
                        Log.e(TAG, "searchLyricsForTrack: no result found $results")
                        onError("No result found")
                    }
                }

                is ApiResult.Error -> onError(results.message)
            }
        }
    }

    /**Converts JSON response into List of [Lyrics].
     * Ensures either plain or synced lyrics present in each list items.*/
    private fun parseSearchResponse(searchResponse: String?): List<Lyrics>? {
        val results: Array<Lyrics>? = Gson().fromJson(searchResponse, Array<Lyrics>::class.java)
        return results?.filter { it.plainLyrics != null || it.syncedLyrics != null }?.toList()
    }

    private fun encode(text: String) = URLEncoder.encode(text, "UTF-8")

}
