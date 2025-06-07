package com.ruhidjavadoff.rjexportcalllogsfree

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale

object LocaleHelper {

    private const val SELECTED_LANGUAGE_KEY = "selected_language_code"
    private const val APP_PREFERENCES = "app_language_prefs"

    // Tətbiqinizin dəstəklədiyi dillərin kodları (siyahını öz dillərinizə uyğunlaşdırın)
    // "system" xüsusi bir dəyərdir, cihaz dilini ifadə edir.
    val supportedLanguageCodes = listOf(
        "system", "en", "az", "tr", "ru", "sw", "ak", "sn", "nd", "fil", "si", "ta", "hi", "ko", "ja"
    )
    private const val DEFAULT_FALLBACK_LANGUAGE_CODE = "en" // Sistem dili dəstəklənməzsə istifadə olunacaq dil

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
    }

    /**
     * İstifadəçinin seçdiyi dil kodunu yadda saxlayır.
     * "system" dəyəri cihazın dilini istifadə etmək deməkdir.
     */
    fun saveSelectedLanguage(context: Context, languageCode: String) {
        getPreferences(context).edit().putString(SELECTED_LANGUAGE_KEY, languageCode).apply()
    }

    /**
     * Yadda saxlanmış dil kodunu qaytarır.
     * Əgər heç bir seçim yoxdursa, "system" qaytarır.
     */
    fun getSelectedLanguage(context: Context): String {
        return getPreferences(context).getString(SELECTED_LANGUAGE_KEY, "system") ?: "system"
    }

    /**
     * Verilmiş dil koduna uyğun Locale yaradır və tətbiq edir.
     * Bu metod Activity-nin `attachBaseContext` və ya `Application.onCreate`-də çağırılmalıdır.
     */
    fun applyLocale(context: Context): Context {
        val selectedLanguageCode = getSelectedLanguage(context)
        var localeToApply: Locale // DƏYİŞİKLİK: val -> var

        if (selectedLanguageCode == "system" || selectedLanguageCode.isBlank()) {
            // Sistem dilini istifadə et
            localeToApply = Resources.getSystem().configuration.locales.get(0)
            // Əgər sistem dili dəstəklədiyimiz dillər arasında yoxdursa və defolt fallback dilindən fərqlidirsə, ingilis dilinə keç
            if (!supportedLanguageCodes.contains(localeToApply.language) && localeToApply.language != DEFAULT_FALLBACK_LANGUAGE_CODE) {
                localeToApply = Locale(DEFAULT_FALLBACK_LANGUAGE_CODE) // Burada yenidən təyin edilir
            }
        } else {
            // Yadda saxlanmış dili istifadə et
            localeToApply = Locale(selectedLanguageCode)
        }

        return updateResources(context, localeToApply)
    }

    /**
     * Activity-nin kontekstini yeni locale ilə yeniləyir (API 17+).
     * Activity-nin attachBaseContext-də çağırılmalıdır.
     */
    fun onAttach(context: Context): Context {
        return applyLocale(context)
    }

    /**
     * Application səviyyəsində dilin tətbiqi üçün (Application.onCreate-də çağırılır)
     */
    fun applyLocaleToApplication(applicationContext: Context) {
        val selectedLanguageCode = getSelectedLanguage(applicationContext)
        var localeToApply: Locale // DƏYİŞİKLİK: val -> var

        if (selectedLanguageCode == "system" || selectedLanguageCode.isBlank()) {
            localeToApply = Resources.getSystem().configuration.locales.get(0)
            if (!supportedLanguageCodes.contains(localeToApply.language) && localeToApply.language != DEFAULT_FALLBACK_LANGUAGE_CODE) {
                localeToApply = Locale(DEFAULT_FALLBACK_LANGUAGE_CODE) // Burada yenidən təyin edilir
            }
        } else {
            localeToApply = Locale(selectedLanguageCode)
        }
        updateResources(applicationContext, localeToApply) // App context-i yenilə
    }


    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }
}