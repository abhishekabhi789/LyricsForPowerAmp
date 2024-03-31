package io.github.abhishekabhi789.lyricsforpoweramp.utils

import android.content.Context
import android.content.SharedPreferences

object AppPreference {
    private const val FILTER_PREF_NAME = "filter_preference"

    private fun getSharedPreference(context: Context): SharedPreferences? {
        return context.getSharedPreferences(FILTER_PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getFilter(context: Context, filter: FILTER): String? {
        val sharedPreferences = getSharedPreference(context)
        return sharedPreferences?.getString(filter.key, null)
    }

    fun setFilter(context: Context, filter: FILTER, value: String) {
        val sharedPreferences = getSharedPreference(context)
        sharedPreferences?.edit()?.putString(filter.key, value)?.apply()
    }

    enum class FILTER(val key: String, val label: String) {
        TITLE_FILTER("title_filter", "Title Filter"),
        ARTISTS_FILTER("artists_filter", "Artists Filter"),
        ALBUM_FILTER("album_filter", "Album Filter"),
    }
}