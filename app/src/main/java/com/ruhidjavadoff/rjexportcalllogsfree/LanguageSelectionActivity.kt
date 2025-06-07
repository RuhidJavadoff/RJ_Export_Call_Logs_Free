package com.ruhidjavadoff.rjexportcalllogsfree

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ruhidjavadoff.rjexportcalllogsfree.databinding.ActivityLanguageSelectionBinding

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding
    private lateinit var languageAdapter: LanguageAdapter
    private val availableLanguages: MutableList<AppLanguage> = mutableListOf()
    private var currentTentativeSelectedLanguageCode: String = "" // İstifadəçinin siyahıda seçdiyi, amma hələ təsdiqləmədiyi dil
    private var initiallyAppliedLanguageCode: String = "" // Bu ekran açılarkən tətbiqdə aktiv olan dil

    // Hər Activity üçün dil kontekstini düzgün qurmaq vacibdir
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ekran açılarkən tətbiqin hazırkı dilini alırıq
        initiallyAppliedLanguageCode = LocaleHelper.getSelectedLanguage(this)
        currentTentativeSelectedLanguageCode = initiallyAppliedLanguageCode // İlkin olaraq seçili dil budur

        setupToolbar()
        setupRecyclerView()
        loadAndDisplayLanguages()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLanguageSelection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Geri düyməsini göstər
        supportActionBar?.title = getString(R.string.title_language_selection) // Başlığı strings.xml-dən al
    }

    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter(
            availableLanguages, // İlkin olaraq boşdur, loadAndDisplayLanguages-də dolacaq
            currentTentativeSelectedLanguageCode // Adapterə hazırkı seçimi bildiririk
        ) { selectedLanguage ->
            // İstifadəçi RecyclerView-dən bir dilə kliklədikdə bu lambda işə düşür
            currentTentativeSelectedLanguageCode = selectedLanguage.code // Təsdiqlənməmiş seçimi yenilə
            languageAdapter.updateSelection(currentTentativeSelectedLanguageCode) // RadioButton-ların görünüşünü yenilə
        }
        binding.recyclerViewLanguages.apply {
            layoutManager = LinearLayoutManager(this@LanguageSelectionActivity)
            adapter = languageAdapter
        }
    }

    private fun loadAndDisplayLanguages() {
        availableLanguages.clear() // Əvvəlki məlumatları təmizləyirik (əgər varsa)

        // 1. Sistem Dili Seçimi (Həmişə birinci)
        availableLanguages.add(
            AppLanguage(
                code = "system",
                nativeName = getString(R.string.language_option_system_default), // strings.xml-dən
                englishName = "System Default" // Bu, adapterdə nativeName ilə müqayisə üçün istifadə edilə bilər
            )
        )

        // 2. Dəstəklənən Digər Dillər (Sizin siyahınıza uyğun)
        // Native adlar və İngiliscə adlar ilə birlikdə
        availableLanguages.add(AppLanguage("az", "Azərbaycan dili", "Azerbaijani"))
        availableLanguages.add(AppLanguage("en", "English", "English"))
        availableLanguages.add(AppLanguage("tr", "Türkçe", "Turkish"))
        availableLanguages.add(AppLanguage("ru", "Русский", "Russian"))
        availableLanguages.add(AppLanguage("sw", "Kiswahili", "Swahili"))
        availableLanguages.add(AppLanguage("ak", "Akan", "Akan")) // Akan üçün native adı dəqiqləşdirin
        availableLanguages.add(AppLanguage("sn", "chiShona", "Shona"))
        availableLanguages.add(AppLanguage("nd", "isiNdebele", "Northern Ndebele"))
        availableLanguages.add(AppLanguage("fil", "Filipino", "Filipino"))
        availableLanguages.add(AppLanguage("si", "සිංහල", "Sinhala"))
        availableLanguages.add(AppLanguage("ta", "தமிழ்", "Tamil"))
        availableLanguages.add(AppLanguage("hi", "हिन्दी", "Hindi"))
        availableLanguages.add(AppLanguage("ko", "한국어", "Korean"))
        availableLanguages.add(AppLanguage("ja", "日本語", "Japanese"))
        // ... (digər dilləriniz)

        // Adapteri yeni məlumatlar siyahısı və hazırkı seçilmiş dil ilə yeniləyirik
        languageAdapter.updateLanguages(availableLanguages, currentTentativeSelectedLanguageCode)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_language_selection, menu) // Təsdiqlə menyusunu toolbar-a əlavə edir
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { // Toolbar-dakı geri düyməsi
                onBackPressedDispatcher.onBackPressed() // Standart geri davranışını icra et
                return true
            }
            R.id.action_confirm_language -> { // Toolbar-dakı "Təsdiqlə" düyməsi
                applyLanguageChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun applyLanguageChanges() {
        // Yalnız dil həqiqətən dəyişibsə (ekran açılanda olan dildən fərqlidirsə)
        if (initiallyAppliedLanguageCode != currentTentativeSelectedLanguageCode) {
            LocaleHelper.saveSelectedLanguage(this, currentTentativeSelectedLanguageCode) // Yeni seçimi yadda saxla
            LocaleHelper.applyLocaleToApplication(applicationContext) // Dili bütün tətbiq üçün yenilə

            // Əvvəlki Activity-yə (SettingsActivity) dilin dəyişdiyini bildirmək üçün nəticə təyin et
            setResult(Activity.RESULT_OK) // Dəyişiklik oldu
            finish() // Bu Activity-ni bağla

            // SettingsActivity bu nəticəni (RESULT_OK) alıb özünü recreate() etməlidir
            // ki, yeni dil orada da görünsün.
        } else {
            // Dil dəyişməyibsə, heç bir şey etmə və ya istifadəçiyə mesaj göstər
            // Toast.makeText(this, getString(R.string.toast_language_not_changed), Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED) // Dəyişiklik olmadı
            finish() // Bu Activity-ni bağla
        }
    }
}