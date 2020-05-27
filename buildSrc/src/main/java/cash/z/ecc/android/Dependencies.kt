package cash.z.ecc.android

object Deps {
    // For use in the top-level build.gradle which gives an error when provided
    // `Deps.Kotlin.version` directly
    const val kotlinVersion = "1.3.61"

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
        object Navigation :     Version("2.2.0") {
            val FRAGMENT_KTX =              "androidx.navigation:navigation-fragment-ktx:$version"
            val UI_KTX =                    "androidx.navigation:navigation-ui-ktx:$version"
        }
        object Lifecycle:       Version("2.2.0-rc02") {
            val LIFECYCLE_RUNTIME_KTX =     "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            val LIFECYCLE_EXTENSIONS =      "androidx.lifecycle:lifecycle-extensions:$version"
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
    object JavaX {
        const val INJECT =                        "javax.inject:javax.inject:1"
    }
    object Kotlin :             Version(kotlinVersion) {
        val STDLIB =                        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        object Coroutines :     Version("1.3.2") {
            val ANDROID =                   "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
            val CORE =                      "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
            val TEST =                      "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
        }
    }
    object Zcash {
        val ANDROID_WALLET_PLUGINS = "com.github.zcash:zcash-android-wallet-plugins:1.0.0"
    }
    object Misc {
        object Plugins {
            val SECURE_STORAGE = "de.adorsys.android:securestoragelibrary:1.2.2"
            object Mnemonics {
                val SPONGY_CASTLE = "com.madgag.spongycastle:core:1.58.0.0"
                val NOVACRYPTO_BIP39 = "io.github.novacrypto:BIP39:2019.01.27"
                val NOVACRYPTO_SECURESTRING = "io.github.novacrypto:securestring:2019.01.27"
            }
            val QR_SCANNER = "com.google.zxing:core:3.2.1"
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

open class Version(@JvmField val version: String)

