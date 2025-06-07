package com.ruhidjavadoff.rjexportcalllogsfree

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair // MaterialDatePicker üçün androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.ruhidjavadoff.rjexportcalllogsfree.databinding.ActivityMainBinding // Layout binding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainUiHelper(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding
) {

    fun setupToolbar() {
        activity.supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    fun setupRecyclerView(adapter: CallLogAdapter) {
        binding.bodySection.recyclerViewCallLog.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    fun setupFilterButton(
        filterOptions: Array<String>, // Bu array-in elementləri MainActivity-dən gəlir və artıq string resursu olmalıdır
        currentFilterIndexProvider: () -> Int,
        currentCustomDateProvider: () -> kotlin.Pair<Long?, Long?>,
        onFilterChanged: (newIndex: Int, newStartDate: Long?, newEndDate: Long?) -> Unit
    ) {
        binding.bodySection.buttonSelectFilter.setOnClickListener {
            showTimeFilterDialog(filterOptions, currentFilterIndexProvider, currentCustomDateProvider, onFilterChanged)
        }
    }

    fun updateFilterButtonTextView(
        filterOptions: Array<String>, // Bu array-in elementləri MainActivity-dən gəlir
        selectedFilterIndex: Int,
        customStartDate: Long?,
        customEndDate: Long?
    ) {
        val selectedText = if (selectedFilterIndex == filterOptions.lastIndex && customStartDate != null && customEndDate != null) {
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault()) // Tarix formatı lokallaşdırıla bilər, amma hələlik belə qalsın
            "${sdf.format(Date(customStartDate))} - ${sdf.format(Date(customEndDate))}"
        } else {
            if (selectedFilterIndex >= 0 && selectedFilterIndex < filterOptions.size) {
                filterOptions[selectedFilterIndex] // Bu, artıq string resursundan gələn lokallaşdırılmış mətn olmalıdır
            } else {
                activity.getString(R.string.text_filter_button_default) // Keçərsiz indeks üçün defolt mətn
            }
        }
        binding.bodySection.buttonSelectFilter.text = selectedText
    }

    fun setupDonateButton(donateUrl: String) {
        // "Dəstək ol!" mətni footer layout-da string resursu ilə əvəz edilmişdi
        binding.footerSection.textViewDonate.setOnClickListener {
            openUrlUtil(donateUrl)
        }
    }

    fun openUrlUtil(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainUiHelper", "URL açılarkən xəta: $url", e)
            Toast.makeText(activity, activity.getString(R.string.toast_no_app_to_open_link), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun showTimeFilterDialog(
        filterOptions: Array<String>, // Bu array-in elementləri MainActivity-dən gəlir
        currentFilterIndexProvider: () -> Int,
        currentCustomDateProvider: () -> kotlin.Pair<Long?, Long?>,
        onFilterChanged: (newIndex: Int, newStartDate: Long?, newEndDate: Long?) -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog_title_select_time_filter))
            .setSingleChoiceItems(filterOptions, currentFilterIndexProvider()) { dialog, which ->
                // filterOptions[filterOptions.lastIndex] "Xüsusi Aralıq" mətnini ehtiva etməlidir (string resursundan)
                if (which == filterOptions.lastIndex) {
                    dialog.dismiss()
                    showDateRangePicker(filterOptions, onFilterChanged)
                } else {
                    onFilterChanged(which, null, null)
                    dialog.dismiss()
                }
            }
            .setNegativeButton(activity.getString(R.string.dialog_button_cancel), null)
            .show()
    }

    private fun showDateRangePicker(
        filterOptions: Array<String>, // filterOptions.lastIndex istifadə olunur
        onFilterChanged: (newIndex: Int, newStartDate: Long?, newEndDate: Long?) -> Unit
    ) {
        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(activity.getString(R.string.datepicker_title_select_date_range))
            .build()

        datePicker.addOnPositiveButtonClickListener { selection: androidx.core.util.Pair<Long, Long> ->
            val startDateMillis = selection.first
            val endDateMillis = selection.second

            if (startDateMillis != null && endDateMillis != null) {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

                calendar.timeInMillis = startDateMillis
                calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
                val finalStartDate = calendar.timeInMillis

                calendar.timeInMillis = endDateMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
                val finalEndDate = calendar.timeInMillis

                onFilterChanged(filterOptions.lastIndex, finalStartDate, finalEndDate)
            }
        }
        datePicker.show(activity.supportFragmentManager, "DATE_PICKER_UI_HELPER")
    }
}