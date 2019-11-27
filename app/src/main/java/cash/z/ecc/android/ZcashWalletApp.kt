package cash.z.ecc.android

import android.content.Context
import android.os.Build
import cash.z.ecc.android.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication


class ZcashWalletApp : DaggerApplication() {

    override fun onCreate() {
        instance = this
        // Setup handler for uncaught exceptions.
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(ExceptionReporter(Thread.getDefaultUncaughtExceptionHandler()))
//        Twig.plant(TroubleshootingTwig())
    }

    /**
     * Implement the HasActivityInjector behavior so that dagger knows which [AndroidInjector] to use.
     */
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
//        MultiDex.install(this)
    }

    companion object {
        lateinit var instance: ZcashWalletApp
    }

    class ExceptionReporter(val ogHandler: Thread.UncaughtExceptionHandler) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(t: Thread?, e: Throwable?) {
//            trackCrash(e, "Top-level exception wasn't caught by anything else!")
//            Analytics.clear()
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
