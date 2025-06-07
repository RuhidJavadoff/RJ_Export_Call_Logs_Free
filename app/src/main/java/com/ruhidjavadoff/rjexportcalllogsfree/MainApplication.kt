package com.ruhidjavadoff.rjexportcalllogsfree

import android.app.Application
import android.content.Context

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Tətbiq başlayanda yadda saxlanmış dili tətbiq et
        LocaleHelper.applyLocaleToApplication(applicationContext)
    }

    override fun attachBaseContext(base: Context) {
        // Hər Activity yaranmazdan əvvəl dilin düzgün kontekstlə tətbiqi üçün
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }
}