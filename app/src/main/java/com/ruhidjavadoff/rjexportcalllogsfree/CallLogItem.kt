package com.ruhidjavadoff.rjexportcalllogsfree // Paket adınızı yoxlayın

// Zəng tarixçəsi elementini təmsil edən data class
data class CallLogItem(
    val phoneNumber: String,
    val timestamp: Long,   // Zəngin baş verdiyi zaman (milisaniyə olaraq)
    val duration: Long,    // Zəngin müddəti (saniyə olaraq)
    val callType: Int      // Zəngin növü (Məs: CallLog.Calls.INCOMING_TYPE)
)
