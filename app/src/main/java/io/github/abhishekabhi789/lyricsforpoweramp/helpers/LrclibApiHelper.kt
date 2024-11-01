package io.github.abhishekabhi789.lyricsforpoweramp.helpers

import android.util.Log
import com.google.gson.Gson
import io.github.abhishekabhi789.lyricsforpoweramp.BuildConfig
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.ui.main.GITHUB_REPO_URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URLEncoder
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

/**Helper to interacts with LRCLIB*/
class LrclibApiHelper(private val client: OkHttpClient) {
    private val TAG = javaClass.simpleName

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
            val httpUrl = (API_BASE_URL + params).toHttpUrlOrNull()
            if (httpUrl != null) {
                val request = Request.Builder().apply {
                    url(httpUrl)
                    header(
                        "User-Agent",
                        buildString {
                            append(BuildConfig.APPLICATION_ID)
                            append("-${BuildConfig.BUILD_TYPE}")
                            append(" ${BuildConfig.VERSION_NAME}")
                            append(" $GITHUB_REPO_URL")
                        })
                    header("Content-Type", "application/json")
                    get()
                }.build()
                val result = withContext(dispatcher) {
                    suspendCancellableCoroutine { continuation ->
                        val call = client.newCall(request)
                        continuation.invokeOnCancellation {
                            Log.i(TAG, "makeApiRequest: continuation cancel invoked")
                            call.cancel()
                        }
                        call.enqueue(object : okhttp3.Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                if (call.isCanceled()) {
                                    Log.i(TAG, "onFailure: call cancelled")
                                    continuation.resume(ApiResult.Error("Cancelled"))
                                } else {
                                    Log.e(TAG, "onFailure: failed, ${e.message}")
                                    continuation.resume(ApiResult.Error("Request Failed, Network error."))
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                try {
                                    when (response.code) {
                                        HttpURLConnection.HTTP_OK -> {
                                            response.body?.let { responseBody ->
                                                val result = responseBody.string()
                                                continuation.resume(ApiResult.Success(result))
                                            }
                                                ?: continuation.resume(ApiResult.Error("Empty response"))
                                        }

                                        HttpURLConnection.HTTP_NOT_FOUND -> {
                                            val errorMsg = response.message
                                            Log.i(TAG, "makeApiRequest: no result $errorMsg")
                                            continuation.resume(ApiResult.Error(errorMsg))
                                        }

                                        else -> {
                                            val errorMsg =
                                                "Request Failed, HTTP ${response.code}: ${response.message}"
                                            Log.e(TAG, "makeApiRequest: $errorMsg")
                                            continuation.resume(ApiResult.Error(errorMsg))
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "makeApiRequest: Error processing response", e)
                                    continuation.resume(ApiResult.Error("Error processing response: ${e.message}"))
                                } finally {
                                    response.close()
                                }
                            }
                        })
                    }
                }
                onComplete(result)
            } else {
                Log.e(TAG, "makeApiRequest: failed to prepare url, prams: $params")
                onComplete(ApiResult.Error("failed to make request"))
            }
        } catch (e: CancellationException) {
            Log.e(TAG, "CancellationException", e)
            onComplete(ApiResult.Error("Cancelled"))
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "IllegalArgumentException", e)
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
                is ApiResult.Error -> {
                    Log.e(TAG, "getLyricsForTracks: error ${output.message}")
                    onError(output.message)
                }

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

    companion object {
        private const val API_BASE_URL = "https://lrclib.net/api/"
    }
}
