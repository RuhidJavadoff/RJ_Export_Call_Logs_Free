package com.ruhidjavadoff.rjexportcalllogsfree // Paket adınızı yoxlayın

import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import android.widget.Toast
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream // Bu import outputStream?.close() üçün lazımdır
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Excel faylını yaradan və verilən URI-yə yazan funksiya
fun writeExcelToFile(context: Context, uri: Uri, logList: List<CallLogItem>) {
    Log.d("ExcelExporter", "Excel ixracı başladı. Siyahı ölçüsü: ${logList.size}")
    if (logList.isEmpty()) {
        Log.w("ExcelExporter", "İxrac üçün məlumat yoxdur.")
        // İstəsəniz, burada da istifadəçiyə Toast ilə məlumat verə bilərsiniz:
        // Toast.makeText(context, context.getString(R.string.toast_no_data_to_export), Toast.LENGTH_SHORT).show()
        return
    }

    var workbook: Workbook? = null
    var outputStream: OutputStream? = null // Dəyişənin tipini dəqiq göstəririk

    try {
        workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet(context.getString(R.string.excel_sheet_name_call_history))

        val headerCellStyle: CellStyle = workbook.createCellStyle()
        val headerFont: Font = workbook.createFont()
        headerFont.bold = true
        headerCellStyle.setFont(headerFont)

        // Başlıqlar string resurslarından alınır
        val headers = listOf(
            context.getString(R.string.excel_header_number),
            context.getString(R.string.excel_header_date),
            context.getString(R.string.excel_header_duration),
            context.getString(R.string.excel_header_type)
        )

        val headerRow: Row = sheet.createRow(0)
        headers.forEachIndexed { index, headerText ->
            val cell: Cell = headerRow.createCell(index)
            cell.setCellValue(headerText)
            cell.cellStyle = headerCellStyle
        }
        Log.d("ExcelExporter", "Başlıq sətri yaradıldı.")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        logList.forEachIndexed { rowIndex, item ->
            val row: Row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue(item.phoneNumber ?: context.getString(R.string.text_unknown_number)) // Naməlum nömrə üçün string
            val dateStr = sdf.format(Date(item.timestamp))
            row.createCell(1).setCellValue(dateStr)
            row.createCell(2).setCellValue(item.duration.toDouble()) // Müddət rəqəm kimi qalır

            // Zəng növü string resurslarından alınır
            val typeStr = when (item.callType) {
                CallLog.Calls.INCOMING_TYPE -> context.getString(R.string.call_type_answered)
                CallLog.Calls.MISSED_TYPE -> context.getString(R.string.call_type_missed)
                CallLog.Calls.OUTGOING_TYPE -> context.getString(R.string.call_type_outgoing)
                CallLog.Calls.VOICEMAIL_TYPE -> context.getString(R.string.call_type_voicemail)
                CallLog.Calls.REJECTED_TYPE -> context.getString(R.string.call_type_rejected)
                CallLog.Calls.BLOCKED_TYPE -> context.getString(R.string.call_type_blocked)
                else -> context.getString(R.string.call_type_unknown_formatted, item.callType.toString()) // Naməlum növ üçün formatlı string
            }
            row.createCell(3).setCellValue(typeStr)
        }
        Log.d("ExcelExporter", "${logList.size} sətir məlumat əlavə edildi.")

        // Sütun eni tənzimlənməsi hissəsi (kommentdə qalıb)
        /*
        Log.d("ExcelExporter", "Sütun enləri tənzimlənir...")
        headers.indices.forEach { index ->
            try {
                sheet.autoSizeColumn(index)
            } catch (e: Exception) {
                Log.w("ExcelExporter", "Sütun $index üçün autoSizeColumn xətası: ${e.message}")
            }
        }
        Log.d("ExcelExporter", "Sütun enləri tənzimləndi.")
        */

        Log.d("ExcelExporter", "Fayla yazmağa cəhd edilir: $uri")
        outputStream = context.contentResolver.openOutputStream(uri)
        if (outputStream != null) {
            workbook.write(outputStream)
            Log.d("ExcelExporter", "Workbook uğurla OutputStream-ə yazıldı.")
            Toast.makeText(context, context.getString(R.string.toast_excel_export_success), Toast.LENGTH_SHORT).show()
        } else {
            Log.e("ExcelExporter", "OutputStream null-dır. Fayla yazıla bilmədi.")
            Toast.makeText(context, context.getString(R.string.toast_excel_export_error_opening_file), Toast.LENGTH_SHORT).show()
        }

    } catch (e: Exception) {
        Log.e("ExcelExporter", "Excel faylı yaradılarkən/yazılarkən xəta baş verdi", e)
        Toast.makeText(context, context.getString(R.string.toast_excel_export_error_saving), Toast.LENGTH_SHORT).show()
    } finally {
        try {
            outputStream?.flush() // Məlumatların tam yazıldığından əmin olmaq üçün
            outputStream?.close()
            Log.d("ExcelExporter", "OutputStream bağlandı.")
        } catch (ex: Exception) {
            Log.e("ExcelExporter", "OutputStream bağlayarkən xəta", ex)
        }
        try {
            workbook?.close() // Workbook-u bağlamaq vacibdir, xüsusilə XSSFWorkbook üçün
            Log.d("ExcelExporter", "Workbook bağlandı.")
        } catch (ex: Exception) {
            Log.e("ExcelExporter", "Workbook bağlayarkən xəta", ex)
        }
    }
}