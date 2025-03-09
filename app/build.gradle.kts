plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // ✅ Firebase Plugin
}

android {
    namespace = "mobile.nganchang.chang"
    compileSdk = 35

    defaultConfig {
        applicationId = "mobile.nganchang.chang"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    // ✅ Firebase BOM ล่าสุด
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // ✅ Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // ✅ Firebase Firestore (สำหรับจัดเก็บข้อมูล)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // ✅ Firebase Storage (สำหรับอัปโหลดไฟล์รูป)
    implementation("com.google.firebase:firebase-storage-ktx")

    // ✅ Glide (ใช้โหลดรูปภาพจาก URL)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ✅ RecyclerView (แสดงรายการ)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ✅ ไลบรารีพื้นฐานของ Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.cardview:cardview:1.0.0")

    // ✅ ทดสอบ
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}
