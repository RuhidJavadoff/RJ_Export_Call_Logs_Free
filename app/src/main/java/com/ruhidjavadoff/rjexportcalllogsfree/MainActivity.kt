package com.ruhidjavadoff.rjexportcalllogsfree

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.Context // attachBaseContext üçün
import android.content.res.Configuration // Dil yoxlaması üçün (opsional, LocaleHelper kifayətdir)
import android.content.res.Resources // Dil yoxlaması üçün
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.ruhidjavadoff.rjexportcalllogsfree.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.Locale // Dil yoxlaması üçün

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var callLogAdapter: CallLogAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var exportHelper: ExportHelper
    private lateinit var uiHelper: MainUiHelper
    private lateinit var callLogRepository: CallLogRepository

    // Activity-nin hansı dil ilə yaradıldığını yadda saxlamaq üçün
    private var createdWithLocaleLanguage: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
        // Dil tətbiq olunduqdan sonra, amma onCreate-dən əvvəl
        // Bu mərhələdə 'resources' obyektinin yeni konfiqurasiyanı əks etdirdiyindən əmin olmaq lazımdır.
        // 'createdWithLocaleLanguage' dəyişənini onCreate-də təyin etmək daha stabildir.
    }

    private var currentCallLogList: List<CallLogItem> = emptyList()
    var selectedFilterIndex = 0
    val filterOptions by lazy { resources.getStringArray(R.array.time_filter_options) }
    var customStartDate: Long? = null
    var customEndDate: Long? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, getString(R.string.toast_permission_granted), Toast.LENGTH_SHORT).show()
                triggerCallLogLoading()
            } else {
                Toast.makeText(this, getString(R.string.toast_permission_required_to_show_call_log), Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // super.onCreate attachBaseContext-dən sonra çağırılır

        // Activity-nin həqiqi yaradıldığı dilini yadda saxlayırıq
        // Bu, resources obyektinin attachBaseContext-də yeniləndiyini fərz edir.
        createdWithLocaleLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0).language
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale.language
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exportHelper = ExportHelper(this)
        uiHelper = MainUiHelper(this, binding)
        callLogRepository = CallLogRepository(applicationContext)

        setupCallLogAdapter()
        setupDrawerAndToolbar()
        uiHelper.setupRecyclerView(callLogAdapter)
        setupClickListeners()
        setupOnBackPressed()
        checkAndRequestPermission() // Bu, triggerCallLogLoading-i çağıra bilər
        uiHelper.updateFilterButtonTextView(filterOptions, selectedFilterIndex, customStartDate, customEndDate)

        try {
            SeasonalEffectsHelper.checkAndShowSeasonalEffect(this, binding.seasonalEffectsContainer)
        } catch (e: Exception) {
            Log.e("MainActivity", "Mövsümi effekt göstərilərkən xəta baş verdi", e)
        }
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
            // Sistem dilinin dəstəklənən versiyasını al (fallback ilə)
            var systemLanguageToUse = systemLocale.language
            // LocaleHelper.supportedLanguageCodes "system" dəyərini də ehtiva edir, ona görə onu çıxarmaq lazım deyil.
            // Amma real dil kodları ilə müqayisə etməliyik.
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
        // Əks halda, onCreate-də yaradılan dil ilə davam edirik.
        // Yaddaşda olan createdWithLocaleLanguage-i yeniləməyə ehtiyac yoxdur,
        // çünki recreate() onsuz da onCreate-i yenidən çağıracaq.
    }


    private fun setupCallLogAdapter() {
        callLogAdapter = CallLogAdapter(emptyList())
    }

    private fun setupDrawerAndToolbar() {
        setSupportActionBar(binding.headerSection.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(true)
            setDisplayUseLogoEnabled(true)
            // setLogo(R.drawable.logom)
        }
        drawerLayout = binding.drawerLayout
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent) // SettingsActivity-ni adi qaydada başlat
            }
            R.id.nav_call_log -> { Toast.makeText(this, getString(R.string.nav_title_call_log), Toast.LENGTH_SHORT).show() }
            R.id.nav_other_apps -> { uiHelper.openUrlUtil("https://ruhidjavadoff.site/app") }
            R.id.nav_privacy_policy -> { uiHelper.openUrlUtil("https://ruhidjavadoff.site/satinalma/proqramlar/gizliliksertleri.html") }
            R.id.nav_about_app -> { uiHelper.openUrlUtil("https://ruhidjavadoff.site/app/callex") }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (isEnabled) { isEnabled = false; onBackPressedDispatcher.onBackPressed(); isEnabled = true }
                }
            }
        })
    }

    private fun setupClickListeners() {
        uiHelper.setupDonateButton("https://ruhidjavadoff.site/donate")
        uiHelper.setupFilterButton(
            filterOptions = filterOptions,
            currentFilterIndexProvider = { selectedFilterIndex },
            currentCustomDateProvider = { kotlin.Pair(customStartDate, customEndDate) },
            onFilterChanged = { newIndex, newStartDate, newEndDate ->
                selectedFilterIndex = newIndex
                customStartDate = newStartDate
                customEndDate = newEndDate
                uiHelper.updateFilterButtonTextView(filterOptions, selectedFilterIndex, customStartDate, customEndDate)
                triggerCallLogLoading()
            }
        )

        binding.bodySection.buttonExportTxt.setOnClickListener {
            val listToExport = getFilteredCallLogs()
            val filterName = filterOptions.getOrElse(selectedFilterIndex) { getString(R.string.text_unknown_filter_fallback) }
            exportHelper.exportToTxt(listToExport, filterName) { callType ->
                if (::callLogAdapter.isInitialized) {
                    callLogAdapter.formatCallTypePublic(this@MainActivity, callType)
                } else {
                    when (callType) {
                        CallLog.Calls.INCOMING_TYPE -> getString(R.string.call_type_answered)
                        CallLog.Calls.MISSED_TYPE -> getString(R.string.call_type_missed)
                        CallLog.Calls.OUTGOING_TYPE -> getString(R.string.call_type_outgoing)
                        CallLog.Calls.VOICEMAIL_TYPE -> getString(R.string.call_type_voicemail)
                        CallLog.Calls.REJECTED_TYPE -> getString(R.string.call_type_rejected)
                        CallLog.Calls.BLOCKED_TYPE -> getString(R.string.call_type_blocked)
                        else -> getString(R.string.call_type_unknown_formatted, callType.toString())
                    }
                }
            }
        }
        binding.bodySection.buttonExportPdf.setOnClickListener {
            val listToExport = getFilteredCallLogs()
            val filterName = filterOptions.getOrElse(selectedFilterIndex) { getString(R.string.text_unknown_filter_fallback) }
            exportHelper.exportToPdf(listToExport, filterName)
        }
        binding.bodySection.buttonExportXlsx.setOnClickListener {
            val listToExport = getFilteredCallLogs()
            val filterName = filterOptions.getOrElse(selectedFilterIndex) { getString(R.string.text_unknown_filter_fallback) }
            exportHelper.exportToXlsx(listToExport, filterName)
        }
    }

    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED -> {
                triggerCallLogLoading()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG) -> {
                Toast.makeText(this, getString(R.string.toast_permission_rationale_call_log), Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
            }
            else -> { requestPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG) }
        }
    }

    private fun triggerCallLogLoading() {
        val timeRange = getTimeRangeForFilter(selectedFilterIndex)
        val startDate = timeRange.first
        val endDate = timeRange.second
        val desiredCallTypes = listOf(CallLog.Calls.INCOMING_TYPE, CallLog.Calls.MISSED_TYPE)

        try {
            currentCallLogList = callLogRepository.fetchCallLogs(startDate, endDate, desiredCallTypes)
            if (::callLogAdapter.isInitialized) {
                callLogAdapter.updateData(currentCallLogList)
            }
            if (currentCallLogList.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_no_calls_found_for_filter), Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e("MainActivity", "İcazə yoxdur! (Repository)", e)
            Toast.makeText(this, getString(R.string.toast_permission_denied_call_log_repo), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Xəta baş verdi (Repository)", e)
            Toast.makeText(this, getString(R.string.toast_error_loading_call_log_repo), Toast.LENGTH_SHORT).show()
        }
    }

    fun getTimeRangeForFilter(filterIndex: Int): kotlin.Pair<Long?, Long?> {
        val calendar = Calendar.getInstance()
        var startTime: Long? = null
        var endTime: Long? = null

        when (filterIndex) {
            0 -> { /* Bütün Zamanlar */ }
            1 -> {
                val todayStart = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                startTime = todayStart.timeInMillis
                val todayEnd = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                endTime = todayEnd.timeInMillis
            }
            2 -> {
                val weekStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                startTime = weekStart.timeInMillis
                val todayEnd = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                endTime = todayEnd.timeInMillis
            }
            3 -> {
                val monthStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -29); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                startTime = monthStart.timeInMillis
                val todayEnd = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                endTime = todayEnd.timeInMillis
            }
            4 -> {
                val yearStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -364); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                startTime = yearStart.timeInMillis
                val todayEnd = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                endTime = todayEnd.timeInMillis
            }
            5 -> {
                startTime = customStartDate
                endTime = customEndDate
            }
        }
        if (filterIndex != 0 && filterIndex != 5 && endTime == null) {
            val currentDayEnd = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
            endTime = currentDayEnd.timeInMillis
        }
        return kotlin.Pair(startTime, endTime)
    }

    fun getFilteredCallLogs(): List<CallLogItem> {
        return currentCallLogList
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_open_drawer) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // toggle.syncState()
    }
}