package com.ruhidjavadoff.rjexportcalllogsfree // Paket adınızı yoxlayın

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.CallLog
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
// import java.io.FileOutputStream // Birbaşa istifadə olunmursa, lazım deyil
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// CallLogItem data class-ının təxmini strukturu (əgər fərqlidirsə uyğunlaşdırın)
// data class CallLogItem(
//     val phoneNumber: String?,
//     val timestamp: Long,
//     val duration: Long,
//     val callType: Int
// )

// PDF faylını yaradan və verilən URI-yə yazan funksiya
fun writePdfToFile(context: Context, uri: Uri, logList: List<CallLogItem>) {
    val pdfDocument = PdfDocument()
    val pageWidth = 595 // A4 eni (points)
    val pageHeight = 842 // A4 hündürlüyü (points)
    val topMargin = 50f
    val bottomMargin = 50f // Səhifənin alt boşluğu
    val leftMargin = 40f
    val lineSpacing = 15f // Sətirlər arası məsafə
    val colWidths = floatArrayOf(150f, 120f, 80f, 100f) // Sütun enləri

    // Rəsm üçün Paint obyektləri
    val titlePaint = TextPaint().apply {
        color = Color.BLACK // Bu rənglər də tema atributlarından alına bilər (gələcəkdə)
        textSize = 16f
        isFakeBoldText = true
    }
    val textPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 10f
    }
    val headerPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 11f
        isFakeBoldText = true
    }

    // Tarix formatı
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    // PDF başlıqları string resurslarından alınır
    val headers = listOf(
        context.getString(R.string.pdf_header_number),
        context.getString(R.string.pdf_header_date),
        context.getString(R.string.pdf_header_duration),
        context.getString(R.string.pdf_header_type)
    )

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var page: PdfDocument.Page = pdfDocument.startPage(pageInfo)
    var canvas: Canvas = page.canvas
    var currentY = topMargin

    fun drawPageContentHeaders(canvasToDrawOn: Canvas, startY: Float): Float {
        var yPos = startY
        var currentX = leftMargin
        headers.forEachIndexed { index, headerText ->
            canvasToDrawOn.drawText(headerText, currentX, yPos, headerPaint)
            currentX += colWidths.getOrElse(index) { 100f }
        }
        yPos += lineSpacing * 1.5f
        return yPos
    }

    // İlk səhifənin ümumi başlığı string resursundan alınır
    canvas.drawText(context.getString(R.string.pdf_document_title), leftMargin, currentY, titlePaint)
    currentY += lineSpacing * 2

    currentY = drawPageContentHeaders(canvas, currentY)

    logList.forEach { item ->
        if (currentY + lineSpacing > pageHeight - bottomMargin) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            currentY = topMargin
            // İstəyə bağlı: Yeni səhifənin də ümumi başlığı olsun
            // canvas.drawText(context.getString(R.string.pdf_document_title_page, pageNumber), leftMargin, currentY, titlePaint)
            // currentY += lineSpacing * 2
            currentY = drawPageContentHeaders(canvas, currentY)
        }

        var currentX = leftMargin
        // Nömrə
        canvas.drawText(item.phoneNumber ?: context.getString(R.string.text_unknown_number), currentX, currentY, textPaint)
        currentX += colWidths[0]

        // Tarix
        val dateStr = sdf.format(Date(item.timestamp))
        canvas.drawText(dateStr, currentX, currentY, textPaint)
        currentX += colWidths[1]

        // Müddət
        canvas.drawText(item.duration.toString(), currentX, currentY, textPaint) // Rəqəm olduğu üçün birbaşa yazılır
        currentX += colWidths[2]

        // Növ (string resurslarından)
        val typeStr = when (item.callType) {
            CallLog.Calls.INCOMING_TYPE -> context.getString(R.string.call_type_answered)
            CallLog.Calls.MISSED_TYPE -> context.getString(R.string.call_type_missed)
            CallLog.Calls.OUTGOING_TYPE -> context.getString(R.string.call_type_outgoing)
            CallLog.Calls.REJECTED_TYPE -> context.getString(R.string.call_type_rejected)
            CallLog.Calls.VOICEMAIL_TYPE -> context.getString(R.string.call_type_voicemail)
            CallLog.Calls.BLOCKED_TYPE -> context.getString(R.string.call_type_blocked)
            else -> context.getString(R.string.call_type_unknown_formatted, item.callType.toString())
        }
        canvas.drawText(typeStr, currentX, currentY, textPaint)

        currentY += lineSpacing
    }

    pdfDocument.finishPage(page)

    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            // outputStream.close() // 'use' bloku bunu avtomatik edir
        }
        // Formatlı string istifadə edirik
        Toast.makeText(context, context.getString(R.string.toast_pdf_export_success_pages, pageNumber), Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        Log.e("PdfExporter", "PDF faylına yazarkən xəta baş verdi", e)
        // Formatlı string istifadə edirik
        Toast.makeText(context, context.getString(R.string.toast_pdf_export_error_saving, e.message ?: "N/A"), Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}