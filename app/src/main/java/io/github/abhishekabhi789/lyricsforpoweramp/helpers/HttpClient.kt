package io.github.abhishekabhi789.lyricsforpoweramp.helpers


import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClient {
    private const val CONNECTION_TIMEOUT = 10_000L
    private const val READ_TIMEOUT = 30_000L
    val okHttpClient: OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
        readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
    }.build()
}
