package io.github.abhishekabhi789.lyricsforpoweramp.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.model.LyricsType

object AppPreference {
    private const val FILTER_PREF_NAME = "filter_preference"
    private const val UI_PREF_NAME = "ui_preference"
    private const val OTHER_PREF = "other_preference"
    private const val UI_THEME_KEY = "app_theme"
    private const val SEARCH_IF_GET_FAILED = "perform_search_if_get_failed"
    private const val SHOW_LYRICS_REQUEST_NOTIFICATION = "lyrics_requests_show_notification"
    private const val OVERWRITE_NOTIFICATION = "lyrics_requests_overwrite_existing_notification"
    private const val PREFERRED_LYRICS_TYPE = "preferred_lyrics_type"

    private fun getSharedPreference(context: Context, prefName: String): SharedPreferences? {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    fun getFilter(context: Context, filter: FILTER): String? {
        val sharedPreferences = getSharedPreference(context, FILTER_PREF_NAME)
        return sharedPreferences?.getString(filter.key, null)
    }

    fun setFilter(context: Context, filter: FILTER, value: String?) {
        val sharedPreferences = getSharedPreference(context, FILTER_PREF_NAME)
        sharedPreferences?.edit()?.putString(filter.key, value)?.apply()
    }

    fun getTheme(context: Context): AppTheme {
        val sharedPreferences = getSharedPreference(context, UI_PREF_NAME)
        val defaultTheme =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) AppTheme.Auto else AppTheme.Light
        val preferredTheme = sharedPreferences?.getString(UI_THEME_KEY, defaultTheme.name)
        return AppTheme.valueOf(preferredTheme ?: defaultTheme.name)
    }

    fun getThemes(): List<AppTheme> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) AppTheme.entries.toList()
        else listOf(AppTheme.Light, AppTheme.Dark)
    }

    fun setTheme(context: Context, theme: AppTheme) {
        val sharedPreferences = getSharedPreference(context, UI_PREF_NAME)
        sharedPreferences?.edit()?.putString(UI_THEME_KEY, theme.name)?.apply()
    }

    fun getSearchIfGetFailed(context: Context): Boolean {
        val sharedPreferences = getSharedPreference(context, OTHER_PREF)
        return sharedPreferences?.getBoolean(SEARCH_IF_GET_FAILED, false) ?: false
    }

    fun setSearchIfGetFailed(context: Context, choice: Boolean) {
        val sharedPreferences = getSharedPreference(context, OTHER_PREF)
        sharedPreferences?.edit()?.putBoolean(SEARCH_IF_GET_FAILED, choice)?.apply()
    }

    fun getShowNotification(context: Context): Boolean {
        val sharedPreferences = getSharedPreference(context, OTHER_PREF)
        return sharedPreferences?.getBoolean(SHOW_LYRICS_REQUEST_NOTIFICATION, true) ?: true
    }

    fun setShowNotification(context: Context, choice: Boolean) {
        val sharedPreferences = getSharedPreference(context, OTHER_PREF)
        sharedPreferences?.edit()?.putBoolean(SHOW_LYRICS_REQUEST_NOTIFICATION, choice)?.apply()
    }

    fun getOverwriteNotification(context: Context): Boolean {
        val sharedPreferences = getSharedPreference(context, OTHER_PREF)
        return sharedPreferences?.getBoolean(OVERWRITE_NOTIFICATION, false) ?: false
    }

    fun setOverwriteNotification(context: Context, choice: Boolean) {
        val sharedPreferences = getSharedPreference(context, OTHER_PREF)
        sharedPreferences?.edit()?.putBoolean(OVERWRITE_NOTIFICATION, choice)?.apply()
    }

    fun getPreferredLyricsType(context: Context): LyricsType {
        val sharedPreferences = getSharedPreference(context, OTHER_PREF)
        val defaultType = LyricsType.SYNCED
        val preferredType =
            sharedPreferences?.getString(PREFERRED_LYRICS_TYPE, defaultType.name)
        return LyricsType.valueOf(preferredType ?: defaultType.name)
    }

    fun setPreferredLyricsType(context: Context, type: LyricsType) {
        val sharedPreferences = getSharedPreference(context, OTHER_PREF)
        sharedPreferences?.edit()?.putString(PREFERRED_LYRICS_TYPE, type.name)?.apply()
    }

    @Composable
    fun isDarkTheme(theme: AppTheme): Boolean {
        return when (theme) {
            AppTheme.Dark -> true
            AppTheme.Light -> false
            AppTheme.Auto -> isSystemInDarkTheme()
        }
    }

    enum class AppTheme(@StringRes val label: Int) {
        Auto(R.string.settings_theme_auto_label),
        Light(R.string.settings_theme_light_label),
        Dark(R.string.settings_theme_dark_label)
    }

    enum class FILTER(val key: String, @StringRes val label: Int) {
        TITLE_FILTER("title_filter", R.string.settings_filter_title_label),
        ARTISTS_FILTER("artists_filter", R.string.settings_filter_artists_label),
        ALBUM_FILTER("album_filter", R.string.settings_filter_album_label),
    }
}
