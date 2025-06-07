package com.ruhidjavadoff.rjexportcalllogsfree

data class AppLanguage(
    val code: String,         // Dil kodu, məsələn: "en", "az", "tr", və ya "system" (sistem dili üçün xüsusi kod)
    val nativeName: String,   // Dilin öz orijinal adı, məsələn: "English", "Azərbaycan dili", "Türkçe", "日本語"
    // "system" kodu üçün bu, strings.xml-dən gələn lokallaşdırılmış mətn olacaq (məs: "Cihaz dili")
    val englishName: String? = null // Dilin ingiliscə adı, məsələn: "English", "Azerbaijani", "Turkish"
    // nativeName ilə eynidirsə və ya "system" üçündürsə null ola bilər.
)