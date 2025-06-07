plugins {
    // Gradle Pluginləri üçün alias istifadəsi (libs.versions.toml faylından gəlir)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ruhidjavadoff.rjexportcalllogsfree"
    compileSdk = 35 // Ən son SDK

    defaultConfig {
        applicationId = "com.ruhidjavadoff.rjexportcalllogsfree"
        minSdk = 26 // Apache POI üçün 26-da qalır
        targetSdk = 35 // Ən son SDK
        versionCode = 8
        versionName = "Alfa 3.87"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true // MultiDex aktivdir
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Minifikasiya deaktivdir
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro" // Proguard qaydaları
            )
        }
    }
    compileOptions {
        // Core Library Desugaring aktivdir (Java 8+ API-ləri üçün)
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11 // Java 11 istifadə olunur
        targetCompatibility = JavaVersion.VERSION_11 // Java 11 istifadə olunur
    }
    kotlinOptions {
        jvmTarget = "11" // Kotlin üçün JVM hədəfi Java 11
    }
    buildFeatures {
        viewBinding = true // View Binding aktivdir
    }
    // packagingOptions -> packaging olaraq dəyişdirildi (köhnə sintaksis xəbərdarlığı üçün)
    packaging {
        // Apache POI və digər kitabxanalarla bağlı potensial dublikat faylları xaric edirik
        resources.excludes.add("META-INF/services/javax.xml.stream.XMLInputFactory")
        resources.excludes.add("META-INF/services/javax.xml.stream.XMLOutputFactory")
        resources.excludes.add("META-INF/services/javax.xml.stream.XMLEventFactory")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat")
        resources.excludes.add("log4j2.component.properties")
        resources.excludes.add("log4j2-test.properties")
        resources.excludes.add("log4j2.xml")
    }
}

dependencies {

    // Əsas Kotlin və AndroidX kitabxanaları (libs.versions.toml-dan gəlir)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation("androidx.core:core-splashscreen:1.0.1") // Və ya ən son versiya
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Apache POI for Excel (.xlsx) - Yenilənmiş versiyalar
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("org.apache.xmlbeans:xmlbeans:5.2.0")
    implementation("org.apache.commons:commons-compress:1.25.0")
    implementation("org.apache.commons:commons-collections4:4.4") // Bu versiya dəyişməyib

    // SLF4J API and NOP Binding - Yenilənmiş versiyalar
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("org.slf4j:slf4j-nop:2.0.12") // NOP versiyası da API ilə uyğunlaşdırıldı

    // XML View sistemi üçün lazımi kitabxanalar - Yenilənmiş versiyalar
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Bu versiya sizin verdiyiniz kimi qaldı (2.2.1 əvəzinə)

    // RecyclerView üçün - Yenilənmiş versiya
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // MultiDex üçün
    implementation("androidx.multidex:multidex:2.0.1") // Bu versiya adətən dəyişmir

    // Test kitabxanaları (libs.versions.toml-dan gəlir)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Core library desugaring üçün asılılıq - Yenilənmiş versiya
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

}
