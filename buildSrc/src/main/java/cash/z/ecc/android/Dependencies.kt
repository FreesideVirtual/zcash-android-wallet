package cash.z.ecc.android

object Deps {
    // For use in the top-level build.gradle which gives an error when provided
    // `Deps.Kotlin.version` directly
    const val kotlinVersion = "1.3.60"

    const val compileSdkVersion = 29
    const val buildToolsVersion = "29.0.2"
    const val minSdkVersion = 21
    const val targetSdkVersion = 29

    object AndroidX {
        const val APPCOMPAT =               "androidx.appcompat:appcompat:1.1.0"
        const val CORE_KTX =                "androidx.core:core-ktx:1.1.0"
        const val CONSTRAINT_LAYOUT =       "androidx.constraintlayout:constraintlayout:1.1.3"
        const val FRAGMENT_KTX =            "androidx.fragment:fragment-ktx:1.1.0-beta01"
        const val MULTIDEX =                "androidx.multidex:multidex:2.0.1"
        object Navigation :     Version("2.1.0") {
            val FRAGMENT_KTX =              "androidx.navigation:navigation-fragment-ktx:$version"
            val UI_KTX =                    "androidx.navigation:navigation-ui-ktx:$version"
        }
        object Lifecycle:       Version("2.2.0-rc02") {
            val LIFECYCLE_RUNTIME_KTX =     "androidx.lifecycle:lifecycle-runtime-ktx:$version"
        }
    }
    object Dagger :             Version("2.25.2") {
        val ANDROID_SUPPORT =               "com.google.dagger:dagger-android-support:$version"
        val ANDROID_PROCESSOR =             "com.google.dagger:dagger-android-processor:$version"
        val COMPILER =                      "com.google.dagger:dagger-compiler:$version"
    }
    object Google {
        const val MATERIAL =                "com.google.android.material:material:1.1.0-beta01"
    }
    object Kotlin :             Version(kotlinVersion) {
        val STDLIB =                        "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"
        object Coroutines :     Version("1.3.2") {
            val ANDROID =                   "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
            val CORE =                      "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
            val TEST =                      "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
        }
    }

    object Test {
        const val JUNIT =                   "junit:junit:4.12"
        object Android {
            const val JUNIT =               "androidx.test.ext:junit:1.1.1"
            const val ESPRESSO =            "androidx.test.espresso:espresso-core:3.2.0"
        }
    }
}

open class Version(@JvmField val version: String)  {
    @JvmField val t = version
}

//zzz
//zzz "androidx.constraintlayout:constraintlayout:2.0.0-alpha3"
//zzz "androidx.core:core:1.1.0-alpha05"
//zzz "androidx.core:core-ktx:1.0.0"
//zzz "androidx.fragment:fragment:1.1.0-beta01"
//zzz "androidx.fragment:fragment-ktx:1.1.0-beta01"
//zzz "androidx.multidex:multidex:2.0.1"
//zzz "androidx.navigation:navigation-fragment-ktx:${versions.navigation}"
//zzz "androidx.navigation:navigation-ui-ktx:${versions.navigation}"
//zzz "androidx.test.espresso:espresso-core:3.1.0"
//zzz "androidx.test:runner:1.1.0"
//zzz "cash.z.android.wallet:zcash-android-testnet:1.9.1-alpha@aar"
//zzz "com.airbnb.android:lottie:3.0.0"
//zzz "com.facebook.stetho:stetho:1.5.1"
//zzz "com.google.android.material:material:1.1.0-alpha06"
//zzz "com.google.dagger:dagger-android-processor:${versions.dagger}"
//zzz "com.google.dagger:dagger-android-support:${versions.dagger}"
//zzz "com.google.dagger:dagger-compiler:${versions.dagger}"
//zzz "com.leinardi.android:speed-dial:2.0.0"
//zzz "com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0"
//zzz "org.jetbrains.kotlin:kotlin-reflect:${versions.kotlin}"
//zzz "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}"
//zzz "org.jetbrains.kotlinx:kotlinx-coroutines-android:${versions.coroutines}"
//zzz "org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.coroutines}"
//zzz "org.junit.jupiter:junit-jupiter-api:${versions.junit5}"
//zzz "org.junit.jupiter:junit-jupiter-api:${versions.junit5}"
//zzz "org.junit.jupiter:junit-jupiter-engine:${versions.junit5}"
//zzz "org.mockito:mockito-junit-jupiter:2.26.0",
