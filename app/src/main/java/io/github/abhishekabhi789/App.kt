package io.github.abhishekabhi789

import android.app.Application
import io.github.abhishekabhi789.lyricsforpoweramp.helpers.LrclibApiHelper
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class App : Application() {
    lateinit var lrclibApiHelper: LrclibApiHelper
        private set

    override fun onCreate() {
        super.onCreate()
        val client: OkHttpClient = OkHttpClient.Builder().apply {
            connectTimeout(LrclibApiHelper.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            readTimeout(LrclibApiHelper.READ_TIMEOUT, TimeUnit.MILLISECONDS)
        }.build()
        lrclibApiHelper = LrclibApiHelper(client)
    }
}
