package com.ruhidjavadoff.rjexportcalllogsfree

import android.app.Activity // RESULT_OK üçün
import android.content.Context // attachBaseContext üçün
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources // Dil yoxlaması üçün
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts // Yeni launcher üçün
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ruhidjavadoff.rjexportcalllogsfree.databinding.ActivitySettingsBinding
import java.util.Locale // Dil yoxlaması üçün

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    // Activity-nin hansı dil ilə yaradıldığını yadda saxlamaq üçün
    private var createdWithLocaleLanguage: String? = null

    // LanguageSelectionActivity-ni nəticə üçün başlatmaq üçün launcher
    private val languageChangeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Dil LanguageSelectionActivity-də dəyişdirildi və təsdiqləndi.
            // SettingsActivity-ni yenidən yaradırıq ki, yeni dili tətbiq etsin.
            recreate()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activity-nin həqiqi yaradıldığı dilini yadda saxlayırıq
        createdWithLocaleLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0).language
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale.language
        }

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        displayAppVersion()
    }

    override fun onResume() {
        super.onResume()
        // Yadda saxlanmış qlobal dil seçimi ilə Activity-nin yaradıldığı dil arasında fərq olub-olmadığını yoxla
        val currentAppLocalePreference = LocaleHelper.getSelectedLanguage(this)
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale
        }

        val targetLanguageCode = if (currentAppLocalePreference == "system") {
            var systemLanguageToUse = systemLocale.language
            val actualSupportedCodes = LocaleHelper.supportedLanguageCodes.filter { it != "system" }
            if (!actualSupportedCodes.contains(systemLanguageToUse)) {
                systemLanguageToUse = "en" // Defolt İngilis dilinə fallback
            }
            systemLanguageToUse
        } else {
            currentAppLocalePreference
        }

        if (createdWithLocaleLanguage != null && createdWithLocaleLanguage != targetLanguageCode) {
            recreate() // Əgər dil dəyişibsə, Activity-ni yenidən yarat
        }
        // Dil dəyişməyibsə, və ya ilk açılışdırsa, onCreate-də təyin olunan createdWithLocaleLanguage yenilənəcək
        // və ya növbəti onResume-da düzgün olacaq.
        // Hər onResume-da createdWithLocaleLanguage-i yeniləmək də olar, amma recreate onsuz da onCreate-i çağıracaq.
    }


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        // Toolbar başlığı XML-də @string/settings_title ilə təyin edilməlidir
        // və ya burada getString(R.string.settings_title) ilə
        supportActionBar?.title = getString(R.string.settings_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupClickListeners() {
        // Tema
        binding.settingTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        // Dil
        binding.settingLanguage.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            languageChangeLauncher.launch(intent) // Nəticə üçün başlat
        }

        // Digər Tətbiqlər
        binding.settingOtherApps.setOnClickListener {
            openUrl("https://ruhidjavadoff.site/app/")
        }

        // Məxfilik Siyasəti
        binding.settingPrivacyPolicy.setOnClickListener {
            openUrl("https://ruhidjavadoff.site/satinalma/proqramlar/gizliliksertleri.html")
        }

        // Haqqında
        binding.settingAboutApp.setOnClickListener {
            openUrl("https://ruhidjavadoff.site/app/callex/")
        }

        // Bizi İzlə
        binding.settingFollowUs.setOnClickListener {
            openUrl("https://ruhidjavadoff.site/followme/")
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = resources.getStringArray(R.array.theme_options)
        val currentThemeMode = ThemeHelper.loadThemeMode(this)

        val checkedItem = when (currentThemeMode) {
            ThemeHelper.LIGHT_MODE -> 0
            ThemeHelper.DARK_MODE -> 1
            else -> 2
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title_select_theme))
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selectedMode = when (which) {
                    0 -> ThemeHelper.LIGHT_MODE
                    1 -> ThemeHelper.DARK_MODE
                    else -> ThemeHelper.SYSTEM_DEFAULT_MODE
                }
                ThemeHelper.saveThemeMode(this, selectedMode)
                ThemeHelper.applyTheme(selectedMode)
                dialog.dismiss()
                recreate() // Tema dəyişikliyinin dərhal görünməsi üçün
            }
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .show()
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_no_app_to_open_link), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun displayAppVersion() {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            val version = packageInfo.versionName
            binding.textViewAppVersion.text = getString(R.string.text_app_version, version)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            binding.textViewAppVersion.text = getString(R.string.text_app_version_na)
        }
    }
}