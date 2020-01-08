package cash.z.ecc.android

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import cash.z.ecc.android.di.component.AppComponent
import cash.z.ecc.android.di.component.DaggerAppComponent
import cash.z.wallet.sdk.ext.TroubleshootingTwig
import cash.z.wallet.sdk.ext.Twig
import cash.z.wallet.sdk.ext.twig


class ZcashWalletApp : Application(), CameraXConfig.Provider {

    var creationTime: Long = 0
        private set

    var creationMeasured: Boolean = false

    override fun onCreate() {
        creationTime = System.currentTimeMillis()
        instance = this
        // Setup handler for uncaught exceptions.
        super.onCreate()

        component = DaggerAppComponent.factory().create(this)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionReporter(Thread.getDefaultUncaughtExceptionHandler()))
        Twig.plant(TroubleshootingTwig())
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
//        MultiDex.install(this)
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    companion object {
        lateinit var instance: ZcashWalletApp
        lateinit var component: AppComponent
    }

    class ExceptionReporter(val ogHandler: Thread.UncaughtExceptionHandler) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(t: Thread?, e: Throwable?) {
//            trackCrash(e, "Top-level exception wasn't caught by anything else!")
//            Analytics.clear()
            twig("Uncaught Exception: $e")
            ogHandler.uncaughtException(t, e)
        }
    }
}


fun ZcashWalletApp.isEmulator(): Boolean {
    val goldfish = Build.HARDWARE.contains("goldfish");
    val emu = (System.getProperty("ro.kernel.qemu", "")?.length ?: 0) > 0;
    val sdk = Build.MODEL.toLowerCase().contains("sdk")
    return goldfish || emu || sdk;
}
