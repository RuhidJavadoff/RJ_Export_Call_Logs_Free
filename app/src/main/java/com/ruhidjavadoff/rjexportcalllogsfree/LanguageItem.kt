package com.ruhidjavadoff.rjexportcalllogsfree

data class LanguageItem(
    val code: String, // Dilin kodu (məsələn, "en", "az", "system")
    val displayName: String, // Dilin hazırkı tətbiq dilində göstərilən adı
    val nativeName: String // Dilin öz orijinal adı (məsələn, "English", "Azərbaycan dili", "日本語")
)