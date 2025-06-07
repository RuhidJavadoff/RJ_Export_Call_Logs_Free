package com.ruhidjavadoff.rjexportcalllogsfree

import android.content.Context
import android.provider.CallLog
import android.util.Log

class CallLogRepository(private val context: Context) {

    /**
     * Verilmiş tarix aralığı və zəng növləri üçün zəng tarixçəsini yükləyir.
     * @param startDate Başlanğıc tarixi (millisaniyə), null olarsa başlanğıc tarixi yoxdur.
     * @param endDate Son tarix (millisaniyə), null olarsa son tarix yoxdur.
     * @param callTypes Yüklənəcək zəng növlərinin siyahısı (məsələn, CallLog.Calls.INCOMING_TYPE).
     * @return CallLogItem siyahısı. SecurityException halında xəta baş verərsə, onu yuxarı ötürür.
     * @throws SecurityException Əgər READ_CALL_LOG icazəsi yoxdursa.
     */
    @Throws(SecurityException::class, Exception::class)
    fun fetchCallLogs(
        startDate: Long?,
        endDate: Long?,
        callTypes: List<Int>
    ): List<CallLogItem> {
        if (callTypes.isEmpty()) {
            Log.w("CallLogRepository", "No call types specified. Returning empty list.")
            return emptyList()
        }

        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )

        val selectionParts = mutableListOf<String>()
        val selectionArgsList = mutableListOf<String>()

        // Zəng növləri üçün selection
        val callTypePlaceholders = callTypes.joinToString(separator = ", ") { "?" }
        selectionParts.add("${CallLog.Calls.TYPE} IN ($callTypePlaceholders)")
        callTypes.forEach { selectionArgsList.add(it.toString()) }

        // Tarix aralığı üçün selection
        startDate?.let {
            selectionParts.add("${CallLog.Calls.DATE} >= ?")
            selectionArgsList.add(it.toString())
        }
        endDate?.let {
            selectionParts.add("${CallLog.Calls.DATE} <= ?")
            selectionArgsList.add(it.toString())
        }

        val selection = selectionParts.joinToString(separator = " AND ")
        val selectionArgs = selectionArgsList.toTypedArray()
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        val callLogList = mutableListOf<CallLogItem>()

        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val numberColumn = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val dateColumn = it.getColumnIndexOrThrow(CallLog.Calls.DATE)
                val durationColumn = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                val typeColumn = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)

                while (it.moveToNext()) {
                    val phoneNumber = it.getString(numberColumn)
                    val timestamp = it.getLong(dateColumn)
                    val duration = it.getLong(durationColumn)
                    val callType = it.getInt(typeColumn)
                    // Yalnız tələb olunan növləri əlavə etdiyimizdən əmin olmaq üçün (sorğu bunu etsə də)
                    if (callTypes.contains(callType)) {
                        val callLogItem = CallLogItem(phoneNumber, timestamp, duration, callType)
                        callLogList.add(callLogItem)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("CallLogRepository", "READ_CALL_LOG icazəsi yoxdur!", e)
            throw e // MainActivity-nin tutması üçün yenidən ötür
        } catch (e: Exception) {
            Log.e("CallLogRepository", "Zəng tarixçəsini oxuyarkən qeyri-müəyyən xəta baş verdi", e)
            throw e // MainActivity-nin tutması üçün yenidən ötür
        }
        return callLogList
    }
}