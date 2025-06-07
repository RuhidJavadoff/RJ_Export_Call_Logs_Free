package com.ruhidjavadoff.rjexportcalllogsfree

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"

    // Mümkün tema rejimləri üçün sabitələr
    const val LIGHT_MODE = AppCompatDelegate.MODE_NIGHT_NO
    const val DARK_MODE = AppCompatDelegate.MODE_NIGHT_YES
    const val SYSTEM_DEFAULT_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveThemeMode(context: Context, themeMode: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_THEME_MODE, themeMode)
        editor.apply()
    }

    fun loadThemeMode(context: Context): Int {
        // Əgər heç bir seçim yadda saxlanmayıbsa, defolt olaraq Sistem seçimini qaytar
        return getPreferences(context).getInt(KEY_THEME_MODE, SYSTEM_DEFAULT_MODE)
    }

    fun applyTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    fun applyPersistedTheme(context: Context) {
        val savedMode = loadThemeMode(context)
        applyTheme(savedMode)
    }
}