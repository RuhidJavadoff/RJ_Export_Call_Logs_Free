package com.ruhidjavadoff.rjexportcalllogsfree

import android.app.Activity
import android.content.Intent
import android.net.Uri
// import android.provider.CallLog // Bu faylda birbaşa istifadə olunmur, CallLogItem-dan gəlir
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportHelper(private val activity: AppCompatActivity) {

    // Köhnə, istifadə olunmayan launcher-lar silindi.
    // Əgər lazım idisə, geri qaytarıla bilər.

    // Saxlanmış məlumatlar üçün müvəqqəti dəyişənlər (launcher callback-ləri üçün)
    private var pendingContentForTxt: String? = null
    private var pendingListForPdf: List<CallLogItem>? = null
    private var pendingListForXlsx: List<CallLogItem>? = null

    // Yenidən işlənmiş (aktiv) launcher-lar
    private val launcherTxt = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                pendingContentForTxt?.let { content ->
                    writeTextToFileInternal(uri, content)
                    pendingContentForTxt = null // Təmizlə
                }
            }
        } else {
            Toast.makeText(activity, activity.getString(R.string.toast_file_not_selected), Toast.LENGTH_SHORT).show()
        }
        pendingContentForTxt = null // Hər halda təmizlə
    }

    private val launcherPdf = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                pendingListForPdf?.let { list ->
                    writePdfToFile(activity, uri, list) // PdfExporter.kt-dən gələn funksiya
                    pendingListForPdf = null // Təmizlə
                }
            }
        } else {
            Toast.makeText(activity, activity.getString(R.string.toast_file_not_selected), Toast.LENGTH_SHORT).show()
        }
        pendingListForPdf = null // Hər halda təmizlə
    }

    private val launcherXlsx = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                pendingListForXlsx?.let { list ->
                    writeExcelToFile(activity, uri, list) // ExcelExporter.kt-dən gələn funksiya
                    pendingListForXlsx = null // Təmizlə
                }
            }
        } else {
            Toast.makeText(activity, activity.getString(R.string.toast_file_not_selected), Toast.LENGTH_SHORT).show()
        }
        pendingListForXlsx = null // Hər halda təmizlə
    }

    // --- İctimai İxrac Funksiyaları ---
    fun exportToTxt(logList: List<CallLogItem>, filterName: String, callTypeFormatter: (Int) -> String) {
        if (logList.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.toast_no_data_to_export), Toast.LENGTH_SHORT).show()
            return
        }
        // callTypeFormatter artıq Context qəbul etməlidir, əgər string resurslarından istifadə edirsə
        // MainActivity-dən ötürülən formatter funksiyası bu konteksti təmin etməlidir.
        // Məsələn: { type -> callLogAdapter.formatCallTypePublic(activity, type) }
        pendingContentForTxt = generateTxtContentInternal(logList, callTypeFormatter)

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val sanitizedFilterName = filterName.replace("[^A-Za-z0-9]+".toRegex(), "_")
        val suggestedFileName = "call_log_export_${sanitizedFilterName}_$timeStamp.txt"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, suggestedFileName)
        }
        launcherTxt.launch(intent)
    }

    fun exportToPdf(logList: List<CallLogItem>, filterName: String) {
        if (logList.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.toast_no_data_to_export), Toast.LENGTH_SHORT).show()
            return
        }
        pendingListForPdf = logList

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val sanitizedFilterName = filterName.replace("[^A-Za-z0-9]+".toRegex(), "_")
        val suggestedFileName = "call_log_export_${sanitizedFilterName}_$timeStamp.pdf"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, suggestedFileName)
        }
        launcherPdf.launch(intent)
    }

    fun exportToXlsx(logList: List<CallLogItem>, filterName: String) {
        if (logList.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.toast_no_data_to_export), Toast.LENGTH_SHORT).show()
            return
        }
        pendingListForXlsx = logList

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val sanitizedFilterName = filterName.replace("[^A-Za-z0-9]+".toRegex(), "_")
        val suggestedFileName = "call_log_export_${sanitizedFilterName}_$timeStamp.xlsx"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_TITLE, suggestedFileName)
        }
        launcherXlsx.launch(intent)
    }

    // --- Daxili Köməkçi Funksiyalar ---
    private fun writeTextToFileInternal(uri: Uri, content: String) {
        try {
            activity.contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { fileOutputStream ->
                    fileOutputStream.write(content.toByteArray())
                }
            }
            Toast.makeText(activity, activity.getString(R.string.toast_txt_export_success), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ExportHelper", "TXT faylına yazarkən xəta baş verdi", e)
            Toast.makeText(activity, activity.getString(R.string.toast_txt_export_error_saving), Toast.LENGTH_SHORT).show()
            e.printStackTrace() // Xətanı loglamaq üçün
        }
    }

    private fun generateTxtContentInternal(logList: List<CallLogItem>, callTypeFormatter: (Int) -> String): String {
        // TXT faylının başlıqları üçün string resurslarından istifadə
        val headerNumber = activity.getString(R.string.txt_header_number)
        val headerDate = activity.getString(R.string.txt_header_date)
        val headerDuration = activity.getString(R.string.txt_header_duration)
        val headerType = activity.getString(R.string.txt_header_type)

        val header = "$headerNumber\t$headerDate\t$headerDuration\t$headerType\n"
        val content = StringBuilder(header)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        logList.forEach { item ->
            val dateStr = sdf.format(Date(item.timestamp))
            val typeStr = callTypeFormatter(item.callType) // Bu artıq lokallaşdırılmış string qaytarmalıdır
            val number = item.phoneNumber ?: activity.getString(R.string.text_unknown_number) // Naməlum nömrə
            content.append("$number\t$dateStr\t${item.duration}\t$typeStr\n")
        }
        return content.toString()
    }
}