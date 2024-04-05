package io.github.abhishekabhi789.lyricsforpoweramp.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import io.github.abhishekabhi789.lyricsforpoweramp.LyricViewModel
import io.github.abhishekabhi789.lyricsforpoweramp.R

object AppPreference {
    private const val FILTER_PREF_NAME = "filter_preference"
    private const val UI_PREF_NAME = "ui_preference"
    private const val UI_THEME_KEY = "app_theme"
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

    fun setTheme(context: Context, theme: AppTheme, viewModel: LyricViewModel) {
        val sharedPreferences = getSharedPreference(context, UI_PREF_NAME)
        sharedPreferences?.edit()?.putString(UI_THEME_KEY, theme.name)?.apply()
        viewModel.updateTheme(theme)
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
        Auto(R.string.theme_auto_label),
        Light(R.string.theme_light_label),
        Dark(R.string.theme_dark_label)
    }

    enum class FILTER(val key: String, @StringRes val label: Int) {
        TITLE_FILTER("title_filter", R.string.filter_title_label),
        ARTISTS_FILTER("artists_filter", R.string.filter_artists_label),
        ALBUM_FILTER("album_filter", R.string.filter_album_label),
    }
}